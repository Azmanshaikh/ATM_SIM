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
            
            // Note: failed_attempts and is_locked added
            String createAccountsTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "account_number TEXT UNIQUE NOT NULL," +
                    "pin TEXT NOT NULL," +
                    "balance REAL NOT NULL DEFAULT 0.0," +
                    "failed_attempts INTEGER NOT NULL DEFAULT 0," +
                    "is_locked BOOLEAN NOT NULL DEFAULT 0" +
                    ");";
            stmt.execute(createAccountsTableSQL);
            
            // Transactions table
            String createTransactionsTableSQL = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "account_id INTEGER NOT NULL," +
                    "type TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(createTransactionsTableSQL);
            
            // Seed a test account if it doesn't exist
            String seedSQL = "INSERT OR IGNORE INTO accounts (account_number, pin, balance, failed_attempts, is_locked) VALUES ('12345', '1234', 1000.0, 0, 0);";
            stmt.execute(seedSQL);
            
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    public boolean createAccount(String accountNumber, String pin) {
        String sql = "INSERT INTO accounts (account_number, pin, balance, failed_attempts, is_locked) VALUES (?, ?, 0.0, 0, 0)";
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

    /**
     * Returns the Account if valid.
     * Returns an Account object with isLocked=true if it is locked.
     * Returns null if account doesn't exist or wrong PIN.
     */
    public Account authenticate(String accountNumber, String pin) {
        String getAccountSql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(getAccountSql)) {
            
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                boolean isLocked = rs.getBoolean("is_locked");
                int accountId = rs.getInt("id");
                String dbPin = rs.getString("pin");
                int failedAttempts = rs.getInt("failed_attempts");

                if (isLocked) {
                    return new Account(accountId, accountNumber, dbPin, rs.getDouble("balance"), true);
                }

                if (dbPin.equals(pin)) {
                    // Reset failed attempts on success
                    resetFailedAttempts(conn, accountId);
                    return new Account(accountId, accountNumber, dbPin, rs.getDouble("balance"), false);
                } else {
                    // Wrong PIN, increment attempts
                    incrementFailedAttempts(conn, accountId, failedAttempts);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    private void incrementFailedAttempts(Connection conn, int accountId, int currentAttempts) throws SQLException {
        int newAttempts = currentAttempts + 1;
        boolean lock = newAttempts >= 3;
        
        String sql = "UPDATE accounts SET failed_attempts = ?, is_locked = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newAttempts);
            pstmt.setBoolean(2, lock);
            pstmt.setInt(3, accountId);
            pstmt.executeUpdate();
        }
    }

    private void resetFailedAttempts(Connection conn, int accountId) throws SQLException {
        String sql = "UPDATE accounts SET failed_attempts = 0 WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.executeUpdate();
        }
    }

    public boolean updateBalance(int accountId, double newBalance, double transactionAmount, String type) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, newBalance);
                pstmt.setInt(2, accountId);
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    recordTransaction(conn, accountId, type, transactionAmount);
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Update balance error: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
        return false;
    }
    
    private void recordTransaction(Connection conn, int accountId, String type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            pstmt.executeUpdate();
        }
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
        String findRecipientSql = "SELECT id, balance FROM accounts WHERE account_number = ?";
        String updateSenderSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        String updateRecipientSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); 

            try (PreparedStatement findRecipientStmt = conn.prepareStatement(findRecipientSql);
                 PreparedStatement updateSenderStmt = conn.prepareStatement(updateSenderSql);
                 PreparedStatement updateRecipientStmt = conn.prepareStatement(updateRecipientSql)) {

                findRecipientStmt.setString(1, toAccountNumber);
                ResultSet rs = findRecipientStmt.executeQuery();
                
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                int toAccountId = rs.getInt("id");

                if (fromAccountId == toAccountId) {
                    conn.rollback();
                    return false;
                }

                updateSenderStmt.setDouble(1, amount);
                updateSenderStmt.setInt(2, fromAccountId);
                int senderRows = updateSenderStmt.executeUpdate();

                updateRecipientStmt.setDouble(1, amount);
                updateRecipientStmt.setInt(2, toAccountId);
                int recipientRows = updateRecipientStmt.executeUpdate();

                if (senderRows > 0 && recipientRows > 0) {
                    recordTransaction(conn, fromAccountId, "Transfer Out to " + toAccountNumber, amount);
                    recordTransaction(conn, toAccountId, "Transfer In", amount);
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

    public List<String> getAllAccountNumbersWithStatus() {
        List<String> accountNumbers = new ArrayList<>();
        String sql = "SELECT account_number, is_locked FROM accounts";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String acc = rs.getString("account_number");
                boolean locked = rs.getBoolean("is_locked");
                if (locked) {
                    accountNumbers.add(acc + " (LOCKED)");
                } else {
                    accountNumbers.add(acc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Get all account numbers error: " + e.getMessage());
        }
        return accountNumbers;
    }

    public boolean unlockAccount(String accountNumber) {
        String sql = "UPDATE accounts SET is_locked = 0, failed_attempts = 0 WHERE account_number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unlock error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteAccount(String accountNumber) {
        String getSql = "SELECT id FROM accounts WHERE account_number = ?";
        String delAccSql = "DELETE FROM accounts WHERE account_number = ?";
        String delTransSql = "DELETE FROM transactions WHERE account_id = ?";
        
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);
            try (PreparedStatement getStmt = conn.prepareStatement(getSql);
                 PreparedStatement delAccStmt = conn.prepareStatement(delAccSql);
                 PreparedStatement delTransStmt = conn.prepareStatement(delTransSql)) {
                 
                getStmt.setString(1, accountNumber);
                ResultSet rs = getStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                int accountId = rs.getInt("id");
                
                delTransStmt.setInt(1, accountId);
                delTransStmt.executeUpdate();
                
                delAccStmt.setString(1, accountNumber);
                int rows = delAccStmt.executeUpdate();
                
                if (rows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Delete logic error: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Delete conn error: " + e.getMessage());
        }
        return false;
    }

    public List<Transaction> getTransactions(int accountId, String keyword) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ?";
        
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql += " AND (type LIKE ? OR amount LIKE ? OR timestamp LIKE ?)";
        }
        sql += " ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, accountId);
            if (hasKeyword) {
                String searchParam = "%" + keyword.trim() + "%";
                pstmt.setString(2, searchParam);
                pstmt.setString(3, searchParam);
                pstmt.setString(4, searchParam);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Get transactions error: " + e.getMessage());
        }
        return transactions;
    }

    public boolean doesAccountExist(String accountNumber) {
        String sql = "SELECT id FROM accounts WHERE account_number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("Account check error: " + e.getMessage());
        }
        return false;
    }
}
