-- =======================================================
-- BANK MANAGEMENT SYSTEM - SAMPLE SEED DATA
-- Inserts realistic records into all 12 tables
-- Includes row-count verification query
-- =======================================================

USE bank_db;

-- 1. Insert Branches
INSERT INTO branch (branch_id, branch_code, branch_name, city, address, phone) VALUES
(1, 'BNK-MUM-01', 'Nariman Point Branch', 'Mumbai', '101 Marine Drive, Nariman Point', '022-22810001'),
(2, 'BNK-DEL-01', 'Connaught Place Branch', 'New Delhi', '45 Regal Building, CP', '011-23340002'),
(3, 'BNK-BLR-01', 'MG Road Branch', 'Bangalore', '12 Brigade Towers, MG Road', '080-25580003'),
(4, 'BNK-PUN-01', 'FC Road Branch', 'Pune', '88 Deccan Gymkhana, FC Road', '020-25670004');

-- 2. Insert Employees
INSERT INTO employee (emp_id, emp_name, role, salary, phone, email, branch_id, hire_date) VALUES
(101, 'Rajesh Khanna', 'BRANCH_MANAGER', 95000.00, '9820111222', 'rajesh.k@bank.com', 1, '2020-03-15'),
(102, 'Pooja Hegde', 'LOAN_OFFICER', 65000.00, '9820222333', 'pooja.h@bank.com', 1, '2021-07-10'),
(103, 'Suresh Menon', 'CASHIER', 45000.00, '9820333444', 'suresh.m@bank.com', 2, '2022-01-20'),
(104, 'Anita Sharma', 'CLERK', 38000.00, '9820444555', 'anita.s@bank.com', 3, '2023-05-12');

-- 3. Insert Customers
INSERT INTO customer (customer_id, customer_name, dob, gender, phone, email, address, pan_number, branch_id) VALUES
(1001, 'Aarav Shah', '1990-05-15', 'Male', '9820011111', 'aarav.shah@email.com', 'Flat 402, Sea View, Mumbai', 'ABCPS1234A', 1),
(1002, 'Sneha Kulkarni', '1985-08-20', 'Female', '9820022222', 'sneha.k@email.com', 'Plot 18, Sector 15, Vashi', 'ABCPS2345B', 1),
(1003, 'Rohan Desai', '2000-01-10', 'Male', '9820033333', 'rohan.d@email.com', '703 Green Valley, Thane', 'ABCPS3456C', 4),
(1004, 'Priya Joshi', '1995-06-12', 'Female', '9876500001', 'priya.j@email.com', 'B-12 Hill Road, Bandra, Mumbai', 'ABCPS4567D', 1);

-- 4. Insert Account Types
INSERT INTO account_type (type_id, type_name, min_balance, interest_rate) VALUES
(1, 'SAVINGS', 1000.00, 4.00),
(2, 'CURRENT', 5000.00, 0.00),
(3, 'FIXED_DEPOSIT', 10000.00, 7.50);

-- 5. Insert Accounts
INSERT INTO account (account_number, customer_id, type_id, branch_id, balance, status, opened_date) VALUES
('ACC100101', 1001, 1, 1, 85000.00, 'ACTIVE', '2022-04-10'),
('ACC100102', 1001, 3, 1, 150000.00, 'ACTIVE', '2023-01-15'),
('ACC100201', 1002, 1, 1, 42500.00, 'ACTIVE', '2021-09-20'),
('ACC100202', 1002, 2, 1, 120000.00, 'ACTIVE', '2022-11-05'),
('ACC100301', 1003, 1, 4, 18500.00, 'ACTIVE', '2024-02-01'),
('ACC100401', 1004, 1, 1, 55000.00, 'ACTIVE', '2023-08-14');

-- 6. Insert Transactions
INSERT INTO transaction (txn_id, account_number, txn_type, amount, resulting_balance, target_account, description, txn_date) VALUES
(5001, 'ACC100101', 'DEPOSIT', 50000.00, 50000.00, NULL, 'Initial opening deposit', '2022-04-10 10:30:00'),
(5002, 'ACC100101', 'DEPOSIT', 40000.00, 90000.00, NULL, 'Salary Credit - TechCorp', '2026-09-01 09:15:00'),
(5003, 'ACC100101', 'WITHDRAW', 5000.00, 85000.00, NULL, 'ATM Cash Withdrawal', '2026-09-05 14:20:00'),
(5004, 'ACC100201', 'DEPOSIT', 50000.00, 50000.00, NULL, 'Initial Deposit', '2021-09-20 11:00:00'),
(5005, 'ACC100201', 'TRANSFER', 7500.00, 42500.00, 'ACC100401', 'Fund transfer to Priya', '2026-09-10 16:45:00'),
(5006, 'ACC100401', 'DEPOSIT', 7500.00, 55000.00, 'ACC100201', 'Received transfer from Sneha', '2026-09-10 16:45:00');

