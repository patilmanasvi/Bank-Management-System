# Bank Management System

An enterprise-grade, full-stack banking web application and desktop management system built with **Java (OOP)**, **MySQL (RDBMS 3NF)**, and a **Vanilla HTML5/CSS3/JavaScript** frontend.

---

## Architecture Overview

```
+--------------------------------------------------------------------+
|                         PRESENTATION LAYER                         |
|  - Modern Web Portal (HTML5, CSS3, ES6 JavaScript)                 |
|  - Desktop GUI (Java Swing & AWT)                                  |
+---------------------------------+----------------------------------+
                                  | REST JSON API / HTTP / JDBC
+---------------------------------v----------------------------------+
|                      APPLICATION / BUSINESS LAYER                  |
|  - Web Server: Embedded com.sun.net.httpserver.HttpServer (Port 8080)
|  - Domain Models (com.bank.model): Person, Customer, Employee,      |
|    Account (Savings, Current, Fixed Deposit), Transaction          |
|  - Business Services (com.bank.service): TransactionService (ACID), |
|    AccountService, LoanService                                     |
+---------------------------------+----------------------------------+
                                  | JDBC (mysql-connector-j-8.3.0.jar)
+---------------------------------v----------------------------------+
|                          DATABASE LAYER                            |
|  - MySQL 8.0 Server (InnoDB Storage Engine)                        |
|  - 12 Normalized Relational Tables (Third Normal Form - 3NF)        |
|  - ACID Compliant Transactions & Strict Referential Integrity       |
+--------------------------------------------------------------------+
```

---

## Key Features

### 1. Customer Banking Portal
- **Dashboard:** Live balance card, quick-action shortcuts, recent 5 transactions mini-ledger.
- **Deposit & Withdrawal:** Immediate account balance updates with non-negative validation.
- **Atomic Fund Transfer (ACID):** Inter-account transfers executed inside a single database transaction with automatic rollback on failure.
- **Transaction Audit History:** Multi-parameter search filtered by Date Range and Transaction Type (Credit/Debit/All).
- **Open New Account:** Instant self-service account opening for Savings (4%), Current (Overdraft), or Fixed Deposit (7.5%).
- **Loan Applications:** Apply for Personal, Home, Car, or Education loans with custom tenure and interest calculations.
- **KYC & Profile Management:** View official bank KYC records (PAN, DOB, Branch), update contact details (Phone, Email, Address), and change account passwords.

### 2. Staff & Admin Control Center
- **Customer Accounts Directory:** Real-time visibility into all customer accounts, balances, and operational statuses.
- **Account Freeze / Activation:** Instantly freeze compromised accounts to block all transactions, or reactivate them.
- **Loan Approvals:** Evaluate customer loan applications and issue approvals or rejections with real-time database updates.
- **Audit Logging:** Comprehensive inspection of system-wide credit, debit, and transfer logs.

### 3. Desktop Java Swing GUI
- Dual-interface support: Native Java Swing GUI for offline/counter operations (`LoginFrame.java`, `Dashboard.java`, `CustomerForm.java`, `TransactionForm.java`).

---

## Object-Oriented Programming (OOP) Implementation

- **Encapsulation:** All financial properties (`balance`, `accountNumber`) are marked `protected`/`private` with synchronized mutators and accessors in [`Account.java`](backend/src/main/java/com/bank/model/Account.java). Financial amounts are calculated using `BigDecimal` to eliminate floating-point rounding errors.
- **Inheritance:**
  - `Person` -> extended by `Customer` and `Employee`.
  - `Account` -> extended by `SavingsAccount`, `CurrentAccount`, and `FixedDepositAccount`.
- **Polymorphism:** Method overriding on `withdraw()` (e.g., overdraft support in `CurrentAccount`, minimum balance check in `SavingsAccount`, lock-in enforcement in `FixedDepositAccount`) and `calculateInterest()`.
- **Abstraction:** Abstract base class `Account` forces concrete subtypes to implement their specific financial rules.
- **Thread Safety:** Synchronized transaction blocks prevent race conditions during concurrent account operations.

---

## Database Schema (3NF Normalization)

The database schema is fully normalized to **Third Normal Form (3NF)** across 12 relational tables in [`database/schema.sql`](database/schema.sql):

1. **`branch`**: Branch details, locations, and IFSC codes.
2. **`customer`**: Verified customer master KYC records.
3. **`employee`**: Bank personnel and designations.
4. **`account_type`**: Account classifications and default interest rates.
5. **`account`**: Customer master accounts and live balances.
6. **`transaction`**: Immutable ledger of all deposits, withdrawals, and transfers.
7. **`beneficiary`**: Saved payee accounts.
8. **`card`**: Debit and credit card mappings.
9. **`loan_type`**: Loan categories, interest rates, and max tenure.
10. **`loan`**: Applied loan applications and status tracking (`PENDING`, `APPROVED`, `REJECTED`).
11. **`loan_payment`**: EMI repayment ledger.
12. **`login`**: Role-based authentication credentials (`CUSTOMER`, `EMPLOYEE`, `ADMIN`).

---

## Project Directory Structure

