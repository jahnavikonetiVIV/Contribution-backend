package com.vivriti.investron.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithRolesDto {
    private String email;
    private List<String> roles;

    public String getEmail() {
        return email;
    }
}
