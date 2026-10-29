package com.healthcare.claims.repository;

import com.healthcare.claims.entity.ClaimRemark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRemarkRepository extends JpaRepository<ClaimRemark, Long> {
    List<ClaimRemark> findByClaimIdOrderByCreatedAtDesc(Long claimId);
}
