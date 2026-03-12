package com.jahnavi.contribution.contribution_management.entity;

import com.jahnavi.contribution.contribution_management.enums.Classification;
import com.jahnavi.contribution.contribution_management.enums.ClassificationStatus;
import com.jahnavi.contribution.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for storing raw VPay callback payloads
 * Stores the complete raw JSON response from VPay before processing
 */
@Entity
@Table(name = "raw_credit_combined", indexes = {
    @Index(name = "idx_vpay_raw_utr", columnList = "utr"),
    @Index(name = "idx_vpay_raw_va_number", columnList = "virtual_account_number"),
    @Index(name = "idx_vpay_raw_received_at", columnList = "received_at"),
    @Index(name = "idx_vpay_raw_transaction_id", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawCreditCombined extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "utr", length = 100)
    private String utr;

    @Column(name = "fund_name")
    private String fundName;

    @Column(name = "virtual_account_number", length = 100)
    private String virtualAccountNumber;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "raw_payload", columnDefinition = "LONGTEXT", nullable = false)
    private String rawPayload;

    @Column(name = "received_at", nullable = false)
    private java.time.LocalDateTime receivedAt;

    @Column(name = "processing_status", length = 50)
    private String processingStatus; // RECEIVED, PROCESSED, FAILED

    @Column(name = "error_message", columnDefinition = "LONGTEXT")
    private String errorMessage;

    @Column(name = "transaction_reference_id", length = 100)
    private String transactionReferenceId; // Link to virtual_account_transactions.reference_id

    @Column(name = "http_headers", columnDefinition = "TEXT")
    private String httpHeaders; // Store HTTP headers for debugging

    @Column(name = "source_ip", length = 50)
    private String sourceIp; // IP address of VPay callback

    @Column(name = "record")
    private String combinedRecordId;

    @Builder.Default
    @Column(name = "is_duplicate")
    private Boolean isDuplicate = Boolean.FALSE;

    @Column(name = "duplicate_reason")
    private String duplicateReason;

    @Column(name = "remitter_name")
    private String remitterName;

    @Column(name = "remitter_account")
    private String remitterAccount;

    @Column(name = "remitter_ifsc")
    private String remitterIfsc;

    @Column(name = "remitter_account_type")
    private String remitterAccountType;

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_amount")
    private String transactionAmount;

    @Column(name = "source")
    private String source; // WEBHOOK, EMAIL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "virtual_account_id",
            referencedColumnName = "id",
            nullable = false
    )
    private VirtualAccount virtualAccount;

    @Column(name = "fund_id")
    private Long fundId;


    @Enumerated(EnumType.STRING)
    @Column(name = "classification")
    private Classification classification;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification_status")
    private ClassificationStatus classificationStatus;

    @Column(name="Reason")
    private String reason;

    @Column(name="mis_batch_id")
    private Long misBatchId;

}


