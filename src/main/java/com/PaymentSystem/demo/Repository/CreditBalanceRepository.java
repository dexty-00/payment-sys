package com.PaymentSystem.demo.Repository;

import com.PaymentSystem.demo.Entity.CreditBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditBalanceRepository extends JpaRepository<CreditBalance, Long> {
    Optional<CreditBalance> findByUserId(String userId);
}