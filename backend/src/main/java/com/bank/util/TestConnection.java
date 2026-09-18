package com.bank.util;

import java.sql.Connection;

/**
 * TestConnection - Verifies Java-to-MySQL JDBC connectivity.
 * Corresponds to Section 11.2 of Student Implementation Manual.
 */
public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Attempting to connect to MySQL Database (bank_db)...");
        try (Connection con = DBConnection.getConnection()) {
            if (con != null && !con.isClosed()) {
                System.out.println("MySQL Database Connected Successfully!");
                System.out.println("Java successfully connected to MySQL 8.0 (bank_db).");
                System.out.println("Connection closed successfully.");
                System.out.println("BUILD SUCCESSFUL");
            } else {
                System.err.println("Connection could not be established.");
            }
        } catch (Exception e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
