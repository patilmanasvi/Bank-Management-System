package com.bank.model;

import java.math.BigDecimal;

/**
 * SavingsAccount - Concrete subclass of Account.
 * Demonstrates Polymorphism in calculateInterest() and custom withdrawal rules.
 */
public class SavingsAccount extends Account {
    private static final BigDecimal MIN_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("0.04"); // 4%

    public SavingsAccount(String accountNumber, BigDecimal balance, int customerId, int branchId) {
        super(accountNumber, balance, customerId, branchId);
    }

    public SavingsAccount(String accountNumber, BigDecimal balance) {
        super(accountNumber, balance);
    }

    @Override
    public synchronized void withdraw(BigDecimal amount) {
        if (getBalance().subtract(amount).compareTo(MIN_BALANCE) < 0) {
            throw new IllegalStateException("Withdrawal denied: Must maintain minimum balance of ₹" + MIN_BALANCE);
        }
        super.withdraw(amount);
    }

    @Override
    public BigDecimal calculateInterest() {
        // Simple annual interest = balance * 4%
        return getBalance().multiply(ANNUAL_INTEREST_RATE);
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }
}
