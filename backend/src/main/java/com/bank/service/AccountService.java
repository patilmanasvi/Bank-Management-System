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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * AccountService - Handles Account operations with MySQL Database persistence.
 *
 * CHANGES from original:
 *  - deposit()/withdraw() now insert a row into `transaction` (previously only
 *    the Swing UI did this; deposits/withdrawals made through the web API were
 *    invisible in transaction history and had no audit trail).
 *  - deposit()/withdraw() now check account status is 'ACTIVE' first, so a
 *    frozen/closed account can no longer be transacted on via the API.
 *  - Added openAccount(), listAllAccountsJson(), setAccountStatus() for the
 *    Open New Account and Admin/Employee Panel tasks.
 */
public class AccountService {
    private final Map<String, Account> memoryRegistry = new HashMap<>();

    public void register(Account account) {
        memoryRegistry.put(account.getNumber(), account);
    }

    public Account find(String accountNumber) {
        if (memoryRegistry.containsKey(accountNumber)) {
            return memoryRegistry.get(accountNumber);
        }
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

    /** Returns the account's status ('ACTIVE', 'FROZEN', 'DORMANT', 'CLOSED'), or null if not found. */
    private String getStatus(Connection con, String accountNumber) throws SQLException {
        String sql = "SELECT status FROM account WHERE account_number = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("status") : null;
        }
    }

    public void deposit(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        String updateSql = "UPDATE account SET balance = balance + ? WHERE account_number = ? AND status = 'ACTIVE'";
        String txnSql = "INSERT INTO transaction (account_number, txn_type, amount, resulting_balance, description) " +
                        "VALUES (?, 'DEPOSIT', ?, (SELECT balance FROM account WHERE account_number = ?), ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String status = getStatus(con, accountNumber);
                if (status == null) {
                    throw new IllegalStateException("Account not found: " + accountNumber);
                }
                if (!"ACTIVE".equals(status)) {
                    throw new IllegalStateException("Account is " + status + " - deposits are not allowed");
                }

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setBigDecimal(1, amount);
                    ps.setString(2, accountNumber);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(txnSql)) {
                    ps.setString(1, accountNumber);
                    ps.setBigDecimal(2, amount);
                    ps.setString(3, accountNumber);
                    ps.setString(4, "Deposit via API");
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during deposit: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void withdraw(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        String updateSql = "UPDATE account SET balance = balance - ? WHERE account_number = ? " +
                           "AND balance >= ? AND status = 'ACTIVE'";
        String txnSql = "INSERT INTO transaction (account_number, txn_type, amount, resulting_balance, description) " +
                        "VALUES (?, 'WITHDRAW', ?, (SELECT balance FROM account WHERE account_number = ?), ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String status = getStatus(con, accountNumber);
                if (status == null) {
                    throw new IllegalStateException("Account not found: " + accountNumber);
                }
                if (!"ACTIVE".equals(status)) {
                    throw new IllegalStateException("Account is " + status + " - withdrawals are not allowed");
                }

                int rows;
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setBigDecimal(1, amount);
                    ps.setString(2, accountNumber);
                    ps.setBigDecimal(3, amount);
                    rows = ps.executeUpdate();
                }
                if (rows == 0) {
                    throw new IllegalStateException("Insufficient balance for withdrawal");
                }
                try (PreparedStatement ps = con.prepareStatement(txnSql)) {
                    ps.setString(1, accountNumber);
                    ps.setBigDecimal(2, amount);
                    ps.setString(3, accountNumber);
                    ps.setString(4, "Withdrawal via API");
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during withdraw: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Opens a new account (SAVINGS, CURRENT, or FIXED_DEPOSIT) for an existing customer.
     * Validates the initial deposit against the account_type's minimum balance.
     * Returns the newly generated account number.
     */
    public String openAccount(int customerId, int branchId, String accountTypeName, BigDecimal initialDeposit) {
        if (initialDeposit == null || initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Validate customer exists
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 1 FROM customer WHERE customer_id = ?")) {
                    ps.setInt(1, customerId);
                    if (!ps.executeQuery().next()) {
                        throw new IllegalArgumentException("Customer not found: " + customerId);
                    }
                }

                // 2. Look up account type + minimum balance
                int typeId;
                BigDecimal minBalance;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT type_id, min_balance FROM account_type WHERE type_name = ?")) {
                    ps.setString(1, accountTypeName.toUpperCase());
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new IllegalArgumentException(
                                "Invalid account type. Must be SAVINGS, CURRENT, or FIXED_DEPOSIT");
                    }
                    typeId = rs.getInt("type_id");
                    minBalance = rs.getBigDecimal("min_balance");
                }

                if (initialDeposit.compareTo(minBalance) < 0) {
                    throw new IllegalArgumentException(
                            "Initial deposit must be at least the minimum balance of " + minBalance +
                            " for a " + accountTypeName + " account");
                }

                // 3. Generate a unique account number: ACC<customerId><next 2-digit sequence>
                int seq;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) AS cnt FROM account WHERE customer_id = ?")) {
                    ps.setInt(1, customerId);
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    seq = rs.getInt("cnt") + 1;
                }
                String accountNumber = String.format("ACC%d%02d", customerId, seq);

                // 4. Insert the new account
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO account (account_number, customer_id, type_id, branch_id, balance, status, opened_date) " +
                        "VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?)")) {
                    ps.setString(1, accountNumber);
                    ps.setInt(2, customerId);
                    ps.setInt(3, typeId);
                    ps.setInt(4, branchId);
                    ps.setBigDecimal(5, initialDeposit);
                    ps.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }

                // 5. Log the opening deposit as a transaction, if non-zero
                if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO transaction (account_number, txn_type, amount, resulting_balance, description) " +
                            "VALUES (?, 'DEPOSIT', ?, ?, 'Initial deposit - account opening')")) {
                        ps.setString(1, accountNumber);
                        ps.setBigDecimal(2, initialDeposit);
                        ps.setBigDecimal(3, initialDeposit);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return accountNumber;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error opening account: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /** Sets an account's status. Used by the Admin/Employee Panel's freeze/activate feature. */
    public void setAccountStatus(String accountNumber, String newStatus) {
        if (!newStatus.equals("ACTIVE") && !newStatus.equals("FROZEN")) {
            throw new IllegalArgumentException("Status must be ACTIVE or FROZEN");
        }
        String sql = "UPDATE account SET status = ? WHERE account_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, accountNumber);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalStateException("Account not found: " + accountNumber);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating status: " + e.getMessage(), e);
        }
    }

    /** Returns all accounts with customer name, type, balance, and status - for the Admin/Employee Panel. */
    public String listAllAccountsJson() {
        String sql = "SELECT a.account_number, a.balance, a.status, t.type_name, c.customer_name, c.customer_id " +
                     "FROM account a " +
                     "JOIN account_type t ON a.type_id = t.type_id " +
                     "JOIN customer c ON a.customer_id = c.customer_id " +
                     "ORDER BY c.customer_name, a.account_number";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append(String.format(
                        "{\"accountNumber\":\"%s\",\"customerId\":%d,\"customerName\":\"%s\"," +
                        "\"type\":\"%s\",\"balance\":%.2f,\"status\":\"%s\"}",
                        rs.getString("account_number"), rs.getInt("customer_id"),
                        rs.getString("customer_name").replace("\"", "\\\""),
                        rs.getString("type_name"), rs.getBigDecimal("balance"), rs.getString("status")));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        } catch (SQLException e) {
            throw new RuntimeException("Database error listing accounts: " + e.getMessage(), e);
        }
    }
}
