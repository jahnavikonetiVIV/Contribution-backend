package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.DdnContributionMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DdnContributionMappingRepository extends JpaRepository<DdnContributionMapping, Long>, 
        JpaSpecificationExecutor<DdnContributionMapping> {

    Optional<DdnContributionMapping> findByUtr(String utr);

    List<DdnContributionMapping> findByFolioAndStatus(String folio, String status);

    List<DdnContributionMapping> findByMappingTypeAndStatus(String mappingType, String status);

    @Query("SELECT m FROM DdnContributionMapping m WHERE m.status = :status " +
           "AND (:folio IS NULL OR m.folio = :folio) " +
           "AND (:fundId IS NULL OR m.fundId = :fundId) " +
           "AND (:fromDate IS NULL OR m.transactionDateTime >= :fromDate) " +
           "AND (:toDate IS NULL OR m.transactionDateTime <= :toDate)")
    Page<DdnContributionMapping> findByFilters(
            @Param("status") String status,
            @Param("folio") String folio,
            @Param("fundId") Long fundId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    boolean existsByUtrAndStatus(String utr, String status);

    /**
     * Find existing mapping by UTR and IFSC with ACTIVE status
     */
    Optional<DdnContributionMapping> findByUtrAndIfscCodeAndStatus(String utr, String ifscCode, String status);

    /**
     * Calculate total amount paid towards a specific DDN across all ACTIVE mappings.
     * A DDN can appear in both initialCommitmentDdnId and topupDdnId columns.
     */
    @Query("SELECT COALESCE(SUM(" +
           "  CASE WHEN m.initialCommitmentDdnId = :ddnId THEN COALESCE(m.initialAmount, 0) ELSE 0 END + " +
           "  CASE WHEN m.topupDdnId = :ddnId THEN COALESCE(m.topupAmount, 0) ELSE 0 END" +
           "), 0) " +
           "FROM DdnContributionMapping m " +
           "WHERE m.status = :status")
    BigDecimal getTotalPaidForDdn(@Param("ddnId") String ddnId, @Param("status") String status);

    /**
     * Calculate total amount already utilized from a specific UTR across all ACTIVE mappings.
     * This supports UTR splitting where one transaction can be mapped to multiple DDNs.
     * Sum includes: initialAmount + topupAmount + excessAmount
     */
    @Query("SELECT COALESCE(SUM(" +
           "  COALESCE(m.initialAmount, 0) + " +
           "  COALESCE(m.topupAmount, 0) + " +
           "  COALESCE(m.excessAmount, 0)" +
           "), 0) " +
           "FROM DdnContributionMapping m " +
           "WHERE m.utr = :utr AND m.status = :status")
    BigDecimal getTotalUtilizedFromUtr(@Param("utr") String utr, @Param("status") String status);
}