-- 7. Insert Beneficiaries
INSERT INTO beneficiary (beneficiary_id, customer_id, beneficiary_name, account_number, bank_name, ifsc_code) VALUES
(201, 1001, 'Sneha Kulkarni', 'ACC100201', 'National Bank', 'BNK0000101'),
(202, 1002, 'Priya Joshi', 'ACC100401', 'National Bank', 'BNK0000101'),
(203, 1003, 'Aarav Shah', 'ACC100101', 'National Bank', 'BNK0000101');

-- 8. Insert Cards
INSERT INTO card (card_number, account_number, card_type, expiry_date, cvv, daily_limit, status) VALUES
('4532-1111-2222-3333', 'ACC100101', 'DEBIT', '2028-12-31', '421', 50000.00, 'ACTIVE'),
('5241-4444-5555-6666', 'ACC100201', 'DEBIT', '2027-06-30', '852', 50000.00, 'ACTIVE'),
('4111-7777-8888-9999', 'ACC100401', 'DEBIT', '2029-03-31', '369', 50000.00, 'ACTIVE');

-- 9. Insert Loan Types
INSERT INTO loan_type (loan_type_id, type_name, base_rate, max_tenure_months) VALUES
(1, 'HOME_LOAN', 8.50, 240),
(2, 'PERSONAL_LOAN', 11.50, 60),
(3, 'CAR_LOAN', 9.20, 84),
(4, 'EDUCATION_LOAN', 8.00, 120);

-- 10. Insert Loans
INSERT INTO loan (loan_id, customer_id, loan_type_id, branch_id, amount, interest_rate, tenure_months, status, applied_date, approved_by_emp_id) VALUES
(301, 1001, 1, 1, 2500000.00, 8.50, 180, 'APPROVED', '2024-01-10', 101),
(302, 1002, 3, 1, 600000.00, 9.20, 48, 'APPROVED', '2024-06-15', 102),
(303, 1004, 2, 1, 200000.00, 11.50, 24, 'PENDING', '2026-09-01', NULL);

-- 11. Insert Loan Payments
INSERT INTO loan_payment (payment_id, loan_id, amount_paid, payment_date, payment_mode, remaining_balance) VALUES
(401, 301, 24800.00, '2026-08-05 10:00:00', 'AUTO_DEBIT', 2350000.00),
(402, 301, 24800.00, '2026-09-05 10:00:00', 'AUTO_DEBIT', 2325200.00),
(403, 302, 15000.00, '2026-09-01 12:30:00', 'ONLINE', 540000.00);

-- 12. Insert Logins
INSERT INTO login (username, password, role, reference_id) VALUES
('customer@example.com', 'password123', 'CUSTOMER', 1001),
('employee@example.com', 'password123', 'EMPLOYEE', 102),
('admin@example.com', 'password123', 'ADMIN', 101),
('admin', 'admin123', 'ADMIN', 101),
('employee', 'employee123', 'EMPLOYEE', 102),
('aarav', 'password123', 'CUSTOMER', 1001),
('sneha', 'password123', 'CUSTOMER', 1002),
('priya', 'password123', 'CUSTOMER', 1004);

-- =======================================================
-- ROW COUNT VERIFICATION QUERY
-- (Matches manual Section 8.3 / Page 17)
-- =======================================================
SELECT 'BRANCH' AS table_name, COUNT(*) AS total FROM branch
UNION ALL SELECT 'EMPLOYEE', COUNT(*) FROM employee
UNION ALL SELECT 'CUSTOMER', COUNT(*) FROM customer
UNION ALL SELECT 'ACCOUNT_TYPE', COUNT(*) FROM account_type
UNION ALL SELECT 'ACCOUNT', COUNT(*) FROM account
UNION ALL SELECT 'TRANSACTION', COUNT(*) FROM transaction
UNION ALL SELECT 'BENEFICIARY', COUNT(*) FROM beneficiary
UNION ALL SELECT 'CARD', COUNT(*) FROM card
UNION ALL SELECT 'LOAN_TYPE', COUNT(*) FROM loan_type
UNION ALL SELECT 'LOAN', COUNT(*) FROM loan
UNION ALL SELECT 'LOAN_PAYMENT', COUNT(*) FROM loan_payment
UNION ALL SELECT 'LOGIN', COUNT(*) FROM login;
