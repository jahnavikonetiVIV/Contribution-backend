package com.jahnavi.contribution.contribution_management.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EcollectTransactionDto {
    
    private String customerCode;
    private String transferUniqueNo;
    private String transferType;
    private Integer attemptNo;
    private String beneAccountNo;
    private String beneAccountIfsc;
    private String beneFullName;
    private String rmtrAccountNo;
    private String rmtrAccountIfsc;
    private String rmtrAccountType;
    private String rmtrFullName;
    private String rmtrAddress;
    private String rmtrToBeneNote;
    private LocalDateTime transferTimestamp;
    private String transferCcy;
    private String transferAmt;
    private String status;
    private String creditAcctNo;
    private LocalDateTime creditedAt;
    private LocalDateTime returnedAt;
    private String apiType;
    private String apiDecision;
    private String apiResult;
    private String rejectReason;
    private String rawPayload;
    private String virtualAccountNo;
    private String duplicate;
}

