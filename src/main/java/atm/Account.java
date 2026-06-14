package atm;

public class Account {
    private int id;
    private String accountNumber;
    private String pin;
    private double balance;

    public Account(int id, String accountNumber, String pin, double balance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
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
}
