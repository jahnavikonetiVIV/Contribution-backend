package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.InvestorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvestorDataRepository extends JpaRepository<InvestorData, Long> {

    Optional<InvestorData> findByVirtualAccountNumberAndActiveTrue(String virtualAccountNumber);

    Optional<InvestorData> findByBankAccountNumberAndIfscCodeAndActiveTrue(String bankAccountNumber, String ifscCode);

    boolean existsByVirtualAccountNumberAndActiveTrue(String virtualAccountNumber);
}
