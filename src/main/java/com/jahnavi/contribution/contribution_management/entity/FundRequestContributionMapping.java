package com.jahnavi.contribution.contribution_management.entity;

import com.jahnavi.contribution.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_request_contribution_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRequestContributionMapping extends BaseEntity {

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

    @Column(name = "initial_amount", precision = 19, scale = 2)
    private BigDecimal initialAmount;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "initial_commitment_fund_request_id", length = 100)
    private String initialCommitmentFundRequestId;

    @Column(name = "topup_amount", precision = 19, scale = 2)
    private BigDecimal topupAmount;

    @Column(name = "topup_fund_request_id", length = 100)
    private String topupFundRequestId;

    @Column(name = "excess_amount", precision = 19, scale = 2)
    private BigDecimal excessAmount;

    @Column(name = "mapping_source", length = 255)
    private String mappingSource;

    @Column(name = "mapping_type", length = 50)
    private String mappingType; // AUTO / MANUAL / BULK

    @Column(name = "status", length = 50)
    private String status; // ACTIVE / INACTIVE / CANCELLED

    @Column(name = "mapped_at")
    private LocalDateTime mappedAt;

    @Column(name = "mapped_by", length = 255)
    private String mappedBy;
}
