package com.certimeter.asset.service;

import com.certimeter.asset.dto.AssetHistoryResPagination;
import com.certimeter.asset.enumeration.HttpResponseEnum;
import com.certimeter.asset.enumeration.MatchMode;
import com.certimeter.asset.exception.FailureException;
import com.certimeter.asset.model.Asset;
import com.certimeter.asset.model.AssetHistory;
import com.certimeter.asset.model.User;
import com.certimeter.asset.repository.AssetHistoryRepository;
import com.certimeter.asset.repository.AssetHistorySpecification;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class AssetHistoryService {
    private final AssetHistoryRepository assetHistoryRepository;
    Logger LOG = Logger.getLogger(AssetHistoryService.class.getName());
    public AssetHistoryService(AssetHistoryRepository assetHistoryRepository) {
        this.assetHistoryRepository = assetHistoryRepository;
    }

    public AssetHistoryResPagination getAllAssetHistories(int pageNo, int pageSize, Optional<String> assetId, Optional<String> assetIdMatchMode, Optional<String> modelName, Optional<String> modelNameMatchMode, Optional<String> admin, Optional<String> adminMatchMode, Optional<String> user, Optional<String> userMatchMode, Optional<String> status, Optional<String> statusMatchMode, Optional<String> date, Optional<String> dateMatchMode, Optional<String> action, Optional<String> actionMatchMode) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Specification<AssetHistory> spec = buildAssetHistorySpecifications(assetId, assetIdMatchMode, modelName, modelNameMatchMode, admin, adminMatchMode, user, userMatchMode, status, statusMatchMode, date, dateMatchMode, action, actionMatchMode);

        Page<AssetHistory> pagedAssetHistories = assetHistoryRepository.findAll(spec, pageable);
        List<AssetHistory> assetHistories = pagedAssetHistories.getContent();

        return buildAssetHistoryResPagination(pagedAssetHistories, assetHistories);
    }

    private Specification<AssetHistory> buildAssetHistorySpecifications(Optional<String> assetId, Optional<String> assetIdMatchMode, Optional<String> modelName, Optional<String> modelNameMatchMode, Optional<String> admin, Optional<String> adminMatchMode, Optional<String> user, Optional<String> userMatchMode, Optional<String> status, Optional<String> statusMatchMode, Optional<String> date, Optional<String> dateMatchMode, Optional<String> action, Optional<String> actionMatchMode) {
        Specification<AssetHistory> spec = Specification.where(null);
        spec = addSpecification(spec, "assetId", assetId, assetIdMatchMode);
        spec = addSpecification(spec, "modelName", modelName, modelNameMatchMode);
        spec = addSpecification(spec, "admin", admin, adminMatchMode);
        spec = addSpecification(spec, "user", user, userMatchMode);
        spec = addSpecification(spec, "status", status, statusMatchMode);
        spec = addSpecification(spec, "date", date, dateMatchMode);
        spec = addSpecification(spec, "action", action, actionMatchMode);
        return spec;
    }

    private Specification<AssetHistory> addSpecification(Specification<AssetHistory> spec, String field, Optional<String> value, Optional<String> matchModeStr) {
        if (value.isPresent() && matchModeStr.isPresent()) {
            MatchMode matchMode = getMatchModeFromString(matchModeStr.get());

            switch (field) {
                case "modelName":
                    return spec.and(AssetHistorySpecification.matchModeInJoin("asset", "modelName", value.get(), matchMode));
                case "admin":
                    return spec.and(AssetHistorySpecification.matchModeInJoin("admin", "username", value.get(), matchMode));
                case "user":
                    return spec.and(AssetHistorySpecification.matchModeInJoin("user", "username", value.get(), matchMode));
                default:
                    //date "2025-01-09" dateMatchMode "dateIs"
                    return spec.and(AssetHistorySpecification.matchMode(field, value.get(), matchMode));
            }
        }
        return spec;
    }


    private AssetHistoryResPagination buildAssetHistoryResPagination(Page<AssetHistory> pagedAssetHistories, List<AssetHistory> assetHistories) {
        AssetHistoryResPagination assetHistoryResPagination = new AssetHistoryResPagination();
        assetHistoryResPagination.setPageNo(pagedAssetHistories.getNumber());
        assetHistoryResPagination.setPageSize(pagedAssetHistories.getSize());
        assetHistoryResPagination.setTotalElements(pagedAssetHistories.getTotalElements());
        assetHistoryResPagination.setTotalPages(pagedAssetHistories.getTotalPages());
        assetHistoryResPagination.setLast(pagedAssetHistories.isLast());
        assetHistoryResPagination.setData(assetHistories);
        return assetHistoryResPagination;
    }

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
            default:
                throw new IllegalArgumentException("Invalid match mode: " + matchModeStr);
        }
    }

    public AssetHistory getAssetHistory(Long id) {
        Optional<AssetHistory> assetHistory = assetHistoryRepository.findById(id);
        if (assetHistory.isEmpty()) {
            throw new FailureException(HttpResponseEnum.RESOURCE_NOT_FOUND, "Asset history not found");
        }
        return assetHistory.get();
    }
}