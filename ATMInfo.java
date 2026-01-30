import java.util.Date; 
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Data model representing a single ATM user account.
 * Stores account credentials, balance, and session transaction history.
 */
public class ATMInfo {
    private String accountName; 
    private int accountNumber;
    private int accountPin;
    private double accountBalance; 
    private ArrayList<String> transactions = new ArrayList<>();
    
    /**
     * Constructor to initialize a new user account object.
     */
    ATMInfo(String accountName, int accountNumber, double accountBalance, int accountPin){
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.accountBalance = accountBalance;
        this.accountPin = accountPin; 
    }

    /**
     * Records a specific financial action with a timestamp.
     * @param type The transaction type (e.g., DEPOSIT, WITHDRAW).
     * @param amount The value of the transaction.
     * @param balance The resulting account balance.
     */
    public void addTransaction(String type, double amount, double balance) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        transactions.add(String.format("%s | %-9s | %10.2f | %10.2f", 
            timestamp, type, amount, balance));
    }

    // Standard Getters and Setters (Encapsulation)
    public ArrayList<String> getTransactions() { return transactions; }
    public void deposit(double amount){ this.accountBalance += amount; }
    public void withdraw(double amount){ this.accountBalance -= amount; }
    public String getAccountName(){ return this.accountName; }
    public int getAccountNumber(){ return this.accountNumber; }
    public int getAccountPin(){ return this.accountPin; }
    public double getAccountBalance(){ return this.accountBalance; }
}