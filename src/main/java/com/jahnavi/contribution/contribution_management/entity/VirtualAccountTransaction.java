package com.jahnavi.contribution.contribution_management.entity;

import com.vivriti.investron.common.entity.BaseEntity;
import com.jahnavi.contribution.contribution_management.enums.Classification;
import com.jahnavi.contribution.contribution_management.enums.ClassificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for storing Virtual Account Transaction data
 * Received from bank callbacks (YES Bank, Federal, ICICI, etc.)
 */
@Entity
@Table(name = "virtual_account_transactions", indexes = {
    @Index(name = "idx_utr", columnList = "utr"),
    @Index(name = "idx_va_number", columnList = "virtual_account_number"),
    @Index(name = "idx_fund_name", columnList = "fund_name"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_mis_batch_id", columnList = "mis_batch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "vpay_reference_id")
    private String vPayReferenceId; // Reference ID from VPay

    @Column(name = "virtual_account_number", nullable = false)
    private String virtualAccountNumber;

    @Column(name = "utr", nullable = false)
    private String utr;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "transaction_status")
    private String transactionStatus;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_amount")
    private String transactionAmount;

    @Column(name = "credit_time")
    private LocalDateTime creditTime;

    @Column(name = "remitter_name")
    private String remitterName;

    @Column(name = "remitter_account")
    private String remitterAccount;

    @Column(name = "remitter_ifsc")
    private String remitterIfsc;

    @Column(name = "remitter_account_type")
    private String remitterAccountType;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "fund_name")
    private String fundName;

    @Column(name = "client_code")
    private String clientCode;

    @Column(name = "agent_name")
    private String agentName;

    @Column(name = "client_account")
    private String clientAccount;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "duplicate_reason", length = 500)
    private String duplicateReason;

    @Column(name = "original_transaction_id")
    private Long originalTransactionId; // Link to first transaction if duplicate

    @Column(name = "source")
    private String source; // WEBHOOK, EMAIL

    @Column(name = "mis_batch_id", length = 100)
    private Long misBatchId; // Link to MIS email batch if from email

    @Column(name = "mis_record_id")
    private Long misRecordId; // Link to MIS record

    @Column(name = "processing_status")
    private String processingStatus; // SUCCESS, FAILURE, PENDING

    @Column(name = "acknowledgement_status")
    private String acknowledgementStatus; // SUCCESS, FAILURE

    @Column(name = "error_message", columnDefinition = "LONGTEXT" )
    private String errorMessage;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "customer_sub_code")
    private String customerSubCode;

    @Column(name = "remitter_code")
    private String remitterCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false)
    private Classification classification;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification_status", nullable = false)
    private ClassificationStatus classificationStatus;

    @Column(name="reason")
    private String reason;



}

