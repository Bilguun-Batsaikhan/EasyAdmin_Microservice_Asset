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

import javax.swing.text.html.Option;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

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
                                                                          @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                                          @RequestParam Optional<String> assetId,
                                                                          @RequestParam Optional<String> assetIdMatchMode,
                                                                          @RequestParam Optional<String> modelName,
                                                                          @RequestParam Optional<String> modelNameMatchMode,
                                                                          @RequestParam Optional<String> admin,
                                                                          @RequestParam Optional<String> adminMatchMode,
                                                                          @RequestParam Optional<String> user,
                                                                          @RequestParam Optional<String> userMatchMode,
                                                                          @RequestParam Optional<String> status,
                                                                          @RequestParam Optional<String> statusMatchMode,
                                                                          @RequestParam Optional<String> date,
                                                                          @RequestParam Optional<String> dateMatchMode,
                                                                          @RequestParam Optional<String> action,
                                                                          @RequestParam Optional<String> actionMatchMode) {
        EnumSet<UserRoleEnum> authorizedRoles = EnumSet.of(UserRoleEnum.SUPER_ADMIN, UserRoleEnum.SYSTEM_ADMIN);
        if (!authorizationService.isAuthorized(requestContext, authorizedRoles)) {
            throw new FailureException(HttpResponseEnum.AUTHORIZATION_FAILED);
        }
        return new ResponseEntity<>(assetHistoryService.getAllAssetHistories(pageNo, pageSize, assetId, assetIdMatchMode, modelName, modelNameMatchMode, admin, adminMatchMode, user, userMatchMode, status, statusMatchMode, date, dateMatchMode, action, actionMatchMode), HttpStatus.OK);
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
