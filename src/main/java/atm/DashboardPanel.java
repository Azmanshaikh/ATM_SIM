package atm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class DashboardPanel extends JPanel {
    private AtmApp app;
    private JLabel balanceLabel;
    private JLabel welcomeLabel;
    
    private static final Color BG_COLOR = new Color(24, 24, 27);
    private static final Color CARD_COLOR = new Color(39, 39, 42);
    private static final Color INPUT_BG = new Color(39, 39, 42);
    private static final Color TEXT_COLOR = new Color(244, 244, 245);
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129); // Green
    private static final Color DANGER_COLOR = new Color(239, 68, 68); // Red
    private static final Color WARNING_COLOR = new Color(245, 158, 11); // Orange
    private static final Color INFO_COLOR = new Color(14, 165, 233); // Light Blue
    private static final Color HISTORY_COLOR = new Color(139, 92, 246); // Purple

    public DashboardPanel(AtmApp app) {
        this.app = app;
        setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        // Top Info Panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(new EmptyBorder(30, 40, 20, 40));

        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Balance Card
        JPanel balanceCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(59, 130, 246), getWidth(), getHeight(), new Color(147, 51, 234));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                super.paintComponent(g);
            }
        };
        balanceCard.setOpaque(false);
        balanceCard.setLayout(new BoxLayout(balanceCard, BoxLayout.Y_AXIS));
        balanceCard.setBorder(new EmptyBorder(30, 30, 30, 30));
        balanceCard.setMaximumSize(new Dimension(500, 150));
        balanceCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel balanceTitleLabel = new JLabel("Current Balance");
        balanceTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        balanceTitleLabel.setForeground(new Color(255, 255, 255, 200));
        balanceTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        balanceLabel = new JLabel("$0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        balanceCard.add(balanceTitleLabel);
        balanceCard.add(Box.createRigidArea(new Dimension(0, 10)));
        balanceCard.add(balanceLabel);

        topPanel.add(welcomeLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        topPanel.add(balanceCard);

        add(topPanel, BorderLayout.NORTH);

        // Center Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 2, 20, 20)); // Grid 3x2 for extra button
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(20, 60, 40, 60));
        
        JButton depositBtn = createActionBtn("Deposit Cash", SUCCESS_COLOR);
        JButton withdrawBtn = createActionBtn("Withdraw Money", DANGER_COLOR);
        JButton transferBtn = createActionBtn("Transfer Funds", INFO_COLOR);
        JButton changePinBtn = createActionBtn("Change PIN", WARNING_COLOR);
        JButton historyBtn = createActionBtn("Transaction History", HISTORY_COLOR);

        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(transferBtn);
        buttonPanel.add(historyBtn);
        buttonPanel.add(changePinBtn);

        add(buttonPanel, BorderLayout.CENTER);
        
        // Bottom Logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(new EmptyBorder(10, 60, 30, 60));
        JButton logoutBtn = createActionBtn("Sign Out", CARD_COLOR);
        logoutBtn.setPreferredSize(new Dimension(300, 50));
        bottomPanel.add(logoutBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Handlers
        depositBtn.addActionListener(e -> showDepositDialog());
        withdrawBtn.addActionListener(e -> showWithdrawDialog());
        transferBtn.addActionListener(e -> showTransferDialog());
        changePinBtn.addActionListener(e -> showChangePinDialog());
        historyBtn.addActionListener(e -> showHistoryDialog());
        logoutBtn.addActionListener(e -> app.handleLogout());
    }

    private void showDepositDialog() {
        JDialog dialog = createStyledDialog("Deposit Cash");
        JTextField amountField = createStyledTextField();
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        content.add(createStyledLabel("Enter amount to deposit:"));
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(amountField);
        
        JButton submitBtn = createActionBtn("Confirm Deposit", SUCCESS_COLOR);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(300, 45));
        
        amountField.addActionListener(e -> submitBtn.doClick());
        
        submitBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                dialog.dispose();
                app.handleDeposit(amount);
            } catch (NumberFormatException ex) {
                showError("Invalid amount entered.");
            }
        });
        
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(submitBtn);
        
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void showWithdrawDialog() {
        JDialog dialog = createStyledDialog("Withdraw Money");
        dialog.setSize(400, 480);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        
        content.add(createStyledLabel("Fast Cash:"));
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel fastCashPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        fastCashPanel.setBackground(BG_COLOR);
        fastCashPanel.setOpaque(false);
        fastCashPanel.setMaximumSize(new Dimension(300, 100));
        fastCashPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton btn20 = createActionBtn("$20", CARD_COLOR);
        JButton btn50 = createActionBtn("$50", CARD_COLOR);
        JButton btn100 = createActionBtn("$100", CARD_COLOR);
        JButton btn200 = createActionBtn("$200", CARD_COLOR);
        
        java.awt.event.ActionListener fastCashAction = e -> {
            dialog.dispose();
            double amount = Double.parseDouble(((JButton)e.getSource()).getText().replace("$", ""));
            app.handleWithdraw(amount);
        };
        
        btn20.addActionListener(fastCashAction);
        btn50.addActionListener(fastCashAction);
        btn100.addActionListener(fastCashAction);
        btn200.addActionListener(fastCashAction);
        
        fastCashPanel.add(btn20);
        fastCashPanel.add(btn50);
        fastCashPanel.add(btn100);
        fastCashPanel.add(btn200);
        
        content.add(fastCashPanel);
        content.add(Box.createRigidArea(new Dimension(0, 30)));
        
        content.add(createStyledLabel("Or enter custom amount:"));
        content.add(Box.createRigidArea(new Dimension(0, 5)));
        JTextField amountField = createStyledTextField();
        content.add(amountField);
        
        JButton submitBtn = createActionBtn("Withdraw Custom", DANGER_COLOR);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(300, 45));
        
        amountField.addActionListener(e -> submitBtn.doClick());
        
        submitBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                dialog.dispose();
                app.handleWithdraw(amount);
            } catch (NumberFormatException ex) {
                showError("Invalid amount entered.");
            }
        });
        
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(submitBtn);
        
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void showTransferDialog() {
        JDialog dialog = createStyledDialog("Transfer Funds");
        JTextField targetField = createStyledTextField();
        JTextField amountField = createStyledTextField();
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        
        content.add(createStyledLabel("Destination Account Number:"));
        content.add(Box.createRigidArea(new Dimension(0, 5)));
        content.add(targetField);
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        
        content.add(createStyledLabel("Amount:"));
        content.add(Box.createRigidArea(new Dimension(0, 5)));
        content.add(amountField);
        
        JButton submitBtn = createActionBtn("Confirm Transfer", INFO_COLOR);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(300, 45));
        
        targetField.addActionListener(e -> amountField.requestFocusInWindow());
        amountField.addActionListener(e -> submitBtn.doClick());
        
        submitBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String targetAccount = targetField.getText();
                dialog.dispose();
                app.handleTransfer(targetAccount, amount);
            } catch (NumberFormatException ex) {
                showError("Invalid amount entered.");
            }
        });
        
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(submitBtn);
        
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void showChangePinDialog() {
        JDialog dialog = createStyledDialog("Change PIN");
        JPasswordField oldPinField = createStyledPasswordField();
        JPasswordField newPinField = createStyledPasswordField();
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        
        content.add(createStyledLabel("Current PIN:"));
        content.add(Box.createRigidArea(new Dimension(0, 5)));
        content.add(oldPinField);
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        
        content.add(createStyledLabel("New PIN:"));
        content.add(Box.createRigidArea(new Dimension(0, 5)));
        content.add(newPinField);
        
        JButton submitBtn = createActionBtn("Update PIN", WARNING_COLOR);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(300, 45));
        
        oldPinField.addActionListener(e -> newPinField.requestFocusInWindow());
        newPinField.addActionListener(e -> submitBtn.doClick());
        
        submitBtn.addActionListener(e -> {
            String oldPin = new String(oldPinField.getPassword());
            String newPin = new String(newPinField.getPassword());
            
            if (oldPin.isEmpty() || newPin.isEmpty()) {
                showError("Please fill in all fields.");
                return;
            }
            
            if (!oldPin.equals(app.getCurrentAccount().getPin())) {
                showError("Incorrect current PIN.");
                return;
            }
            
            dialog.dispose();
            app.handleChangePin(newPin);
        });
        
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(submitBtn);
        
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void showHistoryDialog() {
        JDialog dialog = new JDialog(app, "Transaction History", true);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(app);
        dialog.getContentPane().setBackground(BG_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_COLOR);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JTextField searchField = createStyledTextField();
        searchField.setPreferredSize(new Dimension(300, 40));
        JButton searchBtn = createActionBtn("Search", HISTORY_COLOR);
        
        searchPanel.add(new JLabel(" "), BorderLayout.WEST); // padding
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        // Table
        String[] columnNames = {"Date", "Type", "Amount"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setBackground(CARD_COLOR);
        table.setForeground(TEXT_COLOR);
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int row = table.getSelectedRow();
                    String date = (String) table.getValueAt(row, 0);
                    String type = (String) table.getValueAt(row, 1);
                    String amountStr = (String) table.getValueAt(row, 2);
                    app.promptHistoricalReceipt(date, type, amountStr);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(63, 63, 70)));

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Load initial data
        loadTransactionsIntoTable(tableModel, "");

        searchBtn.addActionListener(e -> {
            loadTransactionsIntoTable(tableModel, searchField.getText());
        });

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void loadTransactionsIntoTable(DefaultTableModel model, String keyword) {
        model.setRowCount(0);
        List<Transaction> txs = app.getDatabaseManager().getTransactions(app.getCurrentAccount().getId(), keyword);
        for (Transaction t : txs) {
            model.addRow(new Object[]{t.getTimestamp(), t.getType(), String.format("$%.2f", t.getAmount())});
        }
    }

    // --- Utility Methods for UI Styling ---

    private JDialog createStyledDialog(String title) {
        JDialog dialog = new JDialog(app, title, true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(app);
        dialog.getContentPane().setBackground(BG_COLOR);
        dialog.getRootPane().setBorder(new EmptyBorder(20, 30, 20, 30));
        return dialog;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(212, 212, 216));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 15, 10, 15),
            BorderFactory.createEmptyBorder()
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(300, 45));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 15, 10, 15),
            BorderFactory.createEmptyBorder()
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(300, 45));
        return field;
    }

    private JButton createActionBtn(String text, Color bg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(null); // Removes default borders completely
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void updateAccountInfo() {
        Account currentAccount = app.getCurrentAccount();
        if (currentAccount != null) {
            welcomeLabel.setText("Account: " + currentAccount.getAccountNumber());
            updateBalanceDisplay();
        }
    }

    public void updateBalanceDisplay() {
        Account currentAccount = app.getCurrentAccount();
        if (currentAccount != null) {
            double currentBalance = app.getDatabaseManager().getBalance(currentAccount.getId());
            currentAccount.setBalance(currentBalance); // sync
            balanceLabel.setText(String.format("$%.2f", currentBalance));
        }
    }
}
