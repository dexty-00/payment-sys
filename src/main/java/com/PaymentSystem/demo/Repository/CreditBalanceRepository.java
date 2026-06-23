package com.PaymentSystem.demo.Repository;

import com.PaymentSystem.demo.Entity.CreditBalance;
import com.PaymentSystem.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditBalanceRepository extends JpaRepository<CreditBalance, Long> {
    Optional<CreditBalance> findByUser(User user);
    Optional<CreditBalance> findByUser_Id(Long userId);
}