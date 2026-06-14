package atm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:atm.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "account_number TEXT UNIQUE NOT NULL," +
                    "pin TEXT NOT NULL," +
                    "balance REAL NOT NULL DEFAULT 0.0" +
                    ");";
            stmt.execute(createTableSQL);
            
            // Seed a test account if it doesn't exist
            String seedSQL = "INSERT OR IGNORE INTO accounts (account_number, pin, balance) VALUES ('12345', '1234', 1000.0);";
            stmt.execute(seedSQL);
            
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    public boolean createAccount(String accountNumber, String pin) {
        String sql = "INSERT INTO accounts (account_number, pin, balance) VALUES (?, ?, 0.0)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, pin);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Account creation error: " + e.getMessage());
        }
        return false;
    }

    public Account authenticate(String accountNumber, String pin) {
        String sql = "SELECT * FROM accounts WHERE account_number = ? AND pin = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Account(
                    rs.getInt("id"),
                    rs.getString("account_number"),
                    rs.getString("pin"),
                    rs.getDouble("balance")
                );
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    public boolean updateBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, accountId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Update balance error: " + e.getMessage());
        }
        return false;
    }
    
    public double getBalance(int accountId) {
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
            
        } catch (SQLException e) {
            System.err.println("Get balance error: " + e.getMessage());
        }
        return -1.0;
    }

    // New methods for extended options and admin

    public boolean updatePin(int accountId, String newPin) {
        String sql = "UPDATE accounts SET pin = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPin);
            pstmt.setInt(2, accountId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Update PIN error: " + e.getMessage());
        }
        return false;
    }

    public boolean transferFunds(int fromAccountId, String toAccountNumber, double amount) {
        // Find recipient account
        String findRecipientSql = "SELECT id, balance FROM accounts WHERE account_number = ?";
        String updateSenderSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        String updateRecipientSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement findRecipientStmt = conn.prepareStatement(findRecipientSql);
                 PreparedStatement updateSenderStmt = conn.prepareStatement(updateSenderSql);
                 PreparedStatement updateRecipientStmt = conn.prepareStatement(updateRecipientSql)) {

                findRecipientStmt.setString(1, toAccountNumber);
                ResultSet rs = findRecipientStmt.executeQuery();
                
                if (!rs.next()) {
                    conn.rollback();
                    return false; // Recipient not found
                }

                int toAccountId = rs.getInt("id");

                if (fromAccountId == toAccountId) {
                    conn.rollback();
                    return false; // Cannot transfer to self
                }

                // Deduct from sender
                updateSenderStmt.setDouble(1, amount);
                updateSenderStmt.setInt(2, fromAccountId);
                int senderRows = updateSenderStmt.executeUpdate();

                // Add to recipient
                updateRecipientStmt.setDouble(1, amount);
                updateRecipientStmt.setInt(2, toAccountId);
                int recipientRows = updateRecipientStmt.executeUpdate();

                if (senderRows > 0 && recipientRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                }

            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Transfer error during transaction: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Transfer connection error: " + e.getMessage());
        }
        return false;
    }

    public List<String> getAllAccountNumbers() {
        List<String> accountNumbers = new ArrayList<>();
        String sql = "SELECT account_number FROM accounts";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                accountNumbers.add(rs.getString("account_number"));
            }
            
        } catch (SQLException e) {
            System.err.println("Get all account numbers error: " + e.getMessage());
        }
        return accountNumbers;
    }
}
