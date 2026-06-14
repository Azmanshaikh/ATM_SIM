package atm;

import javax.swing.*;
import java.awt.*;

public class AtmApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private AdminLoginPanel adminLoginPanel;
    private AdminDashboardPanel adminDashboardPanel;

    private DatabaseManager dbManager;
    private Account currentAccount;

    public AtmApp() {
        super("Premium ATM Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800); // Increased window size
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(24, 24, 27));

        dbManager = new DatabaseManager();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(24, 24, 27));

        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel(this);
        adminLoginPanel = new AdminLoginPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);

        mainPanel.add(loginPanel, "Login");
        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(adminLoginPanel, "AdminLogin");
        mainPanel.add(adminDashboardPanel, "AdminDashboard");

        add(mainPanel);

        // Show user login initially
        cardLayout.show(mainPanel, "Login");
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void showAdminLogin() {
        adminLoginPanel.clearFields();
        cardLayout.show(mainPanel, "AdminLogin");
    }

    public void showUserLogin() {
        loginPanel.clearFields();
        cardLayout.show(mainPanel, "Login");
    }

    public void handleLogin(String accountNumber, String pin) {
        if (accountNumber == null || accountNumber.trim().isEmpty() || pin == null || pin.trim().isEmpty()) {
            showError("Please enter both Account Number and PIN.");
            return;
        }

        Account account = dbManager.authenticate(accountNumber, pin);
        if (account != null) {
            if (account.isLocked()) {
                showError("ACCOUNT LOCKED. Too many failed login attempts.");
                return;
            }
            currentAccount = account;
            loginPanel.clearFields();
            dashboardPanel.updateAccountInfo();
            cardLayout.show(mainPanel, "Dashboard");
        } else {
            showError("Invalid Account Number or PIN.");
        }
    }

    public void handleAdminLogin(String username, String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            adminLoginPanel.clearFields();
            adminDashboardPanel.loadAccounts();
            cardLayout.show(mainPanel, "AdminDashboard");
        } else {
            showError("Invalid Admin Credentials.");
        }
    }

    public void handleCreateAccount(String accountNumber, String pin) {
        if (accountNumber == null || accountNumber.trim().isEmpty() || pin == null || pin.trim().isEmpty()) {
            showError("Please enter both Account Number and PIN to create an account.");
            return;
        }
        
        if (dbManager.createAccount(accountNumber, pin)) {
            JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loginPanel.clearFields();
        } else {
            showError("Failed to create account. The account number might already exist.");
        }
    }

    public void handleLogout() {
        currentAccount = null;
        showUserLogin();
    }

    public void handleDeposit(double amount) {
        if (amount <= 0) {
            showError("Deposit amount must be greater than zero.");
            return;
        }

        if (currentAccount != null) {
            double currentBalance = dbManager.getBalance(currentAccount.getId());
            double newBalance = currentBalance + amount;
            if (dbManager.updateBalance(currentAccount.getId(), newBalance, amount, "Deposit")) {
                dashboardPanel.updateBalanceDisplay();
                promptReceipt("Deposit", amount, newBalance);
            } else {
                showError("Failed to deposit money.");
            }
        }
    }

    public void handleWithdraw(double amount) {
        if (amount <= 0) {
            showError("Withdrawal amount must be greater than zero.");
            return;
        }

        if (currentAccount != null) {
            double currentBalance = dbManager.getBalance(currentAccount.getId());
            if (amount > currentBalance) {
                showError("Insufficient funds.");
                return;
            }

            double newBalance = currentBalance - amount;
            if (dbManager.updateBalance(currentAccount.getId(), newBalance, amount, "Withdrawal")) {
                dashboardPanel.updateBalanceDisplay();
                promptReceipt("Withdrawal", amount, newBalance);
            } else {
                showError("Failed to withdraw money.");
            }
        }
    }

    public void handleTransfer(String targetAccount, double amount) {
        if (amount <= 0) {
            showError("Transfer amount must be greater than zero.");
            return;
        }
        
        if (!dbManager.doesAccountExist(targetAccount)) {
            showError("The destination account doesn't exist. Please verify the account number.");
            return;
        }

        if (currentAccount != null) {
            double currentBalance = dbManager.getBalance(currentAccount.getId());
            if (amount > currentBalance) {
                showError("Insufficient funds for transfer.");
                return;
            }

            if (dbManager.transferFunds(currentAccount.getId(), targetAccount, amount)) {
                dashboardPanel.updateBalanceDisplay();
                promptReceipt("Transfer to " + targetAccount, amount, currentBalance - amount);
            } else {
                showError("Transfer failed due to an unexpected error.");
            }
        }
    }

    private void promptReceipt(String type, double amount, double newBalance) {
        int choice = JOptionPane.showConfirmDialog(this, "Transaction Successful! Would you like a receipt?", "Receipt", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            String receipt = String.format(
                "==============================\n" +
                "       ATM TRANSACTION       \n" +
                "==============================\n\n" +
                " Account: %s\n" +
                " Type: %s\n" +
                " Amount: $%.2f\n" +
                " Available Balance: $%.2f\n\n" +
                "==============================\n" +
                "      Thank you!             \n",
                currentAccount.getAccountNumber(), type, amount, newBalance
            );
            JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void handleChangePin(String newPin) {
        if (currentAccount != null) {
            if (dbManager.updatePin(currentAccount.getId(), newPin)) {
                JOptionPane.showMessageDialog(this, "PIN successfully updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("Failed to update PIN.");
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AtmApp app = new AtmApp();
            app.setVisible(true);
        });
    }
}
