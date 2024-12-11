package com.certimeter.asset.service;

import com.certimeter.asset.enumeration.UserRoleEnum;
import com.certimeter.asset.requestcontext.RequestContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;


@Service
public class AuthorizationService {
    public boolean isAuthorized(RequestContext request, EnumSet<UserRoleEnum> authorizedRoles) {
        return authorizedRoles.contains(request.getRole());
    }
}

