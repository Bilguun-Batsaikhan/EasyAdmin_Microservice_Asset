package com.certimeter.asset.repository;

import com.certimeter.asset.model.AssetHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long>, JpaSpecificationExecutor<AssetHistory> {
    Page<AssetHistory> findAssetHistoriesByAssetId(Long assetId, Pageable pageable);
    long countByAssetId(Long assetId);
}

