package com.jahnavi.contribution.contribution_management.entity;

import com.vivriti.investron.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ddn_contribution_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DdnContributionMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utr", nullable = false, length = 100)
    private String utr;

    @Column(name = "master_va", length = 100)
    private String masterVa;

    @Column(name = "va_account", length = 100)
    private String vaAccount;

    @Column(name = "folio", length = 50)
    private String folio;

    @Column(name = "fund_id")
    private Long fundId;

    @Column(name = "fund_name", length = 255)
    private String fundName;

    @Column(name = "total_transaction_amount", precision = 19, scale = 2)
    private BigDecimal totalTransactionAmount;

    @Column(name = "transaction_datetime")
    private LocalDateTime transactionDateTime;

    @Column(name = "transaction_source", length = 50)
    private String transactionSource; // MIS / Webhook

    @Column(name = "remarks", length = 500)
    private String remarks;

    // Split amounts
    @Column(name = "initial_amount", precision = 19, scale = 2)
    private BigDecimal initialAmount;


    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "initial_commitment_ddn_id", length = 100)
    private String initialCommitmentDdnId;

    @Column(name = "topup_amount", precision = 19, scale = 2)
    private BigDecimal topupAmount;

    @Column(name = "topup_ddn_id", length = 100)
    private String topupDdnId;

    @Column(name = "excess_amount", precision = 19, scale = 2)
    private BigDecimal excessAmount;

    // Mapping metadata
    @Column(name = "mapping_source", length = 255)
    private String mappingSource; // "System" or user email

    @Column(name = "mapping_type", length = 50)
    private String mappingType; // AUTO / MANUAL / BULK

    @Column(name = "status", length = 50)
    private String status; // ACTIVE / INACTIVE / CANCELLED

    @Column(name = "mapped_at")
    private LocalDateTime mappedAt;

    @Column(name = "mapped_by", length = 255)
    private String mappedBy;
}

