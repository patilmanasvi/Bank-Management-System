package com.bank.service;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TransactionService - Handles financial transactions.
 * Demonstrates ACID properties (Atomicity, Consistency, Isolation, Durability)
 * through JDBC explicit transaction management (commit & rollback).
 */
public class TransactionService {
    private final Map<String, List<Transaction>> history = new HashMap<>();

    public void record(Account account, Transaction txn) {
        history.computeIfAbsent(account.getNumber(), k -> new ArrayList<>()).add(txn);
        // Also persist to MySQL database
        String sql = "INSERT INTO transaction (account_number, txn_type, amount, resulting_balance, description) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, account.getNumber());
            ps.setString(2, txn.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? "DEPOSIT" : "WITHDRAW");
            ps.setBigDecimal(3, txn.getAmount().abs());
            ps.setBigDecimal(4, txn.getResultingBalance());
            ps.setString(5, txn.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database audit logging note: " + e.getMessage());
        }
    }

    public List<Transaction> getHistory(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT txn_id, account_number, txn_type, amount, resulting_balance, " +
                     "target_account, description, txn_date FROM transaction " +
                     "WHERE account_number = ? OR target_account = ? ORDER BY txn_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, accountNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("txn_date");
                LocalDate d = ts != null ? ts.toLocalDateTime().toLocalDate() : LocalDate.now();
                BigDecimal amt = rs.getBigDecimal("amount");
                String type = rs.getString("txn_type");
                if ("WITHDRAW".equalsIgnoreCase(type) || ("TRANSFER".equalsIgnoreCase(type) && accountNumber.equals(rs.getString("account_number")))) {
                    amt = amt.negate();
                }
                list.add(new Transaction(d, rs.getString("description"), amt, rs.getBigDecimal("resulting_balance")));
            }
        } catch (SQLException e) {
            System.err.println("Fallback to memory history: " + e.getMessage());
            return history.getOrDefault(accountNumber, new ArrayList<>());
        }
        return list.isEmpty() ? history.getOrDefault(accountNumber, new ArrayList<>()) : list;
    }

    /**
     * ACID FUND TRANSFER:
     * Debits source account and credits destination account within a single atomic transaction.
     * If either operation fails, changes are completely rolled back.
     */
    public boolean transferFunds(String fromAccount, String toAccount, BigDecimal amount, String remarks) throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be strictly positive");
        }
        if (fromAccount.equals(toAccount)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be identical");
        }

        String debitSql = "UPDATE account SET balance = balance - ? WHERE account_number = ? AND balance >= ?";
        String creditSql = "UPDATE account SET balance = balance + ? WHERE account_number = ? AND status = 'ACTIVE'";
        String getBalSql = "SELECT balance FROM account WHERE account_number = ?";
        String insertTxnSql = "INSERT INTO transaction (account_number, txn_type, amount, resulting_balance, target_account, description) VALUES (?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            // 1. Begin ACID Transaction
            con.setAutoCommit(false);

            // 2. Debit Sender
            try (PreparedStatement psDebit = con.prepareStatement(debitSql)) {
                psDebit.setBigDecimal(1, amount);
                psDebit.setString(2, fromAccount);
                psDebit.setBigDecimal(3, amount);
                int rows = psDebit.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Debit failed: Insufficient funds or invalid source account.");
                }
            }

            // 3. Credit Receiver
            try (PreparedStatement psCredit = con.prepareStatement(creditSql)) {
                psCredit.setBigDecimal(1, amount);
                psCredit.setString(2, toAccount);
                int rows = psCredit.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Credit failed: Destination account not found or not ACTIVE.");
                }
            }

            // 4. Retrieve new balance for audit trail
            BigDecimal fromNewBal = BigDecimal.ZERO;
            try (PreparedStatement psBal = con.prepareStatement(getBalSql)) {
                psBal.setString(1, fromAccount);
                ResultSet rs = psBal.executeQuery();
                if (rs.next()) fromNewBal = rs.getBigDecimal("balance");
            }

            BigDecimal toNewBal = BigDecimal.ZERO;
            try (PreparedStatement psBal = con.prepareStatement(getBalSql)) {
                psBal.setString(1, toAccount);
                ResultSet rs = psBal.executeQuery();
                if (rs.next()) toNewBal = rs.getBigDecimal("balance");
            }

            // 5. Insert Transaction Log for Sender
            try (PreparedStatement psTxn = con.prepareStatement(insertTxnSql)) {
                psTxn.setString(1, fromAccount);
                psTxn.setString(2, "TRANSFER");
                psTxn.setBigDecimal(3, amount);
                psTxn.setBigDecimal(4, fromNewBal);
                psTxn.setString(5, toAccount);
                psTxn.setString(6, "Transfer to " + toAccount + ": " + remarks);
                psTxn.executeUpdate();
            }

            // 6. Insert Transaction Log for Receiver
            try (PreparedStatement psTxn = con.prepareStatement(insertTxnSql)) {
                psTxn.setString(1, toAccount);
                psTxn.setString(2, "DEPOSIT");
                psTxn.setBigDecimal(3, amount);
                psTxn.setBigDecimal(4, toNewBal);
                psTxn.setString(5, fromAccount);
                psTxn.setString(6, "Transfer from " + fromAccount + ": " + remarks);
                psTxn.executeUpdate();
            }

            // 7. Commit ACID Transaction
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    // Rollback on any failure to preserve consistency
                    con.rollback();
                    System.err.println("Transaction rolled back successfully due to: " + e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
