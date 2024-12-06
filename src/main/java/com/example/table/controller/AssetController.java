package com.example.table.controller;

import com.example.table.enumeration.HttpResponseEnum;
import com.example.table.enumeration.UserRoleEnum;
import com.example.table.exception.FailureException;
import com.example.table.model.Asset;
import com.example.table.requestcontext.RequestContext;
import com.example.table.service.AssetService;
import com.example.table.service.AuthorizationService;
import jakarta.validation.Valid;
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
    public List<Asset> getAllAssets() {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        return assetService.getAllAssets();
    }

    @PostMapping()
    public Asset createAsset(@Valid @RequestBody Asset asset) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        return assetService.createAsset(asset);
    }

    @PatchMapping("/{id}")
    public Asset updateAsset(@Valid @PathVariable Long id, @RequestBody Map<String, Object> updates) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        return assetService.updateAsset(id, updates);
    }

    @DeleteMapping("/{id}")
    public Asset removeAsset(@Valid @PathVariable Long id) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.FORBIDDEN);
        }
        return assetService.removeAsset(id);
    }
}
