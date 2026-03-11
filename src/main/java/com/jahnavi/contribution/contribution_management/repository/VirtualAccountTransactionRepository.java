package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.VirtualAccountTransaction;
import com.jahnavi.contribution.contribution_management.enums.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualAccountTransactionRepository extends JpaRepository<VirtualAccountTransaction, Long> {

    Optional<VirtualAccountTransaction> findByUtr(String utr);

    boolean existsByUtr(String utr);

    List<VirtualAccountTransaction> findByVirtualAccountNumber(String virtualAccountNumber);

    List<VirtualAccountTransaction> findByFundName(String fundName);

    @Query("SELECT t FROM VirtualAccountTransaction t WHERE t.fundName = :fundName AND t.transactionDate BETWEEN :fromDate AND :toDate")
    List<VirtualAccountTransaction> findByFundNameAndDateRange(
            @Param("fundName") String fundName,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("SELECT t FROM VirtualAccountTransaction t ORDER BY t.transactionDate DESC")
    List<VirtualAccountTransaction> findAllWithFolioInfo();

    @Query("SELECT t FROM VirtualAccountTransaction t WHERE t.classification = :classification ORDER BY t.transactionDate DESC")
    List<VirtualAccountTransaction> findImproperTransactionsWithFolioInfo(@Param("classification") Classification classification);

    @Query("SELECT t FROM VirtualAccountTransaction t WHERE t.utr = :utr AND (t.remitterIfsc IS NOT NULL AND SUBSTRING(t.remitterIfsc, 1, 4) = :ifscPrefix)")
    Optional<VirtualAccountTransaction> findByUtrAndIfscPrefix(
            @Param("utr") String utr,
            @Param("ifscPrefix") String ifscPrefix
    );

    @Query("SELECT t FROM VirtualAccountTransaction t WHERE t.utr = :utr AND t.remitterIfsc = :ifscCode")
    Optional<VirtualAccountTransaction> findByUtrAndRemitterIfsc(
            @Param("utr") String utr,
            @Param("ifscCode") String ifscCode
    );

    @Query("SELECT DISTINCT t FROM VirtualAccountTransaction t LEFT JOIN VirtualAccount va ON t.virtualAccountNumber = va.vaNumber WHERE (:classification IS NULL OR t.classification = :classification) AND (:startDate IS NULL OR t.transactionDate >= :startDate) AND (:endDate IS NULL OR t.transactionDate <= :endDate) AND (:vaPrefix IS NULL OR va.vaPrefix = :vaPrefix) ORDER BY t.id DESC")
    List<VirtualAccountTransaction> findTransactionsWithFilters(
            @Param("classification") Classification classification,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("vaPrefix") String vaPrefix
    );

    List<VirtualAccountTransaction> findByMisBatchId(Long misBatchId);

    @Modifying
    @Query("DELETE FROM VirtualAccountTransaction t WHERE t.misBatchId = :misBatchId")
    int deleteByMisBatchId(@Param("misBatchId") Long misBatchId);
}
