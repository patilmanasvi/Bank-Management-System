-- =======================================================
-- BANK MANAGEMENT SYSTEM - DATABASE SCHEMA (3NF)
-- MySQL 8.0 DDL Build Script
-- 12 Normalized Tables with Integrity Constraints
-- =======================================================

DROP DATABASE IF EXISTS bank_db;
CREATE DATABASE bank_db;
USE bank_db;

-- 1. BRANCH (Master Table: Bank Branches)
CREATE TABLE branch (
    branch_id INT AUTO_INCREMENT,
    branch_code VARCHAR(20) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    address VARCHAR(200) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    CONSTRAINT pk_branch PRIMARY KEY (branch_id),
    CONSTRAINT uq_branch_code UNIQUE (branch_code)
);

-- 2. EMPLOYEE (Bank Staff)
CREATE TABLE employee (
    emp_id INT AUTO_INCREMENT,
    emp_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    salary DECIMAL(10, 2) NOT NULL,
    phone VARCHAR(15),
    email VARCHAR(100) NOT NULL,
    branch_id INT NOT NULL,
    hire_date DATE NOT NULL,
    CONSTRAINT pk_employee PRIMARY KEY (emp_id),
    CONSTRAINT uq_employee_email UNIQUE (email),
    CONSTRAINT fk_employee_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id),
    CONSTRAINT chk_employee_salary CHECK (salary > 0)
);

-- 3. CUSTOMER (Customer Master Profile)
CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    pan_number VARCHAR(20) NOT NULL,
    branch_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_customer PRIMARY KEY (customer_id),
    CONSTRAINT uq_customer_email UNIQUE (email),
    CONSTRAINT uq_customer_pan UNIQUE (pan_number),
    CONSTRAINT fk_customer_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id),
    CONSTRAINT chk_customer_gender CHECK (gender IN ('Male', 'Female', 'Other'))
);

-- 4. ACCOUNT_TYPE (Lookup Table: Savings, Current, Fixed Deposit)
CREATE TABLE account_type (
    type_id INT AUTO_INCREMENT,
    type_name VARCHAR(50) NOT NULL,
    min_balance DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    CONSTRAINT pk_account_type PRIMARY KEY (type_id),
    CONSTRAINT uq_account_type_name UNIQUE (type_name)
);

-- 5. ACCOUNT (Core Financial Account)
CREATE TABLE account (
    account_number VARCHAR(20),
    customer_id INT NOT NULL,
    type_id INT NOT NULL,
    branch_id INT NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    opened_date DATE NOT NULL,
    CONSTRAINT pk_account PRIMARY KEY (account_number),
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE RESTRICT,
    CONSTRAINT fk_account_type FOREIGN KEY (type_id) REFERENCES account_type(type_id),
    CONSTRAINT fk_account_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'DORMANT', 'CLOSED')),
    CONSTRAINT chk_account_balance CHECK (balance >= 0.00)
);

-- 6. TRANSACTION (Deposit, Withdrawal, Transfer Audit Trail)
CREATE TABLE transaction (
    txn_id INT AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL,
    txn_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    resulting_balance DECIMAL(15, 2) NOT NULL,
    target_account VARCHAR(20),
    description VARCHAR(255),
    txn_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_transaction PRIMARY KEY (txn_id),
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_number) REFERENCES account(account_number),
    CONSTRAINT chk_transaction_type CHECK (txn_type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')),
    CONSTRAINT chk_transaction_amount CHECK (amount > 0.00)
);

-- 7. BENEFICIARY (Registered Payees for Transfers)
CREATE TABLE beneficiary (
    beneficiary_id INT AUTO_INCREMENT,
    customer_id INT NOT NULL,
    beneficiary_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    ifsc_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_beneficiary PRIMARY KEY (beneficiary_id),
    CONSTRAINT fk_beneficiary_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

-- 8. CARD (Debit and Credit Cards Linked to Accounts)
CREATE TABLE card (
    card_number VARCHAR(20),
    account_number VARCHAR(20) NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(5) NOT NULL,
    daily_limit DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    CONSTRAINT pk_card PRIMARY KEY (card_number),
    CONSTRAINT fk_card_account FOREIGN KEY (account_number) REFERENCES account(account_number),
    CONSTRAINT chk_card_type CHECK (card_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_card_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED'))
);

-- 9. LOAN_TYPE (Lookup: Home, Personal, Education, Car Loans)
CREATE TABLE loan_type (
    loan_type_id INT AUTO_INCREMENT,
    type_name VARCHAR(50) NOT NULL,
    base_rate DECIMAL(5, 2) NOT NULL,
    max_tenure_months INT NOT NULL,
    CONSTRAINT pk_loan_type PRIMARY KEY (loan_type_id),
    CONSTRAINT uq_loan_type_name UNIQUE (type_name)
);

-- 10. LOAN (Loan Applications & Approvals)
CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT,
    customer_id INT NOT NULL,
    loan_type_id INT NOT NULL,
    branch_id INT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    tenure_months INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    applied_date DATE NOT NULL,
    approved_by_emp_id INT,
    CONSTRAINT pk_loan PRIMARY KEY (loan_id),
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_loan_type FOREIGN KEY (loan_type_id) REFERENCES loan_type(loan_type_id),
    CONSTRAINT fk_loan_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id),
    CONSTRAINT fk_loan_employee FOREIGN KEY (approved_by_emp_id) REFERENCES employee(emp_id),
    CONSTRAINT chk_loan_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_loan_amount CHECK (amount > 0.00)
);

-- 11. LOAN_PAYMENT (EMI Repayments)
CREATE TABLE loan_payment (
    payment_id INT AUTO_INCREMENT,
    loan_id INT NOT NULL,
    amount_paid DECIMAL(15, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_mode VARCHAR(20) DEFAULT 'ONLINE',
    remaining_balance DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_loan_payment PRIMARY KEY (payment_id),
    CONSTRAINT fk_loan_payment_loan FOREIGN KEY (loan_id) REFERENCES loan(loan_id),
    CONSTRAINT chk_payment_mode CHECK (payment_mode IN ('CASH', 'ONLINE', 'CHEQUE', 'AUTO_DEBIT')),
    CONSTRAINT chk_payment_amount CHECK (amount_paid > 0.00)
);

-- 12. LOGIN (System Credentials with Role-Based Access)
CREATE TABLE login (
    username VARCHAR(50),
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    reference_id INT,
    CONSTRAINT pk_login PRIMARY KEY (username),
    CONSTRAINT chk_login_role CHECK (role IN ('ADMIN', 'EMPLOYEE', 'CUSTOMER'))
);
