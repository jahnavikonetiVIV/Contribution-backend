package com.jahnavi.contribution.contribution_management.entity;

import com.jahnavi.contribution.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User/Investor data table containing bank and virtual account details.
 * Used for:
 * - Bank account verification for proper/improper classification
 * - Virtual account number verification
 */
@Entity
@Table(name = "investor_data", indexes = {
    @Index(name = "idx_investor_data_va_number", columnList = "virtual_account_number"),
    @Index(name = "idx_investor_data_bank_account", columnList = "bank_account_number"),
    @Index(name = "idx_investor_data_ifsc", columnList = "ifsc_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestorData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investor_name", nullable = false, length = 255)
    private String investorName;

    @Column(name = "virtual_account_number", nullable = false, unique = true, length = 100)
    private String virtualAccountNumber;

    @Column(name = "bank_account_number", nullable = false, length = 50)
    private String bankAccountNumber;

    @Column(name = "ifsc_code", nullable = false, length = 20)
    private String ifscCode;

    @Column(name = "folio", nullable = false, length = 20)
    private String folio;

    @Column(name = "email_id", length = 255)
    private String emailId;

}
