package com.bank.util;

import com.bank.service.TransactionService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * TestACIDTransfer - Verifies the ACID Fund Transfer logic.
 */
public class TestACIDTransfer {
    public static void main(String[] args) {
        System.out.println("=== TESTING ACID FUND TRANSFER ===");
        TransactionService service = new TransactionService();

        String fromAcc = "ACC100101"; // Aarav Shah (Initial 85000)
        String toAcc = "ACC100401";   // Priya Joshi (Initial 55000)
        BigDecimal transferAmount = new BigDecimal("5000.00");

        try {
            System.out.println("Initial Balances:");
            printBalance(fromAcc);
            printBalance(toAcc);

            System.out.println("\nExecuting atomic transfer of ₹" + transferAmount + " from " + fromAcc + " to " + toAcc + "...");
            boolean success = service.transferFunds(fromAcc, toAcc, transferAmount, "Test Automated Transfer");

            if (success) {
                System.out.println("SUCCESS: Atomic transfer committed successfully!\n");
                System.out.println("Updated Balances:");
                printBalance(fromAcc);
                printBalance(toAcc);
            }

            // Test Rollback with invalid amount (insufficient funds)
            System.out.println("\nTesting Rollback with excessive amount (₹99999999)...");
            try {
                service.transferFunds(fromAcc, toAcc, new BigDecimal("99999999.00"), "Should Fail");
                System.err.println("FAILED: Should not have succeeded!");
            } catch (Exception ex) {
                System.out.println("SUCCESS: Transaction rolled back as expected! Error caught: " + ex.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printBalance(String accNo) {
        String sql = "SELECT balance FROM account WHERE account_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Account " + accNo + " Balance: ₹ " + rs.getBigDecimal("balance"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
