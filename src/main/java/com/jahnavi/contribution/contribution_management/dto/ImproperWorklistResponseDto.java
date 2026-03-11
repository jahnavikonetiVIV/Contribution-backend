package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImproperWorklistResponseDto {
    private String utr;
    private BigDecimal amount;
    private LocalDateTime dateAndTime;
    private String paymentMode;
    private String bankAccount;
    private String ifsc;
    private String va;
    private String reason;
    private String currentState;
}
