package atm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AdminDashboardPanel extends JPanel {
    private AtmApp app;
    private DefaultListModel<String> listModel;
    private JList<String> accountList;

    private static final Color BG_COLOR = new Color(24, 24, 27);
    private static final Color TEXT_COLOR = new Color(244, 244, 245);
    private static final Color CARD_COLOR = new Color(39, 39, 42);

    public AdminDashboardPanel(AtmApp app) {
        this.app = app;
        setBackground(BG_COLOR);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("System Accounts");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        accountList = new JList<>(listModel);
        accountList.setBackground(CARD_COLOR);
        accountList.setForeground(TEXT_COLOR);
        accountList.setFont(new Font("Consolas", Font.PLAIN, 16));
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(accountList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(63, 63, 70)));
        scrollPane.getViewport().setBackground(CARD_COLOR);
        add(scrollPane, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setBackground(new Color(63, 63, 70));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> app.showUserLogin());
        
        JButton unlockBtn = new JButton("Unlock");
        unlockBtn.setBackground(new Color(16, 185, 129));
        unlockBtn.setForeground(Color.WHITE);
        unlockBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        unlockBtn.setFocusPainted(false);
        unlockBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        unlockBtn.addActionListener(e -> {
            String selected = accountList.getSelectedValue();
            if (selected != null) {
                String accNum = selected.replace("Account: ", "").replace(" (LOCKED)", "").trim();
                if (app.getDatabaseManager().unlockAccount(accNum)) {
                    JOptionPane.showMessageDialog(this, "Account " + accNum + " has been unlocked.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAccounts();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to unlock or account is not locked.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an account from the list first.");
            }
        });

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(239, 68, 68));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            String selected = accountList.getSelectedValue();
            if (selected != null) {
                String accNum = selected.replace("Account: ", "").replace(" (LOCKED)", "").trim();
                int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to completely DELETE account " + accNum + "?\nThis action cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    if (app.getDatabaseManager().deleteAccount(accNum)) {
                        JOptionPane.showMessageDialog(this, "Account " + accNum + " deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                        loadAccounts();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete account.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an account from the list first.");
            }
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        bottomPanel.add(unlockBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(logoutBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void loadAccounts() {
        listModel.clear();
        List<String> accounts = app.getDatabaseManager().getAllAccountNumbersWithStatus();
        for (String acc : accounts) {
            listModel.addElement("Account: " + acc);
        }
    }
}
