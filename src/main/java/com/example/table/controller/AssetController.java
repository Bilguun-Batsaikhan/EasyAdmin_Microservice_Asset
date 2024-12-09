package com.example.table.controller;

import com.example.table.dto.AssetResPagination;
import com.example.table.enumeration.HttpResponseEnum;
import com.example.table.enumeration.UserRoleEnum;
import com.example.table.exception.FailureException;
import com.example.table.model.Asset;
import com.example.table.requestcontext.RequestContext;
import com.example.table.service.AssetService;
import com.example.table.service.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assetService;
    private final RequestContext requestContext;
    private final AuthorizationService authorizationService;

    public AssetController(AssetService assetService, RequestContext requestContext, AuthorizationService authorizationService) {
        this.assetService = assetService;
        this.requestContext = requestContext;
        this.authorizationService = authorizationService;
    }
    //--------------------------//
    //CRUD operations for Asset //
    //--------------------------//
    @GetMapping
    public ResponseEntity<AssetResPagination> getAllAssets(@RequestParam(value = "page", defaultValue = "0", required = false) int pageNo,
                                                           @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        return new ResponseEntity<>(assetService.getAllAssets(pageNo, pageSize), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Asset> createAsset(@Valid @RequestBody Asset asset) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        Asset createdAsset = assetService.createAsset(asset);
        return new ResponseEntity<>(createdAsset, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Asset>> createAssets(@Valid @RequestBody List<Asset> assets) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        List<Asset> createdAssets = assetService.createAssets(assets);
        return new ResponseEntity<>(createdAssets, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(@Valid @PathVariable Long id, @RequestBody Map<String, Object> updates) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        Asset updatedAsset = assetService.updateAsset(id, updates);
        return new ResponseEntity<>(updatedAsset, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Asset> removeAsset(@Valid @PathVariable Long id) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        Asset removedAsset = assetService.removeAsset(id);
        return new ResponseEntity<>(removedAsset, HttpStatus.OK);
    }
}
