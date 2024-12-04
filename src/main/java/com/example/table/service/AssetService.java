package com.example.table.service;

import com.example.table.enumeration.AssetFieldNameUpdateEnum;
import com.example.table.enumeration.AssetStatus;
import com.example.table.model.Asset;
import com.example.table.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    @Autowired
    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    public Asset createAsset(Asset asset) {
        //TODO: if user_id is given then status must be ASSIGNED
        return assetRepository.save(asset);
    }

    public Asset updateAsset(Long id, Map<String, ?> updates) {
        Optional<Asset> optionalAsset = assetRepository.findById(id);
        if (optionalAsset.isPresent()) {
            Asset asset = optionalAsset.get();
            updates.forEach((key, value) -> {
                // Match the key with the enum field
                AssetFieldNameUpdateEnum fieldEnum = Arrays.stream(AssetFieldNameUpdateEnum.values())
                        .filter(enumValue -> enumValue.getFieldName().equals(key))
                        .findFirst()
                        .orElse(null);

                if (fieldEnum != null) {
                    switch (fieldEnum) {
                        case NAME:
                            asset.setModelName((String) value);
                            break;
                        case TYPE:
                            asset.setType((String) value);
                            break;
                        case STATUS:
                            //TODO: if status is either AVAILABLE/UNAVAILABLE then user_id must become null
                            if (value instanceof String statusValue) {
                                asset.setStatus(AssetStatus.valueOf(statusValue));
                            }
                            break;
                        case PRICE:
                            if (value instanceof Number numberValue) {
                                asset.setCost(BigDecimal.valueOf(numberValue.doubleValue()));
                            }
                            break;
                        case USER_ID:
                            //TODO: check status before updating user_id
                            if (value instanceof Number numberValue) {
                                asset.setUserID(numberValue.longValue());
                            }
                            break;
                        default:
                            // Ignore or throw an exception for unsupported fields
                            break;
                    }
                }
            });
            assetRepository.save(asset);
            return asset;
        }
        return null;
    }

    public Asset removeAsset(Long id) {
        Asset asset = assetRepository.findById(id).orElse(null);
        if (asset != null) {
            assetRepository.delete(asset);
        }
        return asset;
    }
}

