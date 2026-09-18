@echo off
echo ===================================================
echo     BANK MANAGEMENT SYSTEM - DATABASE TABLES
echo ===================================================
echo.
echo [1] LISTING ALL 12 TABLES IN bank_db:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pSql_nishu11 -e "USE bank_db; SHOW TABLES;"

echo.
echo [2] TABLE ROW COUNTS:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pSql_nishu11 -e "USE bank_db; SELECT 'BRANCH' AS table_name, COUNT(*) AS total FROM branch UNION ALL SELECT 'EMPLOYEE', COUNT(*) FROM employee UNION ALL SELECT 'CUSTOMER', COUNT(*) FROM customer UNION ALL SELECT 'ACCOUNT_TYPE', COUNT(*) FROM account_type UNION ALL SELECT 'ACCOUNT', COUNT(*) FROM account UNION ALL SELECT 'TRANSACTION', COUNT(*) FROM transaction UNION ALL SELECT 'BENEFICIARY', COUNT(*) FROM beneficiary UNION ALL SELECT 'CARD', COUNT(*) FROM card UNION ALL SELECT 'LOAN_TYPE', COUNT(*) FROM loan_type UNION ALL SELECT 'LOAN', COUNT(*) FROM loan UNION ALL SELECT 'LOAN_PAYMENT', COUNT(*) FROM loan_payment UNION ALL SELECT 'LOGIN', COUNT(*) FROM login;"

echo.
echo [3] SAMPLE ACCOUNTS AND BALANCES:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pSql_nishu11 -e "USE bank_db; SELECT a.account_number, c.customer_name, t.type_name, a.balance, a.status FROM account a JOIN customer c ON a.customer_id = c.customer_id JOIN account_type t ON a.type_id = t.type_id;"

echo.
echo [4] SAMPLE CUSTOMER MASTER DATA:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pSql_nishu11 -e "USE bank_db; SELECT customer_id, customer_name, phone, email, pan_number FROM customer;"

echo.
echo [5] LOGIN CREDENTIALS:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pSql_nishu11 -e "USE bank_db; SELECT username, password, role FROM login;"

echo.
echo ===================================================
echo To open interactive MySQL Command Line, run:
echo mysql -u root -pSql_nishu11 bank_db
echo ===================================================
pause
