package com.certimeter.asset.repository;

import com.certimeter.asset.model.AssetHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {
}

