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
        logoutBtn.setBackground(new Color(239, 68, 68));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> app.showUserLogin());
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        bottomPanel.add(logoutBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void loadAccounts() {
        listModel.clear();
        List<String> accounts = app.getDatabaseManager().getAllAccountNumbers();
        for (String acc : accounts) {
            listModel.addElement("Account: " + acc);
        }
    }
}
