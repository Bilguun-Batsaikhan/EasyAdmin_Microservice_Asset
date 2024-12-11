package com.certimeter.asset.controller;

import com.certimeter.asset.dto.AssetHistoryResPagination;
import com.certimeter.asset.enumeration.HttpResponseEnum;
import com.certimeter.asset.enumeration.UserRoleEnum;
import com.certimeter.asset.exception.FailureException;
import com.certimeter.asset.model.AssetHistory;
import com.certimeter.asset.requestcontext.RequestContext;
import com.certimeter.asset.service.AssetHistoryService;
import com.certimeter.asset.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;

@RestController
@RequestMapping("/assets/history")
public class AssetHistoryController {
    private final AssetHistoryService assetHistoryService;
    private final RequestContext requestContext;
    private final AuthorizationService authorizationService;

    public AssetHistoryController(AssetHistoryService assetHistoryService, RequestContext requestContext, AuthorizationService authorizationService) {
        this.assetHistoryService = assetHistoryService;
        this.requestContext = requestContext;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<AssetHistoryResPagination> getAllAssetHistories(@RequestParam(value = "page", defaultValue = "0", required = false) int pageNo,
                                                                          @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.AUTHORIZATION_FAILED);
        }
        return new ResponseEntity<>(assetHistoryService.getAllAssetHistories(pageNo, pageSize), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetHistory> getAssetHistory(@PathVariable Long id) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.AUTHORIZATION_FAILED);
        }
        return new ResponseEntity<>(assetHistoryService.getAssetHistory(id), HttpStatus.OK);
    }
}
