package com.bank.service;

import com.bank.model.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds demo users, passwords and a few accounts in memory.
 * The GUI uses this to perform a fake login and to obtain a pre‑populated AccountService.
 */
public class InMemoryData {
    public static final AccountService accountService = new AccountService();
    public static final Map<String, UserCredentials> credentials = new HashMap<>();

    static {
        // Demo accounts
        Account savings = new SavingsAccount("SAV001", new BigDecimal("50000"));
        Account current = new CurrentAccount("CUR001", new BigDecimal("25000"));
        Account fd = new FixedDepositAccount("FD001", new BigDecimal("100000"));

        // Register them so the service can find them by number
        accountService.register(savings);
        accountService.register(current);
        accountService.register(fd);

        // Demo users – role, password, and a primary account to display on the dashboard
        credentials.put("customer@example.com", new UserCredentials("password123", "customer", savings));
        credentials.put("employee@example.com", new UserCredentials("password123", "employee", current));
        credentials.put("admin@example.com",    new UserCredentials("password123", "admin",    fd));
    }

    /** Simple holder for login details */
    public static class UserCredentials {
        public final String password;
        public final String role;
        public final Account primaryAccount;
        public UserCredentials(String password, String role, Account primaryAccount) {
            this.password = password;
            this.role = role;
            this.primaryAccount = primaryAccount;
        }
    }
}
