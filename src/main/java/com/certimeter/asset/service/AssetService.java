package com.certimeter.asset.service;

import com.certimeter.asset.dto.AssetResPagination;
import com.certimeter.asset.enumeration.*;
import com.certimeter.asset.exception.FailureException;
import com.certimeter.asset.model.Asset;
import com.certimeter.asset.model.AssetHistory;
import com.certimeter.asset.repository.AssetHistoryRepository;
import com.certimeter.asset.repository.AssetRepository;
import com.certimeter.asset.repository.AssetSpecification;
import com.certimeter.asset.requestcontext.RequestContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetHistoryRepository assetHistoryRepository;
    private final RequestContext requestContext;
    private static final String INVALID_INPUT = "Asset cannot have a user assigned when status is UNAVAILABLE";

    public AssetService(AssetRepository assetRepository, AssetHistoryRepository assetHistoryRepository, RequestContext requestContext) {
        this.assetRepository = assetRepository;
        this.assetHistoryRepository = assetHistoryRepository;
        this.requestContext = requestContext;
    }

    // Update the getAllAssets method to exclude deleted assets
    public AssetResPagination getAllAssets(int pageNo, int pageSize, Optional<String> username, Optional<String> usernameMatchMode, Optional<String> modelName, Optional<String> modelNameMatchMode, Optional<String> type, Optional<String> typeMatchMode, Optional<String> status, Optional<String> statusMatchMode, Optional<String> cost, Optional<String> costMatchMode, Optional<String> action, Optional<String> actionMatchMode) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Specification<Asset> spec = buildAssetSpecifications(username, usernameMatchMode, modelName, modelNameMatchMode, type, typeMatchMode, status, statusMatchMode, cost, costMatchMode, action, actionMatchMode);
        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"))); // Exclude deleted assets
        Page<Asset> pagedAssets = assetRepository.findAll(spec, pageable);

        return buildAssetResPagination(pagedAssets, pagedAssets.getContent());
    }

    public AssetResPagination getAllUserAssets(Long userId, int pageNo, int pageSize, Optional<String> username, Optional<String> usernameMatchMode, Optional<String> modelName, Optional<String> modelNameMatchMode, Optional<String> type, Optional<String> typeMatchMode, Optional<String> status, Optional<String> statusMatchMode, Optional<String> cost, Optional<String> costMatchMode, Optional<String> action, Optional<String> actionMatchMode) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Specification<Asset> spec = buildAssetSpecifications(username, usernameMatchMode, modelName, modelNameMatchMode, type, typeMatchMode, status, statusMatchMode, cost, costMatchMode, action, actionMatchMode);
        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"))); // Exclude deleted assets
        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userID"), userId)); // Filter by userId
        Page<Asset> pagedAssets = assetRepository.findAll(spec, pageable);

        return buildAssetResPagination(pagedAssets, pagedAssets.getContent());
    }

    private Specification<Asset> addSpecification(
            Specification<Asset> spec, String field, Optional<String> value, Optional<String> matchModeStr) {

        if (value.isPresent() && matchModeStr.isPresent()) {
            MatchMode matchMode = getMatchModeFromString(matchModeStr.get());
            if (field.equals("username")) {
                return spec.and(AssetSpecification.matchUsername(value.get(), matchMode));
            } else {
                return spec.and(AssetSpecification.matchMode(field, value.get(), matchMode));
            }
        }
        return spec;
    }

    private AssetResPagination buildAssetResPagination(Page<Asset> pagedAsset, List<Asset> assets) {
        return AssetResPagination.builder()
                .pageNo(pagedAsset.getNumber())
                .pageSize(pagedAsset.getSize())
                .totalElements(pagedAsset.getTotalElements())
                .totalPages(pagedAsset.getTotalPages())
                .last(pagedAsset.isLast())
                .data(assets)
                .build();
    }

    private Specification<Asset> buildAssetSpecifications(Optional<String> username, Optional<String> usernameMatchMode, Optional<String> modelName, Optional<String> modelNameMatchMode, Optional<String> type, Optional<String> typeMatchMode, Optional<String> status, Optional<String> statusMatchMode, Optional<String> cost, Optional<String> costMatchMode, Optional<String> action, Optional<String> actionMatchMode) {
        Specification<Asset> spec = Specification.where(null);
        spec = addSpecification(spec, "username", username, usernameMatchMode);
        spec = addSpecification(spec, "modelName", modelName, modelNameMatchMode);
        spec = addSpecification(spec, "type", type, typeMatchMode);
        spec = addSpecification(spec, "status", status, statusMatchMode);
        spec = addSpecification(spec, "cost", cost, costMatchMode);
        spec = addSpecification(spec, "action", action, actionMatchMode);
        return spec;
    }

    public Asset createAsset(Asset asset) {
        try {
            if (asset.getUserID() != null) {
                if (asset.getStatus() == AssetStatus.UNAVAILABLE) {
                    throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                            INVALID_INPUT);
                }
                // Asset might've created with status AVAILABLE, but user is assigned
                asset.setStatus(AssetStatus.ASSIGNED);
            } else {
                if (asset.getStatus() == AssetStatus.ASSIGNED) {
                    throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                            "Asset must have a user assigned when status is ASSIGNED");
                }
            }

            Asset createdAsset = assetRepository.save(asset);

            logAssetHistory(createdAsset, null, AssetAction.CREATED, "Asset created [" + createdAsset.getModelName() + " - " + createdAsset.getType() + "]");
            return createdAsset;
        } catch (DataIntegrityViolationException e) {
            throw new FailureException(HttpResponseEnum.FOREIGN_KEY_CONSTRAINT_VIOLATION);
        }
    }

    //When batch updating, it might give SQL error if foreign key constraint fails
    public List<Asset> createAssets(List<Asset> assets) {
        List<Asset> newAssets = new ArrayList<>();
        for (Asset asset : assets) {
            if (asset.getUserID() != null) {
                if (asset.getStatus() == AssetStatus.UNAVAILABLE) {
                    throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                            INVALID_INPUT);
                }
                asset.setStatus(AssetStatus.ASSIGNED);
            } else {
                if (asset.getStatus() == AssetStatus.ASSIGNED) {
                    throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                            "Asset must have a user assigned when status is ASSIGNED");
                }
            }
            newAssets.add(asset);
        }
        List<Asset> savedAssets = assetRepository.saveAll(newAssets);
        for (Asset savedAsset : savedAssets) {
            logAssetHistory(savedAsset, null, AssetAction.CREATED, "Asset created [" + savedAsset.getModelName() + " - " + savedAsset.getType() + "]");
        }
        return savedAssets;
    }

    public Asset updateAsset(Long id, Map<String, ?> updates) {
        try {
            Optional<Asset> optionalAsset = assetRepository.findById(id);
            if (optionalAsset.isEmpty()) {
                throw new FailureException(HttpResponseEnum.RESOURCE_NOT_FOUND, "Asset not found");
            }

            Asset asset = optionalAsset.get();
            Asset previousState = new Asset(); // Clone asset state for history logging
            BeanUtils.copyProperties(asset, previousState);

            String statusUpdate = null;
            Long userIdUpdate = null;
            StringBuilder commentBuilder = new StringBuilder("Asset updated: ");

            for (Map.Entry<String, ?> entry : updates.entrySet()) {
                AssetFieldNameUpdateEnum fieldEnum = Arrays.stream(AssetFieldNameUpdateEnum.values())
                        .filter(enumValue -> enumValue.getFieldName().equals(entry.getKey()))
                        .findFirst()
                        .orElseThrow(() -> new FailureException(HttpResponseEnum.INVALID_INPUT, "Invalid field name"));

                if (fieldEnum != null) {
                    switch (fieldEnum) {
                        case STATUS:
                            if (entry.getValue() instanceof String statusValue) {
                                statusUpdate = statusValue;
                                if (!statusUpdate.equals(asset.getStatus().name())) {
                                    commentBuilder.append("status changed from ").append(asset.getStatus()).append(" to ").append(statusValue).append("; ");
                                }
                            }
                            break;
                        case USER_ID:
                            if (entry.getValue() instanceof String stringValue) {
                                userIdUpdate = Long.valueOf(stringValue);
                            } else if (entry.getValue() instanceof Number numberValue) {
                                userIdUpdate = numberValue.longValue();
                            }
                            if (asset.getUserID() != null && !Objects.equals(userIdUpdate, asset.getUserID())) {
                                commentBuilder.append("user ID changed from ").append(asset.getUserID()).append(" to ").append(userIdUpdate).append("; ");
                            }

                            break;
                        default:
                            Object currentValue = getFieldValue(asset, fieldEnum);
                            Object newValue = entry.getValue();

                            //System.out.println("The type of obj is: " + newValue.getClass().getName());

                            if (currentValue instanceof BigDecimal && newValue instanceof Integer) {
                                // Convert Integer to BigDecimal for comparison
                                BigDecimal newCost = BigDecimal.valueOf((Integer) newValue);
                                if (((BigDecimal) currentValue).compareTo(newCost) != 0) {
                                    applyFieldUpdate(asset, fieldEnum, newCost);
                                    commentBuilder.append(fieldEnum.getFieldName()).append(" from ").append(currentValue).append(" changed to ").append(newCost).append("; ");
                                }
                            } else if (!Objects.equals(currentValue, newValue)) {
                                applyFieldUpdate(asset, fieldEnum, newValue);
                                commentBuilder.append(fieldEnum.getFieldName()).append(" from ").append(currentValue).append(" changed to ").append(newValue).append("; ");
                            }
                            break;

                    }
                }
            }

            validateAssetUpdates(asset, statusUpdate, userIdUpdate);

            if (statusUpdate != null) {
                asset.setStatus(AssetStatus.valueOf(statusUpdate));
            }

            if (userIdUpdate != null) {
                asset.setUserID(userIdUpdate);
            }

            Asset updatedAsset = assetRepository.save(asset);
            logAssetHistory(updatedAsset, previousState, AssetAction.UPDATED, commentBuilder.toString());
            return updatedAsset;
        } catch (DataIntegrityViolationException e) {
            throw new FailureException(HttpResponseEnum.FOREIGN_KEY_CONSTRAINT_VIOLATION);
        } catch (BeansException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getFieldValue(Asset asset, AssetFieldNameUpdateEnum fieldEnum) {
        switch (fieldEnum) {
            case NAME:
                return asset.getModelName();
            case TYPE:
                return asset.getType();
            case PRICE:
                return asset.getCost();
            default:
                throw new FailureException(HttpResponseEnum.INVALID_INPUT, "Unsupported field: " + fieldEnum.name());
        }
    }

    private void validateAssetUpdates(Asset asset, String statusUpdate, Long userIdUpdate) {
        if (statusUpdate != null && userIdUpdate != null) {
            validateStatusAndUserIdCombination(statusUpdate);
        } else if (statusUpdate != null) {
            validateStatusChange(asset, statusUpdate);
        } else if (userIdUpdate != null) {
            validateUserIdChange(asset);
        }
    }

    /*
    * - AVAILABLE + user_id CONTRADICTION asset cannot be available and be assigned. If we want to be strict about it let's just throw an error
- ASSIGNED + user_id NO PROBLEM
- UNAVAILABLE + user_id CONTRADICTION asset cannot unavailable and be assigned. If we want to be strict about it let's just throw an error
* */

    // I know that user_id is not null
    private void validateStatusAndUserIdCombination(String statusUpdate) {
        if (AssetStatus.valueOf(statusUpdate) == AssetStatus.AVAILABLE || AssetStatus.valueOf(statusUpdate) == AssetStatus.UNAVAILABLE) {
            throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                    "Asset cannot have a user assigned when it is " + statusUpdate);
        }
    }

    /*
    * - AVAILABLE/UNAVAILABLE the user_id must become null
- if it is ASSIGNED then that means user_id must become not null in some way, since we're only modifying status and not the user this should just throw an exception.*/
    private void validateStatusChange(Asset asset, String statusUpdate) {
        if (AssetStatus.valueOf(statusUpdate) == AssetStatus.AVAILABLE || AssetStatus.valueOf(statusUpdate) == AssetStatus.UNAVAILABLE) {
            asset.setUserID(null);
        } else {
            throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                    "Asset must have a user assigned, the user is: " + asset.getUserID());
        }
    }

    /*
    * When only modifying user_id:
-if status is ASSIGNED then that shouldn't be a problem since that means the previous user is no longer the owner.
-if status is AVAILABLE we assume that user_id is also null (but just in case maybe check and if not the case throw an exception) and we just assign the user.
-if status is UNAVAILABLE we can't assign the user throw an exception*/
    private void validateUserIdChange(Asset asset) {
        switch (asset.getStatus()) {
            case AVAILABLE:
                if (asset.getUserID() != null) {
                    throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                            "Asset is already assigned with status AVAILABLE, check DB consistency");
                }
                asset.setStatus(AssetStatus.ASSIGNED);
                break;
            case UNAVAILABLE:
                throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                        INVALID_INPUT);
            case ASSIGNED:
                // No issue if assigning a new user
                break;
        }
    }

    private void applyFieldUpdate(Asset asset, AssetFieldNameUpdateEnum fieldEnum, Object value) {
        switch (fieldEnum) {
            case NAME:
                asset.setModelName((String) value);
                break;
            case TYPE:
                asset.setType((String) value);
                break;
            case PRICE:
                if (value instanceof Number numberValue) {
                    asset.setCost(BigDecimal.valueOf(numberValue.doubleValue()));
                }
                break;
            default:
                throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                        "Unsupported field: " + fieldEnum.name());
        }
    }

    // Update the removeAsset method to perform a soft delete
    public Asset removeAsset(Long id) {
        Optional<Asset> optionalAsset = assetRepository.findById(id);
        if (optionalAsset.isEmpty()) {
            throw new FailureException(HttpResponseEnum.RESOURCE_NOT_FOUND, "Asset not found");
        }

        Asset asset = optionalAsset.get();
        asset.setDeleted(true); // Perform soft delete


        logAssetHistory(asset, null, AssetAction.DELETED, "Asset deleted [" + asset.getModelName() + " - " + asset.getType() + "]");
        asset.setUserID(null);
        assetRepository.save(asset);
        return asset;
    }


    private void logAssetHistory(Asset currentAsset, Asset previousAsset, AssetAction action, String comment) {
        AssetHistory history = new AssetHistory();
        history.setAssetId(currentAsset.getId());
        history.setAdminId(requestContext.getUserID());
        history.setUserId(currentAsset.getUserID());
        history.setStatus(currentAsset.getStatus());
        history.setAction(action);
        history.setDate(LocalDateTime.now());
        history.setComment(comment);

        if (action == AssetAction.UPDATED && previousAsset != null && !previousAsset.getStatus().equals(currentAsset.getStatus())) {
            history.setComment(comment + " - Previous status: " + previousAsset.getStatus());
        }

        assetHistoryRepository.save(history);
    }

    // this could be in a common shared library
    private MatchMode getMatchModeFromString(String matchModeStr) {
        switch (matchModeStr.toLowerCase()) {
            case "startswith":
                return MatchMode.STARTS_WITH;
            case "contains":
                return MatchMode.CONTAINS;
            case "notcontains":
                return MatchMode.NOT_CONTAINS;
            case "endswith":
                return MatchMode.ENDS_WITH;
            case "equals":
                return MatchMode.EQUALS;
            case "notequals":
                return MatchMode.NOT_EQUALS;
            case "nofilter":
                return MatchMode.NO_FILTER;
            case "dateis":
                return MatchMode.DATE_IS;
            case "dateisnot":
                return MatchMode.DATE_IS_NOT;
            case "datebefore":
                return MatchMode.DATE_BEFORE;
            case "dateafter":
                return MatchMode.DATE_AFTER;
            case "gt":
                return MatchMode.GREATER_THAN;
            case "lt":
                return MatchMode.LESS_THAN;
            case "gte":
                return MatchMode.GREATER_THAN_OR_EQUALS;
            case "lte":
                return MatchMode.LESS_THAN_OR_EQUALS;
            default:
                throw new IllegalArgumentException("Invalid match mode: " + matchModeStr);
        }
    }
}

