package com.example.table.service;

import com.example.table.dto.AssetResPagination;
import com.example.table.enumeration.AssetFieldNameUpdateEnum;
import com.example.table.enumeration.AssetStatus;
import com.example.table.enumeration.HttpResponseEnum;
import com.example.table.exception.FailureException;
import com.example.table.model.Asset;
import com.example.table.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    @Autowired
    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public AssetResPagination getAllAssets(int pageNo, int pageSize) {
        Pageable pagebale = PageRequest.of(pageNo, pageSize);
        Page<Asset> pagedAssets = assetRepository.findAll(pagebale);
        List<Asset> assets = pagedAssets.getContent();
        AssetResPagination assetResPagination = new AssetResPagination();

        assetResPagination.setPageNo(pageNo);
        assetResPagination.setPageSize(pageSize);
        assetResPagination.setTotalElements(assetRepository.count());
        assetResPagination.setTotalPages(pagedAssets.getTotalPages());
        assetResPagination.setLast(pagedAssets.isLast());
        assetResPagination.setData(assets);

        return assetResPagination;
    }

    public Asset createAsset(Asset asset) {
        if (asset.getUserID() != null) {
            if(asset.getStatus() == AssetStatus.UNAVAILABLE) {
                throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                        "Asset cannot have a user assigned when status is UNAVAILABLE");
            }
            // Asset might've created with status AVAILABLE, but user is assigned
            asset.setStatus(AssetStatus.ASSIGNED);
        } else {
            if(asset.getStatus() == AssetStatus.ASSIGNED) {
                throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                        "Asset must have a user assigned when status is ASSIGNED");
            }
        }
        return assetRepository.save(asset);
    }

    public Asset updateAsset(Long id, Map<String, ?> updates) {
        Optional<Asset> optionalAsset = assetRepository.findById(id);
        if (optionalAsset.isEmpty()) {
            return null;
        }

        Asset asset = optionalAsset.get();
        String statusUpdate = null;
        Long userIdUpdate = null;

        // Parse and validate updates
        for (Map.Entry<String, ?> entry : updates.entrySet()) {
            AssetFieldNameUpdateEnum fieldEnum = Arrays.stream(AssetFieldNameUpdateEnum.values())
                    .filter(enumValue -> enumValue.getFieldName().equals(entry.getKey()))
                    .findFirst()
                    .orElse(null);

            if (fieldEnum != null) {
                switch (fieldEnum) {
                    case STATUS:
                        if (entry.getValue() instanceof String statusValue) {
                            statusUpdate = statusValue;
                        }
                        break;
                    case USER_ID:
                        if (entry.getValue() instanceof Number numberValue) {
                            userIdUpdate = numberValue.longValue();
                        }
                        break;
                    default:
                        // Process other fields
                        applyFieldUpdate(asset, fieldEnum, entry.getValue());
                        break;
                }
            }
        }

        // Validate the logic for status and user_id updates
        validateAssetUpdates(asset, statusUpdate, userIdUpdate);

        // Apply updates if valid
        if (statusUpdate != null) {
            asset.setStatus(AssetStatus.valueOf(statusUpdate));
        }
        if (userIdUpdate != null) {
            asset.setUserID(userIdUpdate);
        }

        assetRepository.save(asset);
        return asset;
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
                break;
            case UNAVAILABLE:
                throw new FailureException(HttpResponseEnum.INVALID_INPUT,
                        "Asset cannot have a user assigned when status is UNAVAILABLE");
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


    public Asset removeAsset(Long id) {
        Asset asset = assetRepository.findById(id).orElse(null);
        if (asset != null) {
            assetRepository.delete(asset);
        }
        return asset;
    }
}

