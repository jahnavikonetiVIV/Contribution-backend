package com.jahnavi.contribution.contribution_management.entity;

import com.jahnavi.contribution.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Fund Request table for contribution mapping.
 * Contains fund name, investor reference, total payable amount, and status.
 * Used for Fund Request contribution mapping to match transactions with fund requests.
 */
@Entity
@Table(name = "fund_request", indexes = {
    @Index(name = "idx_fund_request_investor_id", columnList = "investor_id"),
    @Index(name = "idx_fund_request_fund_name", columnList = "fund_name"),
    @Index(name = "idx_fund_request_status", columnList = "status"),
    @Index(name = "idx_fund_request_fund_request_code", columnList = "fund_request_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fund_request_code", nullable = false, unique = true, length = 100)
    private String fundRequestCode;

    @Column(name = "fund_name", nullable = false, length = 255)
    private String fundName;

    @Column(name = "fund_id")
    private Long fundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", referencedColumnName = "id")
    private InvestorData investor;

    @Column(name = "total_payable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPayableAmount;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // OPEN or CLOSE

    @Column(name = "commitment_type", length = 50)
    private String commitmentType; // Initial, Top-up (for mapping type distinction)
}
