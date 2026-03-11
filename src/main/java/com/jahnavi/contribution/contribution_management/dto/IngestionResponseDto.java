package com.jahnavi.contribution.contribution_management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngestionResponseDto {

    private String utr;
    private String virtualAccountNumber;
    private String investorReference;
    private String fundName;
    private String bankAccount;
    private String ifscCode;
    private String amount;
    private String paymentMode;
    private Boolean duplicate;
    private String source;
    private Long recordId;
    private String recievedAt;
}


