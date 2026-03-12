package com.jahnavi.contribution.contribution_management.entity;

import com.jahnavi.contribution.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "virtual_account", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"va_number"}),
        @UniqueConstraint(columnNames = {"folio_number", "fund_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folio_number", nullable = false, length = 50)
    private String folioNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", referencedColumnName = "id")
    private InvestorData investor;
    @Column(name = "fund_id", nullable = false)
    private Long fundId;

    @Column(name = "fund_name", nullable = false, length = 255)
    private String fundName;

    @Column(name = "va_prefix", nullable = false, length = 50)
    private String vaPrefix;

    @Column(name = "va_number", nullable = false, unique = true, length = 100)
    private String vaNumber;

    @Column(name = "bank_partner", nullable = false, length = 100)
    private String bankPartner;

    @Column(name = "status", nullable = false, length = 50)
    private String status;
}

