package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction - Financial movement record.
 */
public class Transaction {
    private int txnId;
    private String accountNumber;
    private String txnType;
    private BigDecimal amount;
    private BigDecimal resultingBalance;
    private String targetAccount;
    private String description;
    private LocalDate date;
    private LocalDateTime timestamp;

    public Transaction(LocalDate date, String description, BigDecimal amount, BigDecimal resultingBalance) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.resultingBalance = resultingBalance;
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(int txnId, String accountNumber, String txnType, BigDecimal amount,
                       BigDecimal resultingBalance, String targetAccount, String description, LocalDateTime timestamp) {
        this.txnId = txnId;
        this.accountNumber = accountNumber;
        this.txnType = txnType;
        this.amount = amount;
        this.resultingBalance = resultingBalance;
        this.targetAccount = targetAccount;
        this.description = description;
        this.timestamp = timestamp;
        this.date = timestamp != null ? timestamp.toLocalDate() : LocalDate.now();
    }

    public int getTxnId() { return txnId; }
    public String getAccountNumber() { return accountNumber; }
    public String getTxnType() { return txnType; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getResultingBalance() { return resultingBalance; }
    public String getTargetAccount() { return targetAccount; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date != null ? date : (timestamp != null ? timestamp.toLocalDate() : LocalDate.now()); }
    public LocalDateTime getTimestamp() { return timestamp; }
}
