package com.example.table.controller;

import com.example.table.model.Asset;
import com.example.table.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<Asset> getAllAssets() {
        return assetService.getAllAssets();
    }

    @PostMapping()
    public Asset createAsset(@Valid @RequestBody Asset asset) {
        return assetService.createAsset(asset);
    }

    @PatchMapping("/{id}")
    public Asset updateAsset(@Valid @PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return assetService.updateAsset(id, updates);
    }

    @DeleteMapping("/{id}")
    public Asset removeAsset(@Valid @PathVariable Long id) {
        return assetService.removeAsset(id);
    }
}
