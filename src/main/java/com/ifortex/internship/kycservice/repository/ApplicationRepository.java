package com.ifortex.internship.kycservice.repository;

import com.ifortex.internship.kycservice.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a FROM Application a WHERE a.accountId = :accountId AND a.status IN (" +
           "com.ifortex.internship.kycservice.model.constant.KycStatus.PENDING, " +
           "com.ifortex.internship.kycservice.model.constant.KycStatus.APPROVED)")
    List<Application> findByAccountIdAndStatusPendingOrApproved(@Param("accountId") UUID accountId);

    Optional<Application> findByApplicationId(UUID applicationId);
}
