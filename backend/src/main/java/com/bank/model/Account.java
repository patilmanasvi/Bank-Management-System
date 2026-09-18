package com.bank.model;

import java.math.BigDecimal;

/**
 * Account - Abstract Base Class demonstrating:
 * 1. Encapsulation: Private balance, modified strictly via deposit/withdraw
 * 2. Abstraction: calculateInterest() declared abstract for polymorphism
 */
public abstract class Account {
    protected String accountNumber;
    protected int customerId;
    protected int branchId;
    private BigDecimal balance;
    protected String status;

    public Account(String accountNumber, BigDecimal balance, int customerId, int branchId) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.customerId = customerId;
        this.branchId = branchId;
        this.status = "ACTIVE";
    }

    public Account(String accountNumber, BigDecimal balance) {
        this(accountNumber, balance, 1, 1);
    }

    // Encapsulation: Getter only, no direct balance setter
    public String getNumber() { return accountNumber; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public int getCustomerId() { return customerId; }
    public int getBranchId() { return branchId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Deposit amount into the account.
     */
    public synchronized void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Withdraw amount from the account.
     */
    public synchronized void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds: Available balance is " + this.balance);
        }
        this.balance = this.balance.subtract(amount);
    }

    // Polymorphic method overridden by each specific account type
    public abstract BigDecimal calculateInterest();
    public abstract String getAccountType();
}
