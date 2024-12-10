package com.example.table.repository;

import com.example.table.model.AssetHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {
}

