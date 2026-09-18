package com.bank.ui;

import com.bank.service.AccountService;
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
import java.util.HashMap;
import java.util.Map;

/**
 * BankWebServer - Embedded HTTP & REST API Server
 * Serves the frontend HTML/CSS pages and handles JSON/form API calls connecting to MySQL.
 */
public class BankWebServer {
    private static final int PORT = 8080;
    private static final File FRONTEND_DIR = new File("frontend");
    private static final AccountService accountService = new AccountService();
    private static final TransactionService txnService = new TransactionService();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Endpoints
        server.createContext("/api/auth/login", new LoginApiHandler());
        server.createContext("/api/auth/signup", new SignupApiHandler());
        server.createContext("/api/account/details", new AccountDetailsHandler());
        server.createContext("/api/transactions/deposit", new DepositHandler());
        server.createContext("/api/transactions/withdraw", new WithdrawHandler());
        server.createContext("/api/transactions/transfer", new TransferHandler());
        server.createContext("/api/transactions/history", new HistoryHandler());

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

    // 1. Login Handler
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
                sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
            }
        }
    }

    // 2. Signup Handler
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
                    // 1. Insert customer
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

                    // 2. Insert login
                    String loginSql = "INSERT INTO login (username, password, role, reference_id) VALUES (?, ?, 'CUSTOMER', ?)";
                    try (PreparedStatement ps = con.prepareStatement(loginSql)) {
                        ps.setString(1, email);
                        ps.setString(2, password);
                        ps.setInt(3, custId);
                        ps.executeUpdate();
                    }

                    // 3. Insert account
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
                sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
            }
        }
    }

    // 3. Account Details Handler
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
                sendJson(exchange, 500, "{\"error\":\"" + ex.getMessage() + "\"}");
            }
        }
    }

    // 4. Deposit Handler
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String accNo = p.get("accountNumber");
            BigDecimal amt = new BigDecimal(p.get("amount"));

            try {
                accountService.deposit(accNo, amt);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Deposit successful\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
            }
        }
    }

    // 5. Withdraw Handler
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String accNo = p.get("accountNumber");
            BigDecimal amt = new BigDecimal(p.get("amount"));

            try {
                accountService.withdraw(accNo, amt);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Withdrawal successful\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
            }
        }
    }

    // 6. Transfer Handler (ACID)
    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            Map<String, String> p = parseJsonOrForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String fromAcc = p.get("fromAccount");
            String toAcc = p.get("toAccount");
            BigDecimal amt = new BigDecimal(p.get("amount"));
            String remarks = p.getOrDefault("remarks", "Web Fund Transfer");

            try {
                boolean ok = txnService.transferFunds(fromAcc, toAcc, amt, remarks);
                if (ok) {
                    sendJson(exchange, 200, "{\"success\":true,\"message\":\"Atomic Fund Transfer Completed Successfully!\"}");
                } else {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"Transfer failed.\"}");
                }
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
            }
        }
    }

    // 7. Transaction History Handler
    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            String query = exchange.getRequestURI().getQuery();
            String accNo = getQueryParam(query, "acc");
            if (accNo == null) accNo = "ACC100101";

            String sql = "SELECT txn_id, txn_date, txn_type, amount, target_account, resulting_balance, description " +
                         "FROM transaction WHERE account_number = ? OR target_account = ? ORDER BY txn_date DESC";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, accNo);
                ps.setString(2, accNo);
                ResultSet rs = ps.executeQuery();
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append(String.format("{\"id\":%d,\"date\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"target\":\"%s\",\"balance\":%.2f,\"desc\":\"%s\"}",
                            rs.getInt("txn_id"), rs.getString("txn_date"), rs.getString("txn_type"), rs.getBigDecimal("amount"),
                            rs.getString("target_account") != null ? rs.getString("target_account") : "-",
                            rs.getBigDecimal("resulting_balance"), rs.getString("description")));
                    first = false;
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + ex.getMessage() + "\"}");
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

    private static Map<String, String> parseJsonOrForm(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        body = body.trim();

        // Simple JSON parse
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
            // Form URL encoded parse
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
