package com.example.table.dto;

import com.example.table.model.AssetHistory;
import lombok.Data;
import lombok.Setter;

import java.util.List;
@Data
@Setter
public class AssetHistoryResPagination {
    List<AssetHistory> assetHistoryList;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
