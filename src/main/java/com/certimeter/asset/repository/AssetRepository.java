package com.certimeter.asset.repository;

import com.certimeter.asset.model.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AssetRepository extends JpaRepository<Asset, Long> {
    Page<Asset> findAssetsByUserID(Long userId, Pageable pageable);
}
