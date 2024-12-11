package com.certimeter.asset.service;

import com.certimeter.asset.dto.AssetHistoryResPagination;
import com.certimeter.asset.enumeration.HttpResponseEnum;
import com.certimeter.asset.exception.FailureException;
import com.certimeter.asset.model.AssetHistory;
import com.certimeter.asset.repository.AssetHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetHistoryService {
    private final AssetHistoryRepository assetHistoryRepository;

    public AssetHistoryService(AssetHistoryRepository assetHistoryRepository) {
        this.assetHistoryRepository = assetHistoryRepository;
    }

    public AssetHistoryResPagination getAllAssetHistories(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<AssetHistory> pagedAssetHistories = assetHistoryRepository.findAll(pageable);
        List<AssetHistory> assetHistories = pagedAssetHistories.getContent();
        AssetHistoryResPagination assetHistoryResPagination = new AssetHistoryResPagination();

        assetHistoryResPagination.setPageNo(pagedAssetHistories.getNumber());
        assetHistoryResPagination.setPageSize(pagedAssetHistories.getSize());
        assetHistoryResPagination.setTotalElements(pagedAssetHistories.getTotalElements());
        assetHistoryResPagination.setTotalPages(pagedAssetHistories.getTotalPages());
        assetHistoryResPagination.setLast(pagedAssetHistories.isLast());

        assetHistoryResPagination.setAssetHistoryList(assetHistories);

        return assetHistoryResPagination;
    }

    public AssetHistory getAssetHistory(Long id) {
        Optional<AssetHistory> assetHistory = assetHistoryRepository.findById(id);
        if(assetHistory.isEmpty()) {
            throw new FailureException(HttpResponseEnum.RESOURCE_NOT_FOUND, "Asset history not found");
        }
        return assetHistory.get();
    }
}
