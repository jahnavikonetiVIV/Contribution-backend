package com.jahnavi.contribution.contribution_management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Internal DTO for Virtual Account Transaction Notification
 * Normalized structure used across all bank integrations (YES Bank, Federal, ICICI, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class NotifyVirtualAccountTransactionRequest {

    private String referenceId;
    private String virtualAccountNumber;
    private String institutionCode;
    private String agentCode;
    private String agentName;
    private String clientCode;
    private String subCode;
    private String remitterCode;
    private String validationRemarks;
    private String clientAccount;
    private String agentDetails1;
    private String agentDetails2;

    // Transaction info
    private String transactionId;
    private String transactionStatus;
    private String transactionDate;
    private String transactionType;
    private String transactionAmount;
    private String utrRrn;
    private String creditTime;
    private List<String> transactionParticulars;

    // Remitter info
    private String remitterName;
    private String remitterAccount;
    private String remitterIFSC;
    private String remitterAccountType;
    private String remitterVPA;
    private String remitterMobile;
    private String remitterMMID;
    private List<Object> remitterInfo;
}




