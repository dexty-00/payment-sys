package com.PaymentSystem.demo.Service;

import com.PaymentSystem.demo.Entity.CreditBalance;
import com.PaymentSystem.demo.Entity.SubscriptionRecord;
import com.PaymentSystem.demo.Entity.User;
import com.PaymentSystem.demo.Repository.CreditBalanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditBalanceRepository creditBalanceRepository;
    private final StripeService stripeService;

    @Transactional
    public CreditBalance createCreditSubscription(User user, Integer initialBalance, Integer monthlyCost) {
        CreditBalance balance = CreditBalance.builder()
                .user(user)
                .balance(initialBalance)
                .monthlyCost(monthlyCost)
                .active(true)
                .lastDeductionDate(Instant.now())
                .nextDeductionDate(Instant.now().plusSeconds(30L * 24 * 60 * 60))
                .build();

        return creditBalanceRepository.save(balance);
    }

    public CreditBalance changeToCreditSubscription(User user, Integer initialBalance, Integer monthlyCost, Long nextInvoiceDate){
        // deduction date = Stripe Renewal Date

        return CreditBalance.builder()
                .user(user)
                .balance(initialBalance)
                .monthlyCost(monthlyCost)
                .active(true)
                .lastDeductionDate(null)
                .nextDeductionDate(Instant.ofEpochSecond(nextInvoiceDate))
                .build();
    }

    @Transactional
    public boolean deductCredits(Long userId) {
        Optional<CreditBalance> opt = creditBalanceRepository.findByUser_Id(userId);
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
        return true;
    }

    @Transactional
    public CreditBalance topUpCredits(Long userId, Integer amount) {
        CreditBalance cb = creditBalanceRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("No credit balance found"));

        cb.setBalance(cb.getBalance() + amount);
        if (!cb.getActive() && cb.hasEnoughCredits()) {
            cb.setActive(true);
            cb.setNextDeductionDate(Instant.now().plusSeconds(30L * 24 * 60 * 60));
        }

        return creditBalanceRepository.save(cb);
    }

    public boolean isCreditActive(Long userId) {
        return creditBalanceRepository.findByUser_Id(userId)
                .map(CreditBalance::getActive)
                .orElse(false);
    }

    public CreditBalance getBalance(Long userId) {
        return creditBalanceRepository.findByUser_Id(userId).orElse(null);
    }
}