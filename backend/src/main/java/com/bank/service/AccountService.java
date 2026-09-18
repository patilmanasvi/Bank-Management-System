package com.bank.service;

import com.bank.model.Account;
import com.bank.model.SavingsAccount;
import com.bank.model.CurrentAccount;
import com.bank.model.FixedDepositAccount;
import com.bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * AccountService - Handles Account operations with MySQL Database persistence
 * and in-memory cache support.
 */
public class AccountService {
    private final Map<String, Account> memoryRegistry = new HashMap<>();

    public void register(Account account) {
        memoryRegistry.put(account.getNumber(), account);
    }

    public Account find(String accountNumber) {
        // First check memory registry
        if (memoryRegistry.containsKey(accountNumber)) {
            return memoryRegistry.get(accountNumber);
        }
        // Then query MySQL database
        return findInDatabase(accountNumber);
    }

    public Account findInDatabase(String accountNumber) {
        String sql = "SELECT a.account_number, a.balance, a.customer_id, a.branch_id, t.type_name " +
                     "FROM account a JOIN account_type t ON a.type_id = t.type_id " +
                     "WHERE a.account_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String num = rs.getString("account_number");
                BigDecimal bal = rs.getBigDecimal("balance");
                int custId = rs.getInt("customer_id");
                int branchId = rs.getInt("branch_id");
                String type = rs.getString("type_name");

                return switch (type) {
                    case "CURRENT" -> new CurrentAccount(num, bal, custId, branchId);
                    case "FIXED_DEPOSIT" -> new FixedDepositAccount(num, bal, custId, branchId);
                    default -> new SavingsAccount(num, bal, custId, branchId);
                };
            }
        } catch (SQLException e) {
            System.err.println("Error finding account in DB: " + e.getMessage());
        }
        return null;
    }

    public void deposit(String accountNumber, BigDecimal amount) {
        Account acc = find(accountNumber);
        if (acc != null) {
            acc.deposit(amount);
        }
        // Update database
        String sql = "UPDATE account SET balance = balance + ? WHERE account_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating deposit in DB: " + e.getMessage());
        }
    }

    public void withdraw(String accountNumber, BigDecimal amount) {
        Account acc = find(accountNumber);
        if (acc != null) {
            acc.withdraw(amount);
        }
        // Update database
        String sql = "UPDATE account SET balance = balance - ? WHERE account_number = ? AND balance >= ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setString(2, accountNumber);
            ps.setBigDecimal(3, amount);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalStateException("Database withdrawal failed: insufficient funds or invalid account.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during withdraw: " + e.getMessage(), e);
        }
    }
}
