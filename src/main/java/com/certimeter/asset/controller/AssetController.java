package com.certimeter.asset.controller;

import com.certimeter.asset.dto.AssetDTO;
import com.certimeter.asset.dto.AssetResPagination;
import com.certimeter.asset.enumeration.HttpResponseEnum;
import com.certimeter.asset.enumeration.UserRoleEnum;
import com.certimeter.asset.exception.FailureException;
import com.certimeter.asset.model.Asset;
import com.certimeter.asset.requestcontext.RequestContext;
import com.certimeter.asset.service.AssetService;
import com.certimeter.asset.service.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                                                           @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize, @RequestParam Optional<String> username,
                                                           @RequestParam Optional<String> usernameMatchMode,
                                                           @RequestParam Optional<String> modelName,
                                                           @RequestParam Optional<String> modelNameMatchMode,
                                                           @RequestParam Optional<String> type,
                                                           @RequestParam Optional<String> typeMatchMode,
                                                           @RequestParam Optional<String> status,
                                                           @RequestParam Optional<String> statusMatchMode,
                                                           @RequestParam Optional<String> cost,
                                                           @RequestParam Optional<String> costMatchMode,
                                                           @RequestParam Optional<String> action,
                                                           @RequestParam Optional<String> actionMatchMode) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            return new ResponseEntity<>(assetService.getAllAssets(pageNo, pageSize, username, usernameMatchMode, modelName, modelNameMatchMode, type, typeMatchMode, status, statusMatchMode, cost, costMatchMode, action, actionMatchMode), HttpStatus.OK);
        } else {
            Long userId = requestContext.getUserID();
            return new ResponseEntity<>(assetService.getAllUserAssets(userId, pageNo, pageSize, username, usernameMatchMode, modelName, modelNameMatchMode, type, typeMatchMode, status, statusMatchMode, cost, costMatchMode, action, actionMatchMode), HttpStatus.OK);
        }
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
