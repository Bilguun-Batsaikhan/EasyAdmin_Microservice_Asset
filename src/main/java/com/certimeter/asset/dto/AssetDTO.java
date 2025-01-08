package com.certimeter.asset.dto;

import lombok.Data;

@Data
public class AssetDTO {
    private Long id;
    private String modelName;
    private String type;
    private String status;
    private String username;

    // Getters and Setters
}