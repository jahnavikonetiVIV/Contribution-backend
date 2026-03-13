package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.FundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundRequestRepository extends JpaRepository<FundRequest, Long> {

    Optional<FundRequest> findByFundRequestCodeAndActiveTrue(String fundRequestCode);

    List<FundRequest> findByInvestor_FolioAndStatusAndActiveTrue(String folio, String status);

    @Query("SELECT f FROM FundRequest f JOIN FETCH f.investor i WHERE i.folio = :folio AND f.status = :status AND f.active = true")
    List<FundRequest> findByInvestorFolioAndStatusAndActiveTrueWithInvestor(@Param("folio") String folio, @Param("status") String status);
}
