@echo off
echo Testing MySQL Connection for Bank Management System...
java -cp "backend\bin;backend\lib\mysql-connector-j-8.3.0.jar" com.bank.util.TestConnection
pause
