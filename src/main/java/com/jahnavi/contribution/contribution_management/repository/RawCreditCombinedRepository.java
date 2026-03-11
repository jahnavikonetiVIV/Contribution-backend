package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.RawCreditCombined;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RawCreditCombinedRepository extends JpaRepository<RawCreditCombined, Long> {

    Optional<RawCreditCombined> findByUtr(String utr);

    List<RawCreditCombined> findByVirtualAccountNumber(String virtualAccountNumber);

    Optional<RawCreditCombined> findByTransactionReferenceId(String transactionReferenceId);

    List<RawCreditCombined> findByProcessingStatus(String processingStatus);

    boolean existsByUtrAndFundId(String utr, Long fundId);
    @Query("""
    SELECT COUNT(r) > 0
    FROM RawCreditCombined r
    WHERE r.utr = :utr
      AND SUBSTRING(r.remitterIfsc, 1, 4) = :ifscPrefix
""")
    boolean existsByUtrAndIfscPrefix(
            @Param("utr") String utr,
            @Param("ifscPrefix") String ifscPrefix
    );

    /**
     * Find RawCreditCombined by UTR and first 4 digits of IFSC
     */
    @Query("""
    SELECT r
    FROM RawCreditCombined r
    WHERE r.utr = :utr
      AND r.isDuplicate = false
      AND r.remitterIfsc IS NOT NULL
      AND SUBSTRING(r.remitterIfsc, 1, 4) = :ifscPrefix
""")
    Optional<RawCreditCombined> findByUtrAndIfscPrefix(
            @Param("utr") String utr,
            @Param("ifscPrefix") String ifscPrefix
    );


    Optional<RawCreditCombined> findFirstByUtrOrderByReceivedAtAsc(String utr);

    @Modifying
    @Query("DELETE FROM RawCreditCombined r WHERE r.misBatchId = :misBatchId")
    int deleteByMisBatchId(@Param("misBatchId") Long misBatchId);


}



