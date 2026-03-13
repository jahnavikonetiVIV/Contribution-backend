package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.FundRequestContributionMapping;
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
public interface FundRequestContributionMappingRepository extends JpaRepository<FundRequestContributionMapping, Long>,
        JpaSpecificationExecutor<FundRequestContributionMapping> {

    Optional<FundRequestContributionMapping> findByUtr(String utr);

    List<FundRequestContributionMapping> findByFolioAndStatus(String folio, String status);

    List<FundRequestContributionMapping> findByMappingTypeAndStatus(String mappingType, String status);

    @Query("SELECT m FROM FundRequestContributionMapping m WHERE m.status = :status " +
           "AND (:folio IS NULL OR m.folio = :folio) " +
           "AND (:fundId IS NULL OR m.fundId = :fundId) " +
           "AND (:fromDate IS NULL OR m.transactionDateTime >= :fromDate) " +
           "AND (:toDate IS NULL OR m.transactionDateTime <= :toDate)")
    Page<FundRequestContributionMapping> findByFilters(
            @Param("status") String status,
            @Param("folio") String folio,
            @Param("fundId") Long fundId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    boolean existsByUtrAndStatus(String utr, String status);

    Optional<FundRequestContributionMapping> findByUtrAndIfscCodeAndStatus(String utr, String ifscCode, String status);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM FundRequestContributionMapping m " +
           "WHERE m.utr = :utr AND SUBSTRING(m.ifscCode, 1, 4) = :ifscPrefix AND m.status = :status")
    boolean existsByUtrAndIfscPrefixAndStatus(@Param("utr") String utr, @Param("ifscPrefix") String ifscPrefix, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(" +
           "  CASE WHEN m.initialCommitmentFundRequestId = :fundRequestId THEN COALESCE(m.initialAmount, 0) ELSE 0 END + " +
           "  CASE WHEN m.topupFundRequestId = :fundRequestId THEN COALESCE(m.topupAmount, 0) ELSE 0 END" +
           "), 0) " +
           "FROM FundRequestContributionMapping m " +
           "WHERE m.status = :status")
    BigDecimal getTotalPaidForFundRequest(@Param("fundRequestId") String fundRequestId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(" +
           "  COALESCE(m.initialAmount, 0) + " +
           "  COALESCE(m.topupAmount, 0) + " +
           "  COALESCE(m.excessAmount, 0)" +
           "), 0) " +
           "FROM FundRequestContributionMapping m " +
           "WHERE m.utr = :utr AND m.status = :status")
    BigDecimal getTotalUtilizedFromUtr(@Param("utr") String utr, @Param("status") String status);
}
