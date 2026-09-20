package com.bank.ui;

import com.bank.service.AccountService;
import com.bank.service.LoanService;
import com.bank.service.TransactionService;
import com.bank.util.DBConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * BankWebServer - Embedded HTTP & REST API Server
 * Serves the frontend HTML/CSS pages and handles JSON/form API calls connecting to MySQL.
 *
 * CHANGES (backend contribution):
 *  - Fixed: BigDecimal.parseamount crashes moved inside try/catch (Deposit/Withdraw/Transfer)
 *  - Added: /api/transactions/history now supports ?type=&from=&to= filters
 *  - Added: /api/admin/accounts, /api/admin/account/freeze, /api/admin/account/activate
 *  - Added: /api/loan/apply, /api/loan/list, /api/loan/decision
 *  - Added: /api/account/open (Savings/Current/Fixed Deposit)
 */
public class BankWebServer {
    private static final int PORT = 8080;
    private static final File FRONTEND_DIR = new File("frontend");
    private static final AccountService accountService = new AccountService();
    private static final TransactionService txnService = new TransactionService();
    private static final LoanService loanService = new LoanService();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Existing endpoints
        server.createContext("/api/auth/login", new LoginApiHandler());
        server.createContext("/api/auth/signup", new SignupApiHandler());
        server.createContext("/api/account/details", new AccountDetailsHandler());
        server.createContext("/api/transactions/deposit", new DepositHandler());
        server.createContext("/api/transactions/withdraw", new WithdrawHandler());
        server.createContext("/api/transactions/transfer", new TransferHandler());
        server.createContext("/api/transactions/history", new HistoryHandler());

        // New: Open New Account
        server.createContext("/api/account/open", new OpenAccountHandler());

        // New: Admin/Employee Panel
        server.createContext("/api/admin/accounts", new AdminAccountsHandler());
        server.createContext("/api/admin/account/freeze", new FreezeAccountHandler());
        server.createContext("/api/admin/account/activate", new ActivateAccountHandler());

        // New: Loan Management
        server.createContext("/api/loan/apply", new LoanApplyHandler());
        server.createContext("/api/loan/list", new LoanListHandler());
        server.createContext("/api/loan/decision", new LoanDecisionHandler());

        // New: User Profile & Security Endpoints
        server.createContext("/api/user/profile", new UserProfileHandler());
        server.createContext("/api/user/profile/update", new UserProfileUpdateHandler());
        server.createContext("/api/user/change-password", new ChangePasswordHandler());

