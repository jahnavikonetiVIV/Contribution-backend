package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    Optional<VirtualAccount> findByVaNumber(String vaNumber);

    @Query("SELECT v FROM VirtualAccount v LEFT JOIN FETCH v.investor WHERE v.vaNumber = :vaNumber")
    Optional<VirtualAccount> findByVaNumberWithInvestor(@Param("vaNumber") String vaNumber);

    Optional<VirtualAccount> findByVaNumberAndActiveTrue(String vaNumber);

    Optional<VirtualAccount> findByFolioNumberAndFundIdAndActiveTrue(String folioNumber, Long fundId);

    List<VirtualAccount> findByFolioNumberAndActiveTrue(String folioNumber);

    @Query("SELECT v FROM VirtualAccount v WHERE v.folioNumber = :folioNumber AND v.status = 'Active' AND v.active = true")
    Optional<VirtualAccount> findActiveVaByFolio(@Param("folioNumber") String folioNumber);

    @Query("SELECT v FROM VirtualAccount v WHERE v.fundId = :fundId AND v.status = 'Active' AND v.active = true")
    List<VirtualAccount> findActiveVasByFundId(@Param("fundId") Long fundId);

    @Query("SELECT v FROM VirtualAccount v WHERE v.status = 'Active' AND v.active = true ORDER BY v.fundName, v.folioNumber")
    List<VirtualAccount> findAllActiveVirtualAccounts();

    boolean existsByVaNumberAndActiveTrue(String vaNumber);
}

