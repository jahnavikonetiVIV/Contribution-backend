package com.vivriti.investron.common.service;

import com.vivriti.investron.common.dto.UserWithRolesDto;

import java.util.Collections;
import java.util.List;

/**
 * Stub implementation for user/role lookup.
 * Replace with actual implementation when auth service is configured.
 */
public interface AuthenticationService {

    default List<UserWithRolesDto> getUsersByRole(String role) {
        return Collections.emptyList();
    }
}
