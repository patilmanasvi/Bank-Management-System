package com.bank.model;

import java.math.BigDecimal;

/**
 * CurrentAccount - Concrete subclass of Account.
 * Demonstrates Overdraft support and 0% interest.
 */
public class CurrentAccount extends Account {
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("25000.00");

    public CurrentAccount(String accountNumber, BigDecimal balance, int customerId, int branchId) {
        super(accountNumber, balance, customerId, branchId);
    }

    public CurrentAccount(String accountNumber, BigDecimal balance) {
        super(accountNumber, balance);
    }

    @Override
    public synchronized void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        // Balance can dip into overdraft limit
        BigDecimal availableFunds = getBalance().add(OVERDRAFT_LIMIT);
        if (availableFunds.compareTo(amount) < 0) {
            throw new IllegalStateException("Withdrawal denied: Exceeds overdraft limit of ₹" + OVERDRAFT_LIMIT);
        }
        // Direct deduction within overdraft limit
        deduct(amount);
    }

    @Override
    public BigDecimal calculateInterest() {
        return BigDecimal.ZERO; // Current accounts typically earn 0% interest
    }

    @Override
    public String getAccountType() {
        return "CURRENT";
    }
}
