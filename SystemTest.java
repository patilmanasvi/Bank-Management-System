import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SystemTest - End-to-end and API test harness for the Bank Management System.
 *
 * Covers:
 *  1. End-to-end Testing: login, account details, deposit, withdraw, transfer,
 *     transaction history - and confirms the balance/history reflect each change,
 *     i.e. that the database actually updated correctly.
 *  2. API Testing & Bug Fixing: checks status codes and response shape for both
 *     valid and invalid input (missing fields, bad numbers, insufficient funds).
 *
 * HOW TO RUN:
 *  1. Start the bank server first (run.bat / start_website.bat, or run
 *     BankWebServer.main() directly) so it's listening on http://localhost:8080.
 *  2. Compile and run this file separately, e.g.:
 *       javac SystemTest.java
 *       java SystemTest
 *  3. Read the PASS/FAIL summary printed at the end.
 *
 * This uses only java.net.http (built into the JDK) - no extra dependencies.
 * Uses seeded accounts from the sample data: ACC100101 (Aarav) and ACC100401 (Priya).
 */
public class SystemTest {
    private static final String BASE = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final AtomicInteger passed = new AtomicInteger(0);
    private static final AtomicInteger failed = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        System.out.println("=======================================================");
        System.out.println(" BANK MANAGEMENT SYSTEM - END-TO-END & API TEST SUITE");
        System.out.println("=======================================================\n");

        testLogin();
        testAccountDetails();
        testDepositAndVerifyBalance();
        testWithdrawAndVerifyBalance();
        testWithdrawInsufficientFunds();
        testTransferAndVerifyBothBalances();
        testTransactionHistoryReflectsChanges();
        testTransactionHistoryFilters();

        // API bug-fixing checks: malformed / missing input should fail cleanly, not crash
        testDepositMissingFields();
        testDepositInvalidAmountFormat();
        testDepositNegativeAmount();

