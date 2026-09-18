package com.bank.ui;

import com.bank.util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CustomerForm - Customer Master Management CRUD Module
 * Matches Section 14 of Student Implementation Manual (PatientForm equivalent).
 * Demonstrates INSERT, SELECT, UPDATE, DELETE, and JTable data binding.
 */
public class CustomerForm extends JFrame {
    private final JFrame parentDashboard;

    // Form Components
    private JTextField txtCustomerId;
    private JTextField txtCustomerName;
    private JTextField txtDOB;
    private JComboBox<String> cmbGender;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JTextField txtPAN;
    private JComboBox<String> cmbBranch;

    // Action Buttons
    private JButton btnAdd;
    private JButton btnSearch;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnRefresh;
    private JButton btnBack;

    // Table
    private JTable tblCustomers;
    private DefaultTableModel tableModel;

    public CustomerForm(JFrame parent) {
        this.parentDashboard = parent;
        initComponents();
        setLocationRelativeTo(null);
        loadCustomers();
    }

    public CustomerForm() {
        this(null);
    }

    private void initComponents() {
        setTitle("Bank Management System - Customer Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 680);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Title
        JLabel lblTitle = new JLabel("CUSTOMER MANAGEMENT (CRUD)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center split: Input Form on Top/Left, Buttons on Right
        JPanel formContainer = new JPanel(new BorderLayout(10, 10));
        JPanel inputGrid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCustomerId = new JTextField(15);
        txtCustomerName = new JTextField(15);
        txtDOB = new JTextField(15);
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        txtPhone = new JTextField(15);
        txtEmail = new JTextField(15);
        txtAddress = new JTextField(15);
        txtPAN = new JTextField(15);
        cmbBranch = new JComboBox<>(new String[]{"1 - Nariman Point", "2 - Connaught Place", "3 - MG Road", "4 - FC Road"});

        addFormField(inputGrid, gbc, 0, "Customer ID:", txtCustomerId);
        addFormField(inputGrid, gbc, 1, "Customer Name:", txtCustomerName);
        addFormField(inputGrid, gbc, 2, "Date of Birth (YYYY-MM-DD):", txtDOB);
        addFormField(inputGrid, gbc, 3, "Gender:", cmbGender);
        addFormField(inputGrid, gbc, 4, "Phone:", txtPhone);
        addFormField(inputGrid, gbc, 5, "Email:", txtEmail);
        addFormField(inputGrid, gbc, 6, "Address:", txtAddress);
        addFormField(inputGrid, gbc, 7, "PAN Number:", txtPAN);
        addFormField(inputGrid, gbc, 8, "Branch:", cmbBranch);

        formContainer.add(inputGrid, BorderLayout.CENTER);

        // Buttons Panel (Vertical on right side, matching manual layout)
        JPanel buttonCol = new JPanel(new GridLayout(7, 1, 6, 6));
        btnAdd = new JButton("ADD");
        btnSearch = new JButton("SEARCH");
        btnUpdate = new JButton("UPDATE");
        btnDelete = new JButton("DELETE");
        btnClear = new JButton("CLEAR");
        btnRefresh = new JButton("REFRESH");
        btnBack = new JButton("BACK");

        buttonCol.add(btnAdd);
        buttonCol.add(btnSearch);
        buttonCol.add(btnUpdate);
        buttonCol.add(btnDelete);
        buttonCol.add(btnClear);
        buttonCol.add(btnRefresh);
        buttonCol.add(btnBack);
        buttonCol.setPreferredSize(new Dimension(130, 220));

        formContainer.add(buttonCol, BorderLayout.EAST);
        mainPanel.add(formContainer, BorderLayout.NORTH);

        // Bottom Table
        String[] cols = {"Cust ID", "Name", "DOB", "Gender", "Phone", "Email", "Address", "PAN", "Branch"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCustomers = new JTable(tableModel);
        tblCustomers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tblCustomers);
        scrollPane.setPreferredSize(new Dimension(900, 260));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Wire Event Listeners
        btnAdd.addActionListener(e -> addCustomer());
        btnSearch.addActionListener(e -> searchCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearFields());
        btnRefresh.addActionListener(e -> loadCustomers());
        btnBack.addActionListener(e -> {
            if (parentDashboard != null) {
                parentDashboard.setVisible(true);
            }
            this.dispose();
        });

        // Click a JTable row to fill form (Section 14.11)
        tblCustomers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblCustomers.getSelectedRow() != -1) {
                int row = tblCustomers.getSelectedRow();
                txtCustomerId.setText(tableModel.getValueAt(row, 0).toString());
                txtCustomerName.setText(tableModel.getValueAt(row, 1).toString());
                txtDOB.setText(tableModel.getValueAt(row, 2).toString());
                cmbGender.setSelectedItem(tableModel.getValueAt(row, 3).toString());
                txtPhone.setText(tableModel.getValueAt(row, 4).toString());
                txtEmail.setText(tableModel.getValueAt(row, 5).toString());
                txtAddress.setText(tableModel.getValueAt(row, 6).toString());
                txtPAN.setText(tableModel.getValueAt(row, 7).toString());
            }
        });
    }

    private void addFormField(JPanel p, GridBagConstraints g, int row, String label, Component comp) {
        g.gridy = row;
        g.gridx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        p.add(comp, g);
    }

    // 14.4 loadPatients() equivalent
    private void loadCustomers() {
        tableModel.setRowCount(0);
        String sql = "SELECT customer_id, customer_name, dob, gender, phone, email, address, pan_number, branch_id " +
                     "FROM customer ORDER BY customer_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getDate("dob"),
                    rs.getString("gender"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("pan_number"),
                    rs.getInt("branch_id")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 14.5 clearFields()
    private void clearFields() {
        txtCustomerId.setText("");
        txtCustomerName.setText("");
        txtDOB.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtPAN.setText("");
        cmbGender.setSelectedIndex(0);
        cmbBranch.setSelectedIndex(0);
        tblCustomers.clearSelection();
        txtCustomerName.requestFocus();
    }

    // 14.6 ADD customer
    private void addCustomer() {
        String name = txtCustomerName.getText().trim();
        String dob = txtDOB.getText().trim();
        String gender = cmbGender.getSelectedItem().toString();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        String pan = txtPAN.getText().trim();
        int branchId = cmbBranch.getSelectedIndex() + 1;

        if (name.isEmpty() || dob.isEmpty() || phone.isEmpty() || email.isEmpty() || pan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, DOB, Phone, Email, and PAN are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO customer (customer_name, dob, gender, phone, email, address, pan_number, branch_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDate(2, java.sql.Date.valueOf(dob));
            ps.setString(3, gender);
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.setString(6, address);
            ps.setString(7, pan);
            ps.setInt(8, branchId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Customer Added Successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomers();
                clearFields();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding customer: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 14.7 SEARCH customer
    private void searchCustomer() {
        String id = txtCustomerId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Customer ID to search.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT customer_name, dob, gender, phone, email, address, pan_number, branch_id " +
                     "FROM customer WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtCustomerName.setText(rs.getString("customer_name"));
                txtDOB.setText(rs.getDate("dob").toString());
                cmbGender.setSelectedItem(rs.getString("gender"));
                txtPhone.setText(rs.getString("phone"));
                txtEmail.setText(rs.getString("email"));
                txtAddress.setText(rs.getString("address"));
                txtPAN.setText(rs.getString("pan_number"));
                cmbBranch.setSelectedIndex(Math.max(0, rs.getInt("branch_id") - 1));
            } else {
                JOptionPane.showMessageDialog(this, "Customer not found.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 14.8 UPDATE customer
    private void updateCustomer() {
        String id = txtCustomerId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search or select a Customer ID first.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE customer SET customer_name=?, dob=?, gender=?, phone=?, email=?, address=?, pan_number=?, branch_id=? WHERE customer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtCustomerName.getText().trim());
            ps.setDate(2, java.sql.Date.valueOf(txtDOB.getText().trim()));
            ps.setString(3, cmbGender.getSelectedItem().toString());
            ps.setString(4, txtPhone.getText().trim());
            ps.setString(5, txtEmail.getText().trim());
            ps.setString(6, txtAddress.getText().trim());
            ps.setString(7, txtPAN.getText().trim());
            ps.setInt(8, cmbBranch.getSelectedIndex() + 1);
            ps.setInt(9, Integer.parseInt(id));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Customer Updated Successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomers();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Customer ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 14.9 DELETE customer with referential integrity demo
    private void deleteCustomer() {
        String id = txtCustomerId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Customer ID to delete.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM customer WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Customer Deleted Successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomers();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Customer ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            // Referential integrity: demonstrates foreign-key restriction
            JOptionPane.showMessageDialog(this, "Referential Integrity Violation: Cannot delete customer with active accounts or loans.\n" + ex.getMessage(), "DBMS Constraint Protected", JOptionPane.ERROR_MESSAGE);
        }
    }
}
