package com.jahnavi.contribution.contribution_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ecollect_raw_txn",
        indexes = {
                @Index(name = "idx_customer_code", columnList = "customer_code"),
                @Index(name = "idx_transfer_unique_no", columnList = "transfer_unique_no"),
                @Index(name = "idx_transfer_timestamp", columnList = "transfer_timestamp"),
                @Index(name = "idx_received_at", columnList = "received_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcollectRawTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Core Identifiers
    @Column(name = "customer_code", nullable = false, length = 15)
    private String customerCode;

    @Column(name = "transfer_unique_no", nullable = false, length = 64)
    private String transferUniqueNo; // UTR / reference number

    @Column(name = "transfer_type", nullable = false, length = 10)
    private String transferType; // NEFT / RTGS / IMPS / UPI / FT

    @Column(name = "attempt_no")
    private Integer attemptNo;

    // Beneficiary (your company)
    @Column(name = "bene_account_no", nullable = false, length = 64)
    private String beneAccountNo;

    @Column(name = "bene_account_ifsc", nullable = false, length = 20)
    private String beneAccountIfsc;

    @Column(name = "bene_full_name", length = 255)
    private String beneFullName;

    // Remitter (payer)
    @Column(name = "rmtr_account_no", nullable = false, length = 64)
    private String rmtrAccountNo;

    @Column(name = "rmtr_account_ifsc", nullable = false, length = 20)
    private String rmtrAccountIfsc;

    @Column(name = "rmtr_account_type", length = 10)
    private String rmtrAccountType;

    @Column(name = "rmtr_full_name", length = 255)
    private String rmtrFullName;

    @Column(name = "rmtr_address", length = 255)
    private String rmtrAddress;

    @Column(name = "rmtr_to_bene_note", length = 255)
    private String rmtrToBeneNote;

    // Transaction details
    @Column(name = "transfer_timestamp", nullable = false)
    private LocalDateTime transferTimestamp;

    @Column(name = "transfer_ccy", nullable = false, length = 5)
    private String transferCcy;

    @Column(name = "transfer_amt", nullable = false, precision = 18, scale = 2)
    private BigDecimal transferAmt;

    @Column(name = "status", length = 20)
    private String status; // NEW / CREDITED / RETURNED

    // Credit / Return info
    @Column(name = "credit_acct_no", length = 25)
    private String creditAcctNo;

    @Column(name = "credited_at")
    private LocalDateTime creditedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    // API response info
    @Column(name = "api_type", nullable = false, length = 10)
    private String apiType; // VALIDATE / NOTIFY

    @Column(name = "api_decision", length = 10)
    private String apiDecision; // pass / reject / pending

    @Column(name = "api_result", length = 10)
    private String apiResult; // ok / retry

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "raw_payload", columnDefinition = "JSON")
    private String rawPayload; // full JSON/XML payload stored as JSON string

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
}

