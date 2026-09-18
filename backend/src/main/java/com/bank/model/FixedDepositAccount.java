package com.bank.model;

import java.math.BigDecimal;

/**
 * FixedDepositAccount - Concrete subclass of Account.
 * Demonstrates 7.5% interest and lock-in withdrawal restrictions.
 */
public class FixedDepositAccount extends Account {
    private static final BigDecimal FD_INTEREST_RATE = new BigDecimal("0.075"); // 7.5%

    public FixedDepositAccount(String accountNumber, BigDecimal balance, int customerId, int branchId) {
        super(accountNumber, balance, customerId, branchId);
    }

    public FixedDepositAccount(String accountNumber, BigDecimal balance) {
        super(accountNumber, balance);
    }

    @Override
    public synchronized void withdraw(BigDecimal amount) {
        throw new UnsupportedOperationException("Premature withdrawal on Fixed Deposit requires branch manager approval.");
    }

    @Override
    public BigDecimal calculateInterest() {
        return getBalance().multiply(FD_INTEREST_RATE);
    }

    @Override
    public String getAccountType() {
        return "FIXED_DEPOSIT";
    }
}
