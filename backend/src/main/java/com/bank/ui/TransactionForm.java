package com.bank.ui;

import com.bank.service.TransactionService;
import com.bank.util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TransactionForm - Financial Operations Form
 * Handles Deposit, Withdrawal, and ACID Fund Transfer.
 */
public class TransactionForm extends JFrame {
    private final JFrame parentDashboard;
    private final String username;
    private final String role;
    private final TransactionService txnService = new TransactionService();

    private JComboBox<String> cmbAccount;
    private JLabel lblBalance;
    private JTextField txtAmount;
    private JTextField txtTargetAccount;
    private JTextField txtRemarks;
    private JTable tblHistory;
    private DefaultTableModel historyModel;

    public TransactionForm(JFrame parent, String username, String role) {
        this.parentDashboard = parent;
        this.username = username;
        this.role = role;
        initComponents();
        setLocationRelativeTo(null);
        loadAccounts();
    }

    public TransactionForm() {
        this(null, "admin", "ADMIN");
    }

    private void initComponents() {
        setTitle("Bank Management System - Transactions");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 600);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 8));
        topPanel.setBorder(BorderFactory.createTitledBorder("Select Account & Overview"));
        topPanel.add(new JLabel("Select Account Number:"));
        cmbAccount = new JComboBox<>();
        topPanel.add(cmbAccount);

        topPanel.add(new JLabel("Current Balance:"));
        lblBalance = new JLabel("₹ 0.00");
        lblBalance.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblBalance.setForeground(new Color(0, 120, 0));
        topPanel.add(lblBalance);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Operations
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBorder(BorderFactory.createTitledBorder("Financial Actions"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtAmount = new JTextField(12);
        txtTargetAccount = new JTextField(12);
        txtRemarks = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; actionPanel.add(new JLabel("Amount (₹):"), gbc);
        gbc.gridx = 1; actionPanel.add(txtAmount, gbc);

        gbc.gridx = 0; gbc.gridy = 1; actionPanel.add(new JLabel("Target Account (for Transfer):"), gbc);
        gbc.gridx = 1; actionPanel.add(txtTargetAccount, gbc);

        gbc.gridx = 0; gbc.gridy = 2; actionPanel.add(new JLabel("Remarks:"), gbc);
        gbc.gridx = 1; actionPanel.add(txtRemarks, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnDeposit = new JButton("Deposit");
        JButton btnWithdraw = new JButton("Withdraw");
        JButton btnTransfer = new JButton("Transfer (ACID)");
        JButton btnBack = new JButton("Back to Dashboard");

        btnDeposit.setBackground(new Color(220, 245, 220));
        btnWithdraw.setBackground(new Color(255, 230, 230));
        btnTransfer.setBackground(new Color(230, 235, 255));

        btnPanel.add(btnDeposit);
        btnPanel.add(btnWithdraw);
        btnPanel.add(btnTransfer);
        btnPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        actionPanel.add(btnPanel, gbc);
        centerPanel.add(actionPanel, BorderLayout.NORTH);

        // History Table
        String[] cols = {"Txn ID", "Date/Time", "Type", "Amount (₹)", "Target Acc", "Description"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHistory = new JTable(historyModel);
        JScrollPane scroll = new JScrollPane(tblHistory);
        scroll.setBorder(BorderFactory.createTitledBorder("Transaction History Audit Log"));
        centerPanel.add(scroll, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Events
        cmbAccount.addActionListener(e -> refreshAccountDetails());
        btnDeposit.addActionListener(e -> handleDeposit());
        btnWithdraw.addActionListener(e -> handleWithdraw());
        btnTransfer.addActionListener(e -> handleTransfer());
        btnBack.addActionListener(e -> {
            if (parentDashboard != null) parentDashboard.setVisible(true);
            this.dispose();
        });
    }

    private void loadAccounts() {
        cmbAccount.removeAllItems();
        String sql = "SELECT account_number FROM account ORDER BY account_number";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cmbAccount.addItem(rs.getString("account_number"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage());
        }
        refreshAccountDetails();
    }

    private void refreshAccountDetails() {
        String accNo = (String) cmbAccount.getSelectedItem();
        if (accNo == null) return;

        // Fetch balance
        String balSql = "SELECT balance FROM account WHERE account_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(balSql)) {
            ps.setString(1, accNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblBalance.setText("₹ " + rs.getBigDecimal("balance").setScale(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fetch transactions
        historyModel.setRowCount(0);
        String histSql = "SELECT txn_id, txn_date, txn_type, amount, target_account, description " +
                         "FROM transaction WHERE account_number = ? OR target_account = ? ORDER BY txn_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(histSql)) {
            ps.setString(1, accNo);
            ps.setString(2, accNo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                historyModel.addRow(new Object[]{
                    rs.getInt("txn_id"),
                    rs.getString("txn_date"),
                    rs.getString("txn_type"),
                    rs.getBigDecimal("amount"),
                    rs.getString("target_account") != null ? rs.getString("target_account") : "-",
                    rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeposit() {
        String accNo = (String) cmbAccount.getSelectedItem();
        String amtStr = txtAmount.getText().trim();
        if (amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount.");
            return;
        }
        try {
            BigDecimal amt = new BigDecimal(amtStr);
            if (amt.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

            String sqlUpdate = "UPDATE account SET balance = balance + ? WHERE account_number = ?";
            String sqlTxn = "INSERT INTO transaction (account_number, txn_type, amount, resulting_balance, description) " +
                            "VALUES (?, 'DEPOSIT', ?, (SELECT balance FROM account WHERE account_number = ?), ?)";

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try (PreparedStatement psUp = con.prepareStatement(sqlUpdate);
                     PreparedStatement psTxn = con.prepareStatement(sqlTxn)) {
                    psUp.setBigDecimal(1, amt);
                    psUp.setString(2, accNo);
                    psUp.executeUpdate();

                    psTxn.setString(1, accNo);
                    psTxn.setBigDecimal(2, amt);
                    psTxn.setString(3, accNo);
                    psTxn.setString(4, "Cash/Online Deposit");
                    psTxn.executeUpdate();

                    con.commit();
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                }
            }

            JOptionPane.showMessageDialog(this, "₹ " + amt + " Deposited Successfully!");
            txtAmount.setText("");
            refreshAccountDetails();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Deposit Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleWithdraw() {
        String accNo = (String) cmbAccount.getSelectedItem();
        String amtStr = txtAmount.getText().trim();
        if (amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount.");
            return;
        }
        try {
            BigDecimal amt = new BigDecimal(amtStr);
            if (amt.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

            String sqlUpdate = "UPDATE account SET balance = balance - ? WHERE account_number = ? AND balance >= ?";
            String sqlTxn = "INSERT INTO transaction (account_number, txn_type, amount, resulting_balance, description) " +
                            "VALUES (?, 'WITHDRAW', ?, (SELECT balance FROM account WHERE account_number = ?), ?)";

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try (PreparedStatement psUp = con.prepareStatement(sqlUpdate);
                     PreparedStatement psTxn = con.prepareStatement(sqlTxn)) {
                    psUp.setBigDecimal(1, amt);
                    psUp.setString(2, accNo);
                    psUp.setBigDecimal(3, amt);
                    int rows = psUp.executeUpdate();
                    if (rows == 0) throw new SQLException("Insufficient balance for withdrawal!");

                    psTxn.setString(1, accNo);
                    psTxn.setBigDecimal(2, amt);
                    psTxn.setString(3, accNo);
                    psTxn.setString(4, "Withdrawal / ATM");
                    psTxn.executeUpdate();

                    con.commit();
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                }
            }

            JOptionPane.showMessageDialog(this, "₹ " + amt + " Withdrawn Successfully!");
            txtAmount.setText("");
            refreshAccountDetails();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Withdrawal Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTransfer() {
        String fromAcc = (String) cmbAccount.getSelectedItem();
        String toAcc = txtTargetAccount.getText().trim();
        String amtStr = txtAmount.getText().trim();
        String remarks = txtRemarks.getText().trim();

        if (toAcc.isEmpty() || amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Target Account and Amount are required for transfer.");
            return;
        }

        try {
            BigDecimal amt = new BigDecimal(amtStr);
            boolean ok = txnService.transferFunds(fromAcc, toAcc, amt, remarks.isEmpty() ? "Fund Transfer" : remarks);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "ACID Transfer Completed!\n₹ " + amt + " transferred from " + fromAcc + " to " + toAcc + ".\nBoth accounts and transaction logs committed atomically.",
                        "Transfer Success", JOptionPane.INFORMATION_MESSAGE);
                txtAmount.setText("");
                txtTargetAccount.setText("");
                txtRemarks.setText("");
                refreshAccountDetails();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Transfer Failed (Rolled Back): " + ex.getMessage(), "ACID Rollback", JOptionPane.ERROR_MESSAGE);
        }
    }
}
