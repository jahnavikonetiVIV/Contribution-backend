package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.Ddn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DdnRepository extends JpaRepository<Ddn, Long> {

    Optional<Ddn> findByDdnCodeAndActiveTrue(String ddnCode);

    List<Ddn> findByInvestor_FolioAndStatusAndActiveTrue(String folio, String status);

    @Query("SELECT d FROM Ddn d JOIN FETCH d.investor i WHERE i.folio = :folio AND d.status = :status AND d.active = true")
    List<Ddn> findByInvestorFolioAndStatusAndActiveTrueWithInvestor(@Param("folio") String folio, @Param("status") String status);
}
