package com.example.table.repository;

import com.example.table.model.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AssetRepository extends JpaRepository<Asset, Long> {
    Page<Asset> findAssetsByUserID(Long userId, Pageable pageable);
}
