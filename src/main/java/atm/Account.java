package atm;

public class Account {
    private int id;
    private String accountNumber;
    private String pin;
    private double balance;
    private boolean isLocked;

    public Account(int id, String accountNumber, String pin, double balance, boolean isLocked) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.isLocked = isLocked;
    }

    public int getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getPin() {
        return pin;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
