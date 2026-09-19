package com.bank.service;

import com.bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * LoanService - Handles loan applications and admin/employee approval workflow.
 * Task: Loan Management (customer application + admin/employee approval/rejection)
 */
public class LoanService {

    /**
     * Customer applies for a loan. Validates amount and tenure against the
     * loan_type's max_tenure_months before inserting.
     */
    public int applyForLoan(int customerId, int loanTypeId, int branchId,
                             BigDecimal amount, int tenureMonths) throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan amount must be positive");
        }
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure must be positive");
        }

        try (Connection con = DBConnection.getConnection()) {
            // Validate loan type + fetch base rate / max tenure
            String typeSql = "SELECT base_rate, max_tenure_months FROM loan_type WHERE loan_type_id = ?";
            BigDecimal baseRate;
            int maxTenure;
            try (PreparedStatement ps = con.prepareStatement(typeSql)) {
                ps.setInt(1, loanTypeId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new IllegalArgumentException("Invalid loan type");
                }
                baseRate = rs.getBigDecimal("base_rate");
                maxTenure = rs.getInt("max_tenure_months");
            }

            if (tenureMonths > maxTenure) {
                throw new IllegalArgumentException(
                        "Tenure exceeds maximum allowed (" + maxTenure + " months) for this loan type");
            }

            String insertSql = "INSERT INTO loan (customer_id, loan_type_id, branch_id, amount, " +
                    "interest_rate, tenure_months, status, applied_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'PENDING', ?)";
            try (PreparedStatement ps = con.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.setInt(2, loanTypeId);
                ps.setInt(3, branchId);
                ps.setBigDecimal(4, amount);
                ps.setBigDecimal(5, baseRate);
                ps.setInt(6, tenureMonths);
                ps.setDate(7, java.sql.Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
                ResultSet gk = ps.getGeneratedKeys();
                gk.next();
                return gk.getInt(1);
            }
        }
    }

    /**
     * List loans, optionally filtered by status (PENDING/APPROVED/REJECTED).
     * Pass null to get all loans.
     */
    public String listLoansJson(String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT l.loan_id, l.customer_id, c.customer_name, lt.type_name, l.amount, " +
                "l.interest_rate, l.tenure_months, l.status, l.applied_date, l.approved_by_emp_id " +
                "FROM loan l " +
                "JOIN customer c ON l.customer_id = c.customer_id " +
                "JOIN loan_type lt ON l.loan_type_id = lt.loan_type_id ");
        boolean hasFilter = statusFilter != null && !statusFilter.isBlank();
        if (hasFilter) {
            sql.append("WHERE l.status = ? ");
        }
        sql.append("ORDER BY l.applied_date DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (hasFilter) {
                ps.setString(1, statusFilter.toUpperCase());
            }
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append(String.format(
                        "{\"loanId\":%d,\"customerId\":%d,\"customerName\":\"%s\",\"loanType\":\"%s\"," +
                        "\"amount\":%.2f,\"interestRate\":%.2f,\"tenureMonths\":%d,\"status\":\"%s\"," +
                        "\"appliedDate\":\"%s\",\"approvedByEmpId\":%s}",
                        rs.getInt("loan_id"), rs.getInt("customer_id"),
                        escape(rs.getString("customer_name")), rs.getString("type_name"),
                        rs.getBigDecimal("amount"), rs.getBigDecimal("interest_rate"),
                        rs.getInt("tenure_months"), rs.getString("status"),
                        rs.getDate("applied_date"),
                        rs.getObject("approved_by_emp_id") != null ? rs.getInt("approved_by_emp_id") : "null"));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * Employee/Admin approves or rejects a pending loan.
     * Only PENDING loans can be transitioned, to avoid re-approving/rejecting a decided loan.
     */
    public boolean decideLoan(int loanId, String decision, int empId) throws SQLException {
        if (!decision.equals("APPROVED") && !decision.equals("REJECTED")) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }
        String sql = "UPDATE loan SET status = ?, approved_by_emp_id = ? " +
                     "WHERE loan_id = ? AND status = 'PENDING'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, decision);
            ps.setInt(2, empId);
            ps.setInt(3, loanId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalStateException("Loan not found or already decided");
            }
            return true;
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
