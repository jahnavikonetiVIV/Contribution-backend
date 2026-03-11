package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Combined DTO for MIS and Webhook records download
 * Shows all records with duplicate marking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombinedMisReportDto {

    private Long recordNumber;
    private String utrFromBank;
    private Boolean isDuplicate;
    private String bankAccountNumber;
    private String ifsc;
    private String investorReference;
    private LocalDateTime dateTime;
    private String fundName;
    private String paymentMode;
    private String source;
    private String remitterName;
    private BigDecimal amount;
}




