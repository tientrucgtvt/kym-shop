package com.kinshop.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionService {

    private final AppUserRepository appUserRepository;
    private final AccessRightRepository accessRightRepository;

    public PermissionService(AppUserRepository appUserRepository, AccessRightRepository accessRightRepository) {
        this.appUserRepository = appUserRepository;
        this.accessRightRepository = accessRightRepository;
    }

    public boolean has(String pageKey, String actionName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return false;
        }
        if ("admin".equals(authentication.getName())) {
            return true;
        }
        Optional<AppUser> user = appUserRepository.findByUsername(authentication.getName());
        if (user.isEmpty() || !user.get().isActive()) {
            return false;
        }
        Optional<AccessRight> right = accessRightRepository.findByAppUserAndPageKey(user.get(), pageKey);
        return right.filter(accessRight -> hasAction(accessRight, PermissionAction.valueOf(actionName))).isPresent();
    }

    public void require(String pageKey, String actionName) {
        if (!has(pageKey, actionName)) {
            throw new AccessDeniedException("You do not have permission for this action.");
        }
    }

    public String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return "";
        }
        return authentication.getName();
    }

    private boolean hasAction(AccessRight right, PermissionAction action) {
        switch (action) {
            case VIEW:
                return right.isCanView();
            case CREATE:
                return right.isCanCreate();
            case UPDATE:
                return right.isCanUpdate();
            case DELETE:
                return right.isCanDelete();
            case CANCEL:
                return right.isCanCancel();
            case RETURN:
                return right.isCanReturn();
            case STOCK:
                return right.isCanStock();
            default:
                return false;
        }
    }
}
