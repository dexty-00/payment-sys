package com.PaymentSystem.demo.Service;

import com.PaymentSystem.demo.Entity.CreditBalance;
import com.PaymentSystem.demo.Entity.DTO.CreditSubscriptionRequest;
import com.PaymentSystem.demo.Repository.CreditBalanceRepository;
import com.PaymentSystem.demo.Repository.SubscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;



@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditBalanceRepository creditBalanceRepository;

    @Transactional
    public CreditBalance createCreditSubscription(CreditSubscriptionRequest request) {
        CreditBalance balance = CreditBalance.builder()
                .userId(request.getUserId())
                .balance(request.getInitialBalance())
                .monthlyCost(request.getMonthlyCost())
                .active(true)
                .lastDeductionDate(Instant.now())
                .nextDeductionDate(Instant.now().plusSeconds(30L * 24 * 60 * 60))
                .build();

        return creditBalanceRepository.save(balance);
    }

    @Transactional
    public boolean deductCredits(String userId) {
        Optional<CreditBalance> opt = creditBalanceRepository.findByUserId(userId);
        if (opt.isEmpty()) return false;

        CreditBalance cb = opt.get();
        if (!cb.getActive() || !cb.hasEnoughCredits()) {
            cb.setActive(false);
            creditBalanceRepository.save(cb);
            return false;
        }

        cb.deductMonthlyCost();
        if (cb.getBalance() < cb.getMonthlyCost()) {
            cb.setActive(false);
        }
        creditBalanceRepository.save(cb);

        log.info("Deducted {} credits for user={}, remaining={}",
                cb.getMonthlyCost(), userId, cb.getBalance());
        return true;
    }

    @Transactional
    public CreditBalance topUpCredits(String userId, Integer amount) {
        CreditBalance cb = creditBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No credit balance found"));

        cb.setBalance(cb.getBalance() + amount);
        if (!cb.getActive() && cb.hasEnoughCredits()) {
            cb.setActive(true);
            cb.setNextDeductionDate(Instant.now().plusSeconds(30L * 24 * 60 * 60));
        }

        return creditBalanceRepository.save(cb);
    }

    public boolean isCreditActive(String userId) {
        return creditBalanceRepository.findByUserId(userId)
                .map(CreditBalance::getActive)
                .orElse(false);
    }

    public CreditBalance getBalance(String userId) {
        return creditBalanceRepository.findByUserId(userId).orElse(null);
    }
}