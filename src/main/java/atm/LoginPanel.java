package atm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {
    private JTextField accountField;
    private JPasswordField pinField;
    private JButton loginButton;
    private JButton createAccountButton;

    private static final Color BG_COLOR = new Color(24, 24, 27);
    private static final Color ACCENT_COLOR = new Color(59, 130, 246); // Blue
    private static final Color TEXT_COLOR = new Color(244, 244, 245);
    private static final Color INPUT_BG = new Color(39, 39, 42);

    public LoginPanel(AtmApp app) {
        setBackground(BG_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(50, 40, 50, 40));

        // Logo / Icon Placeholder
        JLabel iconLabel = new JLabel("ATM", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(iconLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel titleLabel = new JLabel("Welcome back");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Please enter your details to sign in.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(161, 161, 170));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(subtitleLabel);
        
        add(Box.createRigidArea(new Dimension(0, 40)));

        // Inputs Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(BG_COLOR);
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.setMaximumSize(new Dimension(300, 200));

        accountField = createStyledTextField("Account Number");
        pinField = createStyledPasswordField("PIN");

        inputPanel.add(createLabel("Account Number"));
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        inputPanel.add(accountField);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        inputPanel.add(createLabel("PIN"));
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        inputPanel.add(pinField);

        add(inputPanel);
        add(Box.createRigidArea(new Dimension(0, 40)));

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(300, 150));

        loginButton = createStyledButton("Sign In", ACCENT_COLOR, Color.WHITE);
        createAccountButton = createStyledButton("Create Account", INPUT_BG, TEXT_COLOR);

        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(createAccountButton);

        add(buttonPanel);
        
        add(Box.createVerticalGlue());
        
        JButton adminBtn = new JButton("Admin Access");
        adminBtn.setForeground(new Color(161, 161, 170));
        adminBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminBtn.setContentAreaFilled(false);
        adminBtn.setBorderPainted(false);
        adminBtn.setFocusPainted(false);
        adminBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        adminBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminBtn.addActionListener(e -> app.showAdminLogin());
        
        add(adminBtn);

        // Actions
        loginButton.addActionListener(e -> {
            String accNum = accountField.getText();
            String pin = new String(pinField.getPassword());
            app.handleLogin(accNum, pin);
        });

        createAccountButton.addActionListener(e -> {
            String accNum = accountField.getText();
            String pin = new String(pinField.getPassword());
            app.handleCreateAccount(accNum, pin);
        });
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(212, 212, 216));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField(String placeholder) {
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
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 15, 10, 15),
            BorderFactory.createEmptyBorder()
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(300, 40));
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
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
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 15, 10, 15),
            BorderFactory.createEmptyBorder()
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(300, 40));
        return field;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public void clearFields() {
        accountField.setText("");
        pinField.setText("");
    }
}
