package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccessControlService {

    public void requireSelfOrAdmin(Integer userId) {
        if (isAdmin()) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !String.valueOf(userId).equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该用户资源");
        }
    }

    public void requireParticipantOrAdmin(Integer... userIds) {
        if (isAdmin()) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            for (Integer userId : userIds) {
                if (userId != null && String.valueOf(userId).equals(authentication.getName())) {
                    return;
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该资源");
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
    }

    public boolean hasAdminRole() {
        return isAdmin();
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_Admin".equals(authority.getAuthority()));
    }
}