```
Bank-Management-System/
├── backend/
│   ├── bin/                          # Compiled Java bytecode (.class files)
│   ├── lib/                          # External dependencies
│   │   └── mysql-connector-j-8.3.0.jar
│   └── src/main/java/com/bank/
│       ├── model/                    # OOP Domain Models
│       │   ├── Person.java
│       │   ├── Customer.java
│       │   ├── Employee.java
│       │   ├── Account.java
│       │   ├── SavingsAccount.java
│       │   ├── CurrentAccount.java
│       │   ├── FixedDepositAccount.java
│       │   └── Transaction.java
│       ├── service/                  # Business Logic & ACID Services
│       │   ├── AccountService.java
│       │   ├── TransactionService.java
│       │   ├── LoanService.java
│       │   └── InMemoryData.java
│       ├── ui/                       # Web Server & Desktop GUI
│       │   ├── BankWebServer.java    # REST API & Static File Server
│       │   ├── Main.java             # Entry point for Swing GUI
│       │   ├── LoginFrame.java
│       │   ├── Dashboard.java
│       │   ├── CustomerForm.java
│       │   ├── TransactionForm.java
│       │   └── UIApp.java
│       └── util/                     # Database & Utilities
│           ├── DBConnection.java     # JDBC Connection Factory
│           ├── TestConnection.java
│           └── TestACIDTransfer.java
├── database/
│   ├── schema.sql                    # 12 Normalized 3NF Relational Tables
│   ├── data.sql                      # Initial seed data & demo records
│   └── schema_updates.sql            # FROZEN account status constraint patch
├── frontend/
│   ├── style.css                     # Global design system & animations
│   ├── app.js                        # Frontend Controller & REST connector
│   ├── index.html                    # Auto-redirect to login
│   ├── login.html                    # User & Staff Authentication
│   ├── signup.html                   # Account Registration
│   ├── dashboard.html                # Customer Dashboard & Quick Actions
│   ├── deposit.html                  # Cash Deposit Form
│   ├── withdraw.html                 # Cash Withdrawal Form
│   ├── transfer.html                 # ACID Fund Transfer Form
│   ├── transactions.html             # Audit Ledger with Date/Type Filters
│   ├── loans.html                    # Loan Application & Approval Portal
│   ├── open_account.html             # Instant Sub-Account Opening
│   ├── profile.html                  # KYC & Password Management
│   └── admin.html                    # Staff Accounts & Freeze/Activate Panel
├── SystemTest.java                   # 22-Step Automated E2E & API Test Suite
├── start_website.bat                 # One-click script to start web server
├── run_tests.bat                     # One-click script to run test suite
├── run.bat                           # One-click script to start Swing GUI
├── test_db.bat                       # One-click script to test MySQL connection
├── view_tables.bat                   # One-click script to inspect database tables
├── .gitignore                        # Git exclusion rules
└── README.md                         # Project documentation
```

---

## Getting Started & Setup

### Prerequisites
- **Java Development Kit (JDK 11+)**
- **MySQL Server 8.0+** running on `localhost:3306`

### 1. Database Setup
Import the schema and sample seed data into MySQL:
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS bank_db;"
mysql -u root -p bank_db < database/schema.sql
mysql -u root -p bank_db < database/data.sql
mysql -u root -p bank_db < database/schema_updates.sql
```

### 2. Running the Web Application
Double-click `start_website.bat` or run via terminal:
```bash
# Compile backend
javac -cp "backend/lib/mysql-connector-j-8.3.0.jar" -d backend/bin $(find backend/src -name "*.java")

# Start server
java -cp "backend/bin;backend/lib/mysql-connector-j-8.3.0.jar" com.bank.ui.BankWebServer
```
Open your browser and navigate to: **`http://localhost:8080/login.html`**

### 3. Running Automated End-to-End Tests
Double-click `run_tests.bat` or run:
```bash
javac SystemTest.java
java SystemTest
```
*Executes all 22 end-to-end and API verification tests (100% pass).*

---

## Demo Credentials

| Role | Email / Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@example.com` / `aarav` | `password123` | Dashboard, Deposit, Withdraw, Transfer, History, KYC, Loans, Open Account |
| **Employee** | `employee@example.com` / `vikram` | `password123` | Counter Services, Audit Transactions, Loan Approvals |
| **Admin** | `admin@example.com` / `neha` | `password123` | Full Manager Control, Accounts Directory, Freeze/Activate Accounts, Loan Decisions |

---

## REST API Endpoints Reference

| Endpoint | Method | Role | Description |
| :--- | :--- | :--- | :--- |
| `/api/auth/login` | `POST` | Public | Authenticates credentials and returns user profile session. |
| `/api/account/details` | `GET` | Customer/Staff | Retrieves live balance and account metadata (`?acc=...`). |
| `/api/account/deposit` | `POST` | Customer/Staff | Credits funds to an active account. |
| `/api/account/withdraw` | `POST` | Customer/Staff | Debits funds with non-negative balance checks. |
| `/api/account/open` | `POST` | Customer | Opens a new Savings, Current, or Fixed Deposit account. |
| `/api/transactions/transfer` | `POST` | Customer | Performs an atomic ACID fund transfer between accounts. |
| `/api/transactions/history` | `GET` | Customer/Staff | Fetches transaction audit ledger with date and type filters. |
| `/api/admin/accounts` | `GET` | Staff/Admin | Lists all customer accounts, balances, and statuses. |
| `/api/admin/account/status` | `POST` | Admin | Updates account status (`ACTIVE` or `FROZEN`). |
| `/api/loans/apply` | `POST` | Customer | Submits a new loan application. |
| `/api/loans/list` | `GET` | Customer/Staff | Lists loan applications with optional status filtering. |
| `/api/loans/decision` | `POST` | Staff/Admin | Approves or rejects a pending loan. |
| `/api/user/profile` | `GET` | Customer/Staff | Retrieves user KYC information and linked accounts. |
| `/api/user/profile/update` | `POST` | Customer/Staff | Updates contact information (phone, email, address). |
| `/api/user/password/change`| `POST` | Customer/Staff | Verifies old password and updates credentials. |