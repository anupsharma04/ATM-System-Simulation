/**
 * Entry point for the ATM Simulation System.
 * Initializes the ATM application and launches the main interface.
 */
public class Main {   
    public static void main(String[] args) {
        // Instantiate the ATM engine
        ATM atm = new ATM(); 
        
        // Start the application lifecycle
        atm.start();
    }
}