package com.uepb.PaymentService.repository;

import com.uepb.PaymentService.domain.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, String> {

    Optional<Billing> findByAbacatePayId(String abacatePayId);
}
