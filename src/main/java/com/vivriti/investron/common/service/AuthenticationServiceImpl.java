package com.vivriti.investron.common.service;

import com.vivriti.investron.common.dto.UserWithRolesDto;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Override
    public List<UserWithRolesDto> getUsersByRole(String role) {
        return Collections.emptyList();
    }
}
