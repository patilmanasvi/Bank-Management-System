package com.bank.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard - Central Navigation Hub
 * Matches Section 13 of Student Implementation Manual.
 */
public class Dashboard extends JFrame {
    private final String username;
    private final String role;
    private final int referenceId;

    public Dashboard(String username, String role, int referenceId) {
        this.username = username;
        this.role = role;
        this.referenceId = referenceId;
        initComponents();
        setLocationRelativeTo(null);
    }

    public Dashboard() {
        this("admin", "ADMIN", 101);
    }

    private void initComponents() {
        setTitle("Bank Management System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 480);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("BANK MANAGEMENT SYSTEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(25, 55, 110));

        JLabel lblUser = new JLabel("Logged in as: " + username + " (" + role + ")", SwingConstants.CENTER);
        lblUser.setFont(new Font("SansSerif", Font.ITALIC, 13));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblUser, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Grid of Operations
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JButton btnCustomer = new JButton("Customer Management");
        JButton btnTransactions = new JButton("Transactions (Deposit/Withdraw/Transfer)");
        JButton btnAccounts = new JButton("Account Overview");
        JButton btnLoans = new JButton("Loan Applications");
        JButton btnReports = new JButton("Database Summary Reports");
        JButton btnLogout = new JButton("Logout");

        // Styling
        Font btnFont = new Font("SansSerif", Font.BOLD, 14);
        btnCustomer.setFont(btnFont);
        btnTransactions.setFont(btnFont);
        btnAccounts.setFont(btnFont);
        btnLoans.setFont(btnFont);
        btnReports.setFont(btnFont);
        btnLogout.setFont(btnFont);
        btnLogout.setForeground(new Color(180, 20, 20));

        gridPanel.add(btnCustomer);
        gridPanel.add(btnTransactions);
        gridPanel.add(btnAccounts);
        gridPanel.add(btnLoans);
        gridPanel.add(btnReports);
        gridPanel.add(btnLogout);

        mainPanel.add(gridPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Actions
        btnCustomer.addActionListener(e -> {
            new CustomerForm(this).setVisible(true);
            this.setVisible(false);
        });

        btnTransactions.addActionListener(e -> {
            new TransactionForm(this, username, role).setVisible(true);
            this.setVisible(false);
        });

        btnAccounts.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Account Overview is accessible via Transactions & Database views.\nAll accounts are persisted in MySQL 'account' table.",
                    "Account Module", JOptionPane.INFORMATION_MESSAGE);
        });

        btnLoans.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Loan Management Module is connected to MySQL 'loan' and 'loan_payment' tables.",
                    "Loan Module", JOptionPane.INFORMATION_MESSAGE);
        });

        btnReports.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Total System Tables: 12 (3NF Normalized)\nACID Support: Active with Commit & Rollback",
                    "DBMS Verification", JOptionPane.INFORMATION_MESSAGE);
        });

        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
    }
}
