@echo off
echo ===================================================
echo   STARTING BANK MANAGEMENT SYSTEM WEB SERVER...
echo ===================================================
echo.
echo Server running at: http://localhost:8080/
echo Opening browser...
start http://localhost:8080/login.html
java -cp "backend\bin;backend\lib\mysql-connector-j-8.3.0.jar" com.bank.ui.BankWebServer
pause
