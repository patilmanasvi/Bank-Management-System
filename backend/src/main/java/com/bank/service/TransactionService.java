package com.bank.service;

import com.bank.model.Account;
import com.bank.model.Transaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very small in‑memory transaction history manager.
 * In a real system this would be persisted in a DB with proper ACID handling.
 */
public class TransactionService {
    private final Map<String, List<Transaction>> history = new HashMap<>();

    public void record(Account account, Transaction txn) {
        history.computeIfAbsent(account.getNumber(), k -> new ArrayList<>()).add(txn);
    }

    public List<Transaction> getHistory(String accountNumber) {
        return history.getOrDefault(accountNumber, new ArrayList<>());
    }
}