        System.out.println("\n=======================================================");
        System.out.println(" RESULTS: " + passed.get() + " passed, " + failed.get() + " failed");
        System.out.println("=======================================================");
    }

    // ---------- End-to-end tests ----------

    static void testLogin() throws Exception {
        String body = "{\"username\":\"aarav\",\"password\":\"password123\"}";
        HttpResponse<String> res = post("/api/auth/login", body);
        check("Login with valid credentials returns 200", res.statusCode() == 200);
        check("Login response reports success:true", res.body().contains("\"success\":true"));
    }

    static void testAccountDetails() throws Exception {
        HttpResponse<String> res = get("/api/account/details?acc=ACC100101");
        check("Account details returns 200", res.statusCode() == 200);
        check("Account details includes account number", res.body().contains("ACC100101"));
    }

    static void testDepositAndVerifyBalance() throws Exception {
        BigDecimalHolder before = getBalance("ACC100101");
        HttpResponse<String> res = post("/api/transactions/deposit",
                "{\"accountNumber\":\"ACC100101\",\"amount\":\"500.00\"}");
        check("Deposit returns 200", res.statusCode() == 200);

        BigDecimalHolder after = getBalance("ACC100101");
        boolean correct = after.value.subtract(before.value).compareTo(new java.math.BigDecimal("500.00")) == 0;
        check("Balance increased by exactly the deposited amount (DB updated correctly)", correct);
    }

    static void testWithdrawAndVerifyBalance() throws Exception {
        BigDecimalHolder before = getBalance("ACC100101");
        HttpResponse<String> res = post("/api/transactions/withdraw",
                "{\"accountNumber\":\"ACC100101\",\"amount\":\"200.00\"}");
        check("Withdraw returns 200", res.statusCode() == 200);

        BigDecimalHolder after = getBalance("ACC100101");
        boolean correct = before.value.subtract(after.value).compareTo(new java.math.BigDecimal("200.00")) == 0;
        check("Balance decreased by exactly the withdrawn amount (DB updated correctly)", correct);
    }

    static void testWithdrawInsufficientFunds() throws Exception {
        HttpResponse<String> res = post("/api/transactions/withdraw",
                "{\"accountNumber\":\"ACC100101\",\"amount\":\"99999999.00\"}");
        check("Withdrawing far more than balance is rejected (not 200)", res.statusCode() != 200);
        check("Insufficient funds error message is returned", res.body().toLowerCase().contains("insufficient"));
    }

    static void testTransferAndVerifyBothBalances() throws Exception {
        BigDecimalHolder fromBefore = getBalance("ACC100101");
        BigDecimalHolder toBefore = getBalance("ACC100401");

        HttpResponse<String> res = post("/api/transactions/transfer",
                "{\"fromAccount\":\"ACC100101\",\"toAccount\":\"ACC100401\",\"amount\":\"300.00\",\"remarks\":\"SystemTest\"}");
        check("Transfer returns 200", res.statusCode() == 200);

        BigDecimalHolder fromAfter = getBalance("ACC100101");
        BigDecimalHolder toAfter = getBalance("ACC100401");

        boolean debited = fromBefore.value.subtract(fromAfter.value).compareTo(new java.math.BigDecimal("300.00")) == 0;
        boolean credited = toAfter.value.subtract(toBefore.value).compareTo(new java.math.BigDecimal("300.00")) == 0;
        check("Sender balance decreased by transferred amount", debited);
        check("Receiver balance increased by transferred amount (atomic, both sides consistent)", credited);
    }

    static void testTransactionHistoryReflectsChanges() throws Exception {
        HttpResponse<String> res = get("/api/transactions/history?acc=ACC100101");
        check("Transaction history returns 200", res.statusCode() == 200);
        check("History includes at least one TRANSFER entry after the test transfer",
                res.body().contains("\"type\":\"TRANSFER\""));
        check("History includes at least one DEPOSIT entry after the test deposit",
                res.body().contains("\"type\":\"DEPOSIT\""));
    }

    static void testTransactionHistoryFilters() throws Exception {
        HttpResponse<String> res = get("/api/transactions/history?acc=ACC100101&type=WITHDRAW");
        check("Filtered history (type=WITHDRAW) returns 200", res.statusCode() == 200);
        check("Filtered history contains only WITHDRAW entries",
                !res.body().replace("\"type\":\"WITHDRAW\"", "").contains("\"type\":\""));
    }

    // ---------- API bug-fixing checks ----------

    static void testDepositMissingFields() throws Exception {
        HttpResponse<String> res = post("/api/transactions/deposit", "{\"accountNumber\":\"ACC100101\"}");
        check("Deposit with missing amount is rejected with 400 (not a raw 500 crash)", res.statusCode() == 400);
    }

    static void testDepositInvalidAmountFormat() throws Exception {
        HttpResponse<String> res = post("/api/transactions/deposit",
                "{\"accountNumber\":\"ACC100101\",\"amount\":\"not-a-number\"}");
        check("Deposit with non-numeric amount is rejected with 400 (not a raw 500 crash)", res.statusCode() == 400);
        check("Error message explains the amount is invalid", res.body().toLowerCase().contains("number"));
    }

    static void testDepositNegativeAmount() throws Exception {
        HttpResponse<String> res = post("/api/transactions/deposit",
                "{\"accountNumber\":\"ACC100101\",\"amount\":\"-50.00\"}");
        check("Depositing a negative amount is rejected", res.statusCode() != 200);
    }

    // ---------- Helpers ----------

    static HttpResponse<String> post(String path, String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    static HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .GET()
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    static BigDecimalHolder getBalance(String accountNumber) throws Exception {
        HttpResponse<String> res = get("/api/account/details?acc=" + accountNumber);
        String body = res.body();
        int i = body.indexOf("\"balance\":");
        int start = i + "\"balance\":".length();
        int end = body.indexOf(",", start);
        if (end == -1) end = body.indexOf("}", start);
        String num = body.substring(start, end).trim();
        return new BigDecimalHolder(new java.math.BigDecimal(num));
    }

    static class BigDecimalHolder {
        final java.math.BigDecimal value;
        BigDecimalHolder(java.math.BigDecimal v) { this.value = v; }
    }

    static void check(String description, boolean condition) {
        if (condition) {
            passed.incrementAndGet();
            System.out.println("  [PASS] " + description);
        } else {
            failed.incrementAndGet();
            System.out.println("  [FAIL] " + description);
        }
    }
}
