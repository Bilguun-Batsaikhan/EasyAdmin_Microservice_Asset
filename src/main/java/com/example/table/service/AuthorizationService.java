package com.example.table.service;

import com.example.table.enumeration.UserRoleEnum;
import com.example.table.requestcontext.RequestContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;


@Service
public class AuthorizationService {
    public boolean isAuthorized(RequestContext request, EnumSet<UserRoleEnum> authorizedRoles) {
        return authorizedRoles.contains(request.getRole());
    }
}