        // Static Files Handler (serves HTML, CSS, JS from frontend folder)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("=================================================");
        System.out.println("  BANK MANAGEMENT SYSTEM - WEB SERVER RUNNING!");
        System.out.println("  Open your browser at: http://localhost:" + PORT);
        System.out.println("=================================================");
    }

    // Static file server
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/login.html";
            }

            File file = new File(FRONTEND_DIR, path);
            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String contentType = "text/html";
                if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";
                else if (path.endsWith(".png")) contentType = "image/png";

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String response = "404 Not Found: " + path;
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    // 1. Login Handler (unchanged)
    static class LoginApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseJsonOrForm(body);

            String usernameOrEmail = params.get("username");
            if (usernameOrEmail == null) usernameOrEmail = params.get("email");
            if (usernameOrEmail != null) usernameOrEmail = usernameOrEmail.trim();
            String password = params.get("password");
            if (password != null) password = password.trim();

            String sql = "SELECT l.username, l.role, l.reference_id, a.account_number, a.balance, " +
                         "COALESCE(c.customer_name, e.emp_name, l.username) as display_name " +
                         "FROM login l " +
                         "LEFT JOIN customer c ON l.reference_id = c.customer_id " +
                         "LEFT JOIN employee e ON l.reference_id = e.emp_id " +
                         "LEFT JOIN account a ON (c.customer_id = a.customer_id OR a.account_number = CASE WHEN l.role = 'ADMIN' THEN 'ACC100102' WHEN l.role = 'EMPLOYEE' THEN 'ACC100202' ELSE 'ACC100101' END) " +
                         "WHERE (l.username = ? OR c.email = ? OR e.email = ?) AND l.password = ? LIMIT 1";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, usernameOrEmail);
                ps.setString(2, usernameOrEmail);
                ps.setString(3, usernameOrEmail);
                ps.setString(4, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String role = rs.getString("role");
                    String user = rs.getString("username");
                    String accNo = rs.getString("account_number");
                    BigDecimal bal = rs.getBigDecimal("balance");
                    String name = rs.getString("display_name");

                    String json = String.format("{\"success\":true,\"username\":\"%s\",\"role\":\"%s\",\"accountNumber\":\"%s\",\"balance\":%.2f,\"customerName\":\"%s\"}",
                            user, role, accNo != null ? accNo : "N/A", bal != null ? bal : BigDecimal.ZERO, name != null ? name : user);
                    sendJson(exchange, 200, json);
                } else {
                    sendJson(exchange, 401, "{\"success\":false,\"message\":\"Invalid credentials\"}");
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 2. Signup Handler (unchanged)
    static class SignupApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> p = parseJsonOrForm(body);

            String name = p.getOrDefault("name", "New Customer");
            String email = p.get("email");
            String password = p.get("password");
            String phone = p.getOrDefault("phone", "9876543210");
            String pan = p.getOrDefault("pan", "PAN" + System.currentTimeMillis() % 100000);

            if (email == null || password == null) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Email and password required\"}");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    String custSql = "INSERT INTO customer (customer_name, dob, gender, phone, email, address, pan_number, branch_id) " +
                                     "VALUES (?, '1995-01-01', 'Other', ?, ?, 'Online Signup', ?, 1)";
                    int custId;
                    try (PreparedStatement ps = con.prepareStatement(custSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, name);
                        ps.setString(2, phone);
                        ps.setString(3, email);
                        ps.setString(4, pan);
                        ps.executeUpdate();
                        ResultSet gk = ps.getGeneratedKeys();
                        gk.next();
                        custId = gk.getInt(1);
                    }

                    String loginSql = "INSERT INTO login (username, password, role, reference_id) VALUES (?, ?, 'CUSTOMER', ?)";
                    try (PreparedStatement ps = con.prepareStatement(loginSql)) {
                        ps.setString(1, email);
                        ps.setString(2, password);
                        ps.setInt(3, custId);
                        ps.executeUpdate();
                    }

                    String accNo = "ACC" + custId + "01";
                    String accSql = "INSERT INTO account (account_number, customer_id, type_id, branch_id, balance, status, opened_date) " +
                                   "VALUES (?, ?, 1, 1, 1000.00, 'ACTIVE', CURDATE())";
                    try (PreparedStatement ps = con.prepareStatement(accSql)) {
                        ps.setString(1, accNo);
                        ps.setInt(2, custId);
                        ps.executeUpdate();
                    }

                    con.commit();
                    sendJson(exchange, 200, String.format("{\"success\":true,\"message\":\"Account created!\",\"accountNumber\":\"%s\"}", accNo));
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 3. Account Details Handler (unchanged)
    static class AccountDetailsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            String query = exchange.getRequestURI().getQuery();
            String accNo = getQueryParam(query, "acc");
            if (accNo == null) accNo = "ACC100101";

            String sql = "SELECT a.account_number, a.balance, t.type_name, c.customer_name " +
                         "FROM account a JOIN account_type t ON a.type_id = t.type_id " +
                         "JOIN customer c ON a.customer_id = c.customer_id WHERE a.account_number = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, accNo);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String json = String.format("{\"accountNumber\":\"%s\",\"balance\":%.2f,\"type\":\"%s\",\"customerName\":\"%s\"}",
                            rs.getString("account_number"), rs.getBigDecimal("balance"), rs.getString("type_name"), rs.getString("customer_name"));
                    sendJson(exchange, 200, json);
                } else {
                    sendJson(exchange, 404, "{\"error\":\"Account not found\"}");
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 4. Deposit Handler - FIXED: amount parsing now inside try, so bad input returns clean JSON 400 instead of a raw 500
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String accNo = p.get("accountNumber");
                if (accNo == null || p.get("amount") == null) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"accountNumber and amount are required\"}");
                    return;
                }
                BigDecimal amt = new BigDecimal(p.get("amount"));
                accountService.deposit(accNo, amt);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Deposit successful\"}");
            } catch (NumberFormatException nfe) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"amount must be a valid number\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 5. Withdraw Handler - same fix as Deposit
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String accNo = p.get("accountNumber");
                if (accNo == null || p.get("amount") == null) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"accountNumber and amount are required\"}");
                    return;
                }
                BigDecimal amt = new BigDecimal(p.get("amount"));
                accountService.withdraw(accNo, amt);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Withdrawal successful\"}");
            } catch (NumberFormatException nfe) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"amount must be a valid number\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 6. Transfer Handler (ACID) - same fix as Deposit
    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String fromAcc = p.get("fromAccount");
                String toAcc = p.get("toAccount");
                if (fromAcc == null || toAcc == null || p.get("amount") == null) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"fromAccount, toAccount and amount are required\"}");
                    return;
                }
                BigDecimal amt = new BigDecimal(p.get("amount"));
                String remarks = p.getOrDefault("remarks", "Web Fund Transfer");

                boolean ok = txnService.transferFunds(fromAcc, toAcc, amt, remarks);
                if (ok) {
                    sendJson(exchange, 200, "{\"success\":true,\"message\":\"Atomic Fund Transfer Completed Successfully!\"}");
                } else {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"Transfer failed.\"}");
                }
            } catch (NumberFormatException nfe) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"amount must be a valid number\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 7. Transaction History Handler - now supports ?type=&from=&to= filters
    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            String query = exchange.getRequestURI().getQuery();
            String accNo = getQueryParam(query, "acc");
            if (accNo == null) accNo = "ACC100101";
            String type = getQueryParam(query, "type");
            String fromStr = getQueryParam(query, "from");
            String toStr = getQueryParam(query, "to");

            StringBuilder sql = new StringBuilder(
                "SELECT txn_id, txn_date, txn_type, amount, target_account, resulting_balance, description " +
                "FROM transaction WHERE (account_number = ? OR target_account = ?) ");
            if (type != null && !type.isBlank()) sql.append("AND txn_type = ? ");
            if (fromStr != null && !fromStr.isBlank()) sql.append("AND txn_date >= ? ");
            if (toStr != null && !toStr.isBlank()) sql.append("AND txn_date < ? ");
            sql.append("ORDER BY txn_date DESC");

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int idx = 1;
                ps.setString(idx++, accNo);
                ps.setString(idx++, accNo);
                if (type != null && !type.isBlank()) ps.setString(idx++, type.toUpperCase());
                if (fromStr != null && !fromStr.isBlank()) ps.setDate(idx++, java.sql.Date.valueOf(LocalDate.parse(fromStr)));
                if (toStr != null && !toStr.isBlank()) ps.setDate(idx++, java.sql.Date.valueOf(LocalDate.parse(toStr).plusDays(1)));

                ResultSet rs = ps.executeQuery();
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append(String.format("{\"id\":%d,\"date\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"target\":\"%s\",\"balance\":%.2f,\"desc\":\"%s\"}",
                            rs.getInt("txn_id"), rs.getString("txn_date"), rs.getString("txn_type"), rs.getBigDecimal("amount"),
                            rs.getString("target_account") != null ? rs.getString("target_account") : "-",
                            rs.getBigDecimal("resulting_balance"), escapeJson(rs.getString("description"))));
                    first = false;
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 8. NEW: Open New Account Handler (Savings / Current / Fixed Deposit)
    static class OpenAccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                if (p.get("customerId") == null || p.get("accountType") == null || p.get("initialDeposit") == null) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"customerId, accountType and initialDeposit are required\"}");
                    return;
                }
                int customerId = Integer.parseInt(p.get("customerId"));
                int branchId = p.get("branchId") != null ? Integer.parseInt(p.get("branchId")) : 1;
                String accountType = p.get("accountType");
                BigDecimal initialDeposit = new BigDecimal(p.get("initialDeposit"));

                String accNo = accountService.openAccount(customerId, branchId, accountType, initialDeposit);
                sendJson(exchange, 200, String.format(
                        "{\"success\":true,\"message\":\"Account opened successfully\",\"accountNumber\":\"%s\"}", accNo));
            } catch (NumberFormatException nfe) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"customerId/branchId/initialDeposit must be valid numbers\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 9. NEW: Admin - list all accounts
    static class AdminAccountsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            try {
                sendJson(exchange, 200, accountService.listAllAccountsJson());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 10. NEW: Admin - freeze account
    static class FreezeAccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String accNo = p.get("accountNumber");
                if (accNo == null) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"accountNumber is required\"}");
                    return;
                }
                accountService.setAccountStatus(accNo, "FROZEN");
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Account frozen\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 11. NEW: Admin - activate account
    static class ActivateAccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String accNo = p.get("accountNumber");
                if (accNo == null) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"accountNumber is required\"}");
                    return;
                }
                accountService.setAccountStatus(accNo, "ACTIVE");
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Account activated\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 12. NEW: Loan - customer applies
    static class LoanApplyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                if (p.get("customerId") == null || p.get("loanTypeId") == null ||
                    p.get("amount") == null || p.get("tenureMonths") == null) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"customerId, loanTypeId, amount and tenureMonths are required\"}");
                    return;
                }
                int customerId = Integer.parseInt(p.get("customerId"));
                int loanTypeId = Integer.parseInt(p.get("loanTypeId"));
                int branchId = p.get("branchId") != null ? Integer.parseInt(p.get("branchId")) : 1;
                BigDecimal amount = new BigDecimal(p.get("amount"));
                int tenureMonths = Integer.parseInt(p.get("tenureMonths"));

                int loanId = loanService.applyForLoan(customerId, loanTypeId, branchId, amount, tenureMonths);
                sendJson(exchange, 200, String.format(
                        "{\"success\":true,\"message\":\"Loan application submitted\",\"loanId\":%d}", loanId));
            } catch (NumberFormatException nfe) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Numeric fields must be valid numbers\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 13. NEW: Loan - list (admin/employee view), optional ?status=PENDING
    static class LoanListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            try {
                String status = getQueryParam(exchange.getRequestURI().getQuery(), "status");
                sendJson(exchange, 200, loanService.listLoansJson(status));
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 14. NEW: Loan - admin/employee approve or reject
    static class LoanDecisionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                if (p.get("loanId") == null || p.get("decision") == null || p.get("empId") == null) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"loanId, decision and empId are required\"}");
                    return;
                }
                int loanId = Integer.parseInt(p.get("loanId"));
                String decision = p.get("decision").toUpperCase();
                int empId = Integer.parseInt(p.get("empId"));

                loanService.decideLoan(loanId, decision, empId);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Loan " + decision.toLowerCase() + "\"}");
            } catch (NumberFormatException nfe) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"loanId/empId must be valid numbers\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 11. User Profile Handler
    static class UserProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            String query = exchange.getRequestURI().getQuery();
            String username = getQueryParam(query, "user");
            if (username == null || username.isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Missing user query param\"}");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String sql = "SELECT l.username, l.role, l.reference_id, " +
                             "c.customer_id, c.customer_name, c.dob, c.gender, c.phone as cust_phone, c.email as cust_email, c.address, c.pan_number, " +
                             "e.emp_id, e.emp_name, e.role as emp_role, e.salary, e.phone as emp_phone, e.email as emp_email, e.hire_date, " +
                             "b.branch_name, b.city " +
                             "FROM login l " +
                             "LEFT JOIN customer c ON l.reference_id = c.customer_id " +
                             "LEFT JOIN employee e ON l.reference_id = e.emp_id " +
                             "LEFT JOIN branch b ON (c.branch_id = b.branch_id OR e.branch_id = b.branch_id) " +
                             "WHERE l.username = ? OR c.email = ? OR e.email = ? LIMIT 1";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, username);
                    ps.setString(2, username);
                    ps.setString(3, username);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String role = rs.getString("role");
                        StringBuilder sb = new StringBuilder("{");
                        sb.append("\"success\":true,");
                        sb.append("\"username\":\"").append(escapeJson(rs.getString("username"))).append("\",");
                        sb.append("\"role\":\"").append(escapeJson(role)).append("\",");
                        sb.append("\"branch\":\"").append(escapeJson(rs.getString("branch_name"))).append(" (").append(escapeJson(rs.getString("city"))).append(")\",");

                        if ("CUSTOMER".equalsIgnoreCase(role)) {
                            int custId = rs.getInt("customer_id");
                            sb.append("\"id\":").append(custId).append(",");
                            sb.append("\"name\":\"").append(escapeJson(rs.getString("customer_name"))).append("\",");
                            sb.append("\"dob\":\"").append(rs.getDate("dob") != null ? rs.getDate("dob").toString() : "").append("\",");
                            sb.append("\"gender\":\"").append(escapeJson(rs.getString("gender"))).append("\",");
                            sb.append("\"phone\":\"").append(escapeJson(rs.getString("cust_phone"))).append("\",");
                            sb.append("\"email\":\"").append(escapeJson(rs.getString("cust_email"))).append("\",");
                            sb.append("\"address\":\"").append(escapeJson(rs.getString("address"))).append("\",");
                            sb.append("\"pan\":\"").append(escapeJson(rs.getString("pan_number"))).append("\"");

                            // Fetch customer accounts
                            String accSql = "SELECT a.account_number, t.type_name, a.balance, a.status FROM account a JOIN account_type t ON a.type_id = t.type_id WHERE a.customer_id = ?";
                            try (PreparedStatement psAcc = con.prepareStatement(accSql)) {
                                psAcc.setInt(1, custId);
                                ResultSet rsAcc = psAcc.executeQuery();
                                sb.append(",\"accounts\":[");
                                boolean first = true;
                                while (rsAcc.next()) {
                                    if (!first) sb.append(",");
                                    sb.append("{\"accountNumber\":\"").append(rsAcc.getString("account_number"))
                                      .append("\",\"type\":\"").append(rsAcc.getString("type_name"))
                                      .append("\",\"balance\":").append(rsAcc.getBigDecimal("balance"))
                                      .append(",\"status\":\"").append(rsAcc.getString("status")).append("\"}");
                                    first = false;
                                }
                                sb.append("]");
                            }
                        } else {
                            sb.append("\"id\":").append(rs.getInt("emp_id")).append(",");
                            sb.append("\"name\":\"").append(escapeJson(rs.getString("emp_name"))).append("\",");
                            sb.append("\"designation\":\"").append(escapeJson(rs.getString("emp_role"))).append("\",");
                            sb.append("\"salary\":").append(rs.getBigDecimal("salary")).append(",");
                            sb.append("\"phone\":\"").append(escapeJson(rs.getString("emp_phone"))).append("\",");
                            sb.append("\"email\":\"").append(escapeJson(rs.getString("emp_email"))).append("\",");
                            sb.append("\"hireDate\":\"").append(rs.getDate("hire_date") != null ? rs.getDate("hire_date").toString() : "").append("\"");
                        }
                        sb.append("}");
                        sendJson(exchange, 200, sb.toString());
                    } else {
                        sendJson(exchange, 404, "{\"success\":false,\"message\":\"User profile not found\"}");
                    }
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 12. User Profile Update Handler
    static class UserProfileUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String username = p.get("username");
            String phone = p.get("phone");
            String address = p.get("address");
            String email = p.get("email");

            if (username == null || phone == null || email == null) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Missing required profile fields\"}");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    String updCust = "UPDATE customer c JOIN login l ON c.customer_id = l.reference_id " +
                                     "SET c.phone = ?, c.address = COALESCE(?, c.address), c.email = ? " +
                                     "WHERE l.username = ? OR c.email = ?";
                    try (PreparedStatement ps = con.prepareStatement(updCust)) {
                        ps.setString(1, phone);
                        ps.setString(2, address);
                        ps.setString(3, email);
                        ps.setString(4, username);
                        ps.setString(5, username);
                        ps.executeUpdate();
                    }

                    String updEmp = "UPDATE employee e JOIN login l ON e.emp_id = l.reference_id " +
                                    "SET e.phone = ?, e.email = ? WHERE l.username = ? OR e.email = ?";
                    try (PreparedStatement ps = con.prepareStatement(updEmp)) {
                        ps.setString(1, phone);
                        ps.setString(2, email);
                        ps.setString(3, username);
                        ps.setString(4, username);
                        ps.executeUpdate();
                    }

                    con.commit();
                    sendJson(exchange, 200, "{\"success\":true,\"message\":\"Profile updated successfully in MySQL!\"}");
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // 13. Change Password Handler
    static class ChangePasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String username = p.get("username");
            String oldPassword = p.get("oldPassword");
            String newPassword = p.get("newPassword");

            if (username == null || oldPassword == null || newPassword == null || newPassword.trim().length() < 4) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"New password must be at least 4 characters\"}");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String sql = "UPDATE login SET password = ? WHERE (username = ? OR username = (SELECT email FROM customer WHERE customer_id = reference_id)) AND password = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, newPassword.trim());
                    ps.setString(2, username);
                    ps.setString(3, oldPassword.trim());
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        sendJson(exchange, 200, "{\"success\":true,\"message\":\"Password changed successfully in database!\"}");
                    } else {
                        sendJson(exchange, 400, "{\"success\":false,\"message\":\"Current password is incorrect\"}");
                    }
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // Helpers
    private static void setCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, b.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(b);
        }
    }

    /** Escapes double quotes/backslashes so error messages can't break the JSON response. */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Map<String, String> parseJsonOrForm(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        body = body.trim();

        if (body.startsWith("{") && body.endsWith("}")) {
            String inner = body.substring(1, body.length() - 1);
            String[] pairs = inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String p : pairs) {
                String[] kv = p.split(":", 2);
                if (kv.length == 2) {
                    String k = kv[0].trim().replace("\"", "");
                    String v = kv[1].trim().replace("\"", "");
                    map.put(k, v);
                }
            }
        } else {
            String[] pairs = body.split("&");
            for (String p : pairs) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
        }
        return map;
    }

    private static String getQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
