package com.certimeter.asset.repository;

import com.certimeter.asset.model.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface AssetRepository extends JpaRepository<Asset, Long> , JpaSpecificationExecutor<Asset> {
    Page<Asset> findAssetsByUserID(Long userId, Pageable pageable);
    long countByUserID(Long userId);
}
