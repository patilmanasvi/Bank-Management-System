package com.bank.ui;

import com.bank.model.Account;
import com.bank.service.InMemoryData;
import com.bank.service.AccountService;
import com.bank.service.TransactionService;
import com.bank.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Simple Swing GUI that demonstrates the OOP model.
 * - Login with the demo credentials shown on the HTML page.
 * - After login, shows account balance and a list of recent transactions.
 * - Deposit / Withdraw / Transfer actions record a Transaction via TransactionService.
 */
public class UIApp {
    private final JFrame frame = new JFrame("Bank – Demo GUI");
    private final CardLayout cards = new CardLayout();
    private final JPanel mainPanel = new JPanel(cards);

    // Services shared across the app
    private final AccountService accountService = InMemoryData.accountService;
    private final TransactionService txnService = new TransactionService();
    private Account loggedInAccount;
    private String loggedInRole;

    public UIApp() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        mainPanel.add(buildLoginPanel(), "login");
        mainPanel.add(buildDashboardPanel(), "dashboard");

        frame.add(mainPanel);
        cards.show(mainPanel, "login");
        frame.setVisible(true);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(20);
        panel.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JButton loginBtn = new JButton("Sign In");
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener((ActionEvent e) -> {
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());
            var cred = InMemoryData.credentials.get(email);
            if (cred != null && cred.password.equals(pass)) {
                loggedInAccount = cred.primaryAccount;
                loggedInRole = cred.role;
                refreshDashboard();
                cards.show(mainPanel, "dashboard");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    // Components that need to be refreshed
    private JLabel balanceLabel;
    private JTable txnTable;
    private TransactionTableModel txnTableModel;

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // Top bar with welcome and logout
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel();
        topBar.add(welcome, BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout");
        topBar.add(logoutBtn, BorderLayout.EAST);
        logoutBtn.addActionListener(e -> {
            loggedInAccount = null;
            loggedInRole = null;
            cards.show(mainPanel, "login");
        });
        panel.add(topBar, BorderLayout.NORTH);

        // Center: balance and transaction table
        JPanel center = new JPanel(new BorderLayout());
        balanceLabel = new JLabel();
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        center.add(balanceLabel, BorderLayout.NORTH);

        txnTableModel = new TransactionTableModel();
        txnTable = new JTable(txnTableModel);
        JScrollPane scroll = new JScrollPane(txnTable);
        center.add(scroll, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        // Bottom: actions
        JPanel actions = new JPanel();
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton transferBtn = new JButton("Transfer");
        actions.add(depositBtn);
        actions.add(withdrawBtn);
        actions.add(transferBtn);
        panel.add(actions, BorderLayout.SOUTH);

        depositBtn.addActionListener(e -> showAmountDialog("Deposit", (amt) -> {
            accountService.deposit(loggedInAccount.getNumber(), amt);
            loggedInAccount.deposit(amt); // update local instance
            recordTxn("Deposit", amt);
            refreshDashboard();
        }));

        withdrawBtn.addActionListener(e -> showAmountDialog("Withdraw", (amt) -> {
            try {
                accountService.withdraw(loggedInAccount.getNumber(), amt);
                loggedInAccount.withdraw(amt);
                recordTxn("Withdraw", amt.negate());
                refreshDashboard();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }));

        transferBtn.addActionListener(e -> {
            JTextField toField = new JTextField(15);
            JTextField amtField = new JTextField(10);
            JPanel dlg = new JPanel(new GridLayout(0,1));
            dlg.add(new JLabel("To account number:"));
            dlg.add(toField);
            dlg.add(new JLabel("Amount:"));
            dlg.add(amtField);
            int res = JOptionPane.showConfirmDialog(frame, dlg, "Transfer", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                try {
                    String toAccNo = toField.getText().trim();
                    BigDecimal amt = new BigDecimal(amtField.getText().trim());
                    // Simple demo: just withdraw from source and deposit to destination if it exists
                    Account toAcc = accountService.find(toAccNo);
                    if (toAcc == null) throw new IllegalArgumentException("Destination account not found");
                    accountService.withdraw(loggedInAccount.getNumber(), amt);
                    loggedInAccount.withdraw(amt);
                    accountService.deposit(toAccNo, amt);
                    toAcc.deposit(amt);
                    recordTxn("Transfer to "+toAccNo, amt.negate());
                    // Optionally record a credit transaction on destination – omitted for brevity
                    refreshDashboard();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Store reference for welcome label update later
        panel.putClientProperty("welcomeLabel", welcome);
        return panel;
    }

    private void refreshDashboard() {
        balanceLabel.setText("Balance: ₹ " + loggedInAccount.getBalance().setScale(2, BigDecimal.ROUND_HALF_EVEN));
        JLabel welcome = (JLabel) ((JPanel) ((JPanel) mainPanel.getComponent(1)).getComponent(0)).getClientProperty("welcomeLabel");
        if (welcome != null) {
            welcome.setText("Welcome, " + loggedInRole);
        }
        // Load transactions
        List<Transaction> txns = txnService.getHistory(loggedInAccount.getNumber());
        txnTableModel.setTransactions(txns);
    }

    private void recordTxn(String description, BigDecimal amount) {
        BigDecimal newBalance = loggedInAccount.getBalance();
        Transaction txn = new Transaction(LocalDate.now(), description, amount, newBalance);
        txnService.record(loggedInAccount, txn);
    }

    /** Simple dialog that asks for an amount and passes it to the consumer */
    private void showAmountDialog(String title, java.util.function.Consumer<BigDecimal> consumer) {
        String amtStr = JOptionPane.showInputDialog(frame, "Amount (₹):", title, JOptionPane.PLAIN_MESSAGE);
        if (amtStr != null && !amtStr.isBlank()) {
            try {
                BigDecimal amt = new BigDecimal(amtStr.trim());
                if (amt.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
                consumer.accept(amt);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        // Use the system look‑and‑feel for a more native feel
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(UIApp::new);
    }
}

/** Table model for displaying Transaction objects */
class TransactionTableModel extends AbstractTableModel {
    private final String[] columns = {"Date", "Description", "Amount", "Balance"};
    private List<Transaction> data = List.of();

    public void setTransactions(List<Transaction> txns) {
        this.data = txns;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int col) { return columns[col]; }
    @Override public Object getValueAt(int row, int col) {
        Transaction t = data.get(row);
        return switch (col) {
            case 0 -> t.getDate();
            case 1 -> t.getDescription();
            case 2 -> t.getAmount();
            case 3 -> t.getResultingBalance();
            default -> null;
        };
    }
    @Override public Class<?> getColumnClass(int col) {
        return switch (col) {
            case 0 -> LocalDate.class;
            case 2,3 -> BigDecimal.class;
            default -> String.class;
        };
    }
}
