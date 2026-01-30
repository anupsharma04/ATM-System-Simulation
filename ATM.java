import java.io.*;
import java.util.*;

/**
 * The ATM class handles the core logic of the application, including
 * user authentication, account creation, and financial transactions.
 */
public class ATM {
    private Scanner input = new Scanner(System.in);
    private ArrayList<ATMInfo> userDetails = new ArrayList<>();
    private Random random = new Random();

    // UI Constants for consistent console formatting
    private static final String LINE = "==============================================";
    private static final String SMALL_LINE = "----------------------------------------------";

    /**
     * Routes user selection from the main menu to specific banking operations.
     * @param option The menu choice selected by the user.
     * @param index The index of the authenticated user in the ArrayList.
     */
    public void switchCase(int option, int index) {
        switch(option) {
            case 1: deposit(index); break; 
            case 2: withdraw(index); break;
            case 3: viewTransactionHistory(index); break;
            default: System.out.println("Error. Please Try Again."); break; 
        }
    }
    
    /**
     * Primary application loop. Handles initial data loading and the top-level welcome menu.
     */
    public void start() {
        userDetails = loadFromFile("userInfo.txt");
        
        while(true) {
            displayHeader("Welcome To The ATM");
            System.out.println("Please select an option:");
            System.out.println(SMALL_LINE);
            System.out.println("1. Create New Account\n2. Login to Existing Account\n3. Exit ATM");
            System.out.println(SMALL_LINE);

            int choice = getValidatedInteger("Enter your choice (1-3): "); 

            if(choice == -1 || isOutOfRange(choice, 1, 3)) {
                waitForInput();
                continue;
            }

            if(choice == 1) {
                createNewAccount();
                waitForInput();
            } else if(choice == 2) {
                int userIndex = Authentication();
                if(userIndex != -1) menu(userIndex);
            } else if(choice == 3) {
                saveToFile("userInfo.txt");
                System.exit(0);
            }
        }
    }

    // Displays the main menu, handles user input, and validates choices
    public void menu(int index) {
        
        while (true) {  // Keeps displaying the menu until user selects option 4 (Exit)
            // Displaying the main menu options
            displayInformation(index);
            System.out.println("--- Main Menu ---");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. View Transaction History");
            System.out.println("4. Exit");

            // Handling input and validating user choice
            int choice = getValidatedInteger("\nSelect an option: ");
            System.out.println();

            if(choice == -1) continue;

            if (choice == 4) {
                saveToFile("userInfo.txt");
                System.out.println("\n\nThank you for using the ATM.\n");
                System.exit(0);  // Terminates the program
            }

            // Validating range of input (1 to 4 only)
            if(isOutOfRange(choice, 1, 4)) continue;
            else switchCase(choice, index); // Navigate to selected operation

            waitForInput(); // Pause before returning to menu
        }
    }
    
    public void createNewAccount(){
        String firstName = getValidatedString("Enter first name: ", "First name");
        String lastName = getValidatedString("Enter last name: ", "Last name");

        String name = firstName + " " + lastName; 
        int pin; 

        while(true){
            pin = getValidatedInteger("\nEnter the pin for your account: ");

            if (pin == -1 || pin < 1000 || pin > 9999) {
                System.out.println("PIN must be 4 digits");
                continue;
            } else break;
        }

        int userAccountNumber;

        while (true) {
            userAccountNumber = 1000 + random.nextInt(9000); // 1000 to 9999
            boolean isDuplicate = false;

            for (ATMInfo s : userDetails) {
                if (userAccountNumber == s.getAccountNumber()) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) break; // unique found, escape loop
        }

        ATMInfo newUser = new ATMInfo(name, userAccountNumber, 0, pin);
        userDetails.add(newUser);
        System.out.println("\n\nAccount created Successfully.\n");
        System.out.println("Your account number is: " + userAccountNumber);
        System.out.println("\nPlease login with your account number and pin to continue.\n");
        saveToFile("userInfo.txt");
    }

    public void displayInformation(int index){
        // displays the information
        displayHeader("Account Information: ");

        System.out.println("Account Name: " + userDetails.get(index).getAccountName());
        System.out.println("Account Number: " + userDetails.get(index).getAccountNumber());
        System.out.println("Account Balance: " + userDetails.get(index).getAccountBalance()); 
        System.out.println();       
    }

    /**
     * Handles the login process by validating account numbers and PINs against the database.
     * @return The index of the user if successful, -1 if the user cancels or fails login.
     */
    public int Authentication() {
        displayHeader("LOGIN TO ACCOUNT");
        System.out.println("Enter '0' at any time to return to the start menu.");
        
        while(true) {
            int userAccountNumber = getValidatedInteger("\nEnter Account Number: ");
            if(userAccountNumber == 0) return -1;
            if(userAccountNumber == -1) continue;

            int userPin = getValidatedInteger("Enter Account Pin: ");
            if(userPin == 0) return -1;

            for(int i = 0; i < userDetails.size(); i++) {
                ATMInfo user = userDetails.get(i);
                if(user.getAccountNumber() == userAccountNumber && user.getAccountPin() == userPin) {
                    System.out.println("\nLogin successful!");
                    return i;
                }
            }
            System.out.println("\n[!] Login Failed: Incorrect credentials.");
            System.out.print("Try again? (y/n): ");
            if(input.nextLine().trim().toLowerCase().startsWith("n")) return -1;
        }
    }

    /**
     * Performs a deposit operation, updates user balance, and logs the transaction.
     */
    public void deposit(int index) {
        ATMInfo user = userDetails.get(index);
        while(true) {
            int amount = getValidatedInteger("\nEnter deposit amount (or 0 to cancel): ");
            if(amount == 0) break;
            if(amount < 0) {
                System.out.println("Error: Amount must be positive.");
                continue;
            }
            double oldBalance = user.getAccountBalance();
            user.deposit(amount);
            user.addTransaction("DEPOSIT", amount, user.getAccountBalance());
            saveTransactionToFile(user, "DEPOSIT", amount, oldBalance);
            System.out.println("Success! New Balance: $" + user.getAccountBalance());
            break;
        }
    }

    /**
     * Performs a withdrawal after verifying sufficient funds and logging the transaction.
     */
    public void withdraw(int index) {
        ATMInfo user = userDetails.get(index);
        while(true) {
            int amount = getValidatedInteger("\nEnter withdrawal amount (or 0 to cancel): ");
            if(amount == 0) break;
            if(amount > user.getAccountBalance()) {
                System.out.println("Error: Insufficient funds. Balance: $" + user.getAccountBalance());
                continue;
            }
            double oldBalance = user.getAccountBalance();
            user.withdraw(amount);
            user.addTransaction("WITHDRAW", amount, user.getAccountBalance());
            saveTransactionToFile(user, "WITHDRAW", amount, oldBalance);
            System.out.println("Success! New Balance: $" + user.getAccountBalance());
            break;
        }
    }

    /**
     * Appends a detailed transaction log to an external text file for permanent record keeping.
     */
    private void saveTransactionToFile(ATMInfo user, String type, double amount, double oldBalance) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("statement.txt", true))) {
            writer.write(String.format("%s,%d,%.2f,%s,%.2f,%.2f%n",
                user.getAccountName(), user.getAccountNumber(), oldBalance, type, amount, user.getAccountBalance()));
        } catch (IOException e) {
            System.out.println("File Error: " + e.getMessage());
        }
    }

    public void viewTransactionHistory(int index) {
        displayHeader("TRANSACTION HISTORY");
        ATMInfo user = userDetails.get(index);
        
        System.out.printf("%-20s | %-10s | %-10s | %-10s%n", "Date/Time", "Type", "Amount", "Balance");
        System.out.println(SMALL_LINE);
        
        if(user.getTransactions().isEmpty()) {
            System.out.println("[No recent transactions this session]");
        } else {
            for (String t : user.getTransactions()) {
                System.out.println(t);
            }
        }
    }
    
    public void saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (ATMInfo user : userDetails) {
                String line = user.getAccountName() + ", " 
                            + user.getAccountNumber() + ", " 
                            + user.getAccountBalance() + ", " 
                            + user.getAccountPin();
                writer.write(line);
                writer.newLine(); // move to next line
            }
        } catch (IOException e) {
            System.out.println("Error while writing to file: " + e.getMessage());
        }
    }

    public static ArrayList<ATMInfo> loadFromFile(String filename) {
        ArrayList<ATMInfo> atmList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty or malformed lines
                if (line.trim().isEmpty() || line.split(",").length < 4) continue;

                String[] parts = line.split(",\\s*"); // handles optional space after comma
                String name = parts[0];
                int accountNumber = Integer.parseInt(parts[1]);
                double balance = Double.parseDouble(parts[2]);
                int pin = Integer.parseInt(parts[3]);

                ATMInfo atm = new ATMInfo(name, accountNumber, balance, pin);
                atmList.add(atm);
            }
        } catch (IOException e) {
            System.out.println("Error while reading from file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Data format issue: " + e.getMessage());
        }

        return atmList;
    }

    // ======================
    // HELPER FUNCTIONS
    // ======================

    /**
     * Validates and formats a single-word string input (for names or other fields)
     * - Ensures input is not empty
     * - Contains no digits
     * - Contains no spaces (single word only)
     * - Only allows letters, hyphens, and apostrophes
     * - Returns formatted string with first letter capitalized
     * 
     * @param message The prompt to display to the user (e.g., "First name: ")
     * @param type    The field name for error messages (e.g., "First name")
     * @return Validated and properly formatted string
     */
    String getValidatedString(String message, String type) {
        String string;
        while (true) {
            System.out.print(message);
            // Convert to lowercase first for consistent formatting
            string = input.nextLine().trim().toLowerCase();

            // Empty input check
            if (string.isEmpty()) {
                System.out.println("\n" + type + " cannot be empty.\n");
                continue;
            }

            // Digit check
            if (string.matches(".*\\d.*")) {
                System.out.println("\n" + type + " cannot contain digits.\n");
                continue;
            }

            // Space check (enforces single word)
            if (string.contains(" ")) {
                System.out.println("Error: Please enter only one name (no spaces)\n");
                continue;
            }
        
            // Special character validation
            if (!string.matches("^[a-zA-Z'-]+$")) {
                System.out.println("Error: Only letters, hyphens (-), and apostrophes (') allowed\n");
                continue;
            }

            break; // Exit loop when all validations pass
        }
    
        // Format: Capitalize first letter + keep rest lowercase
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }


    /**
     * Displays a consistent header section with title
     * @param title The section title to display (will be converted to uppercase)
     */
    public void displayHeader(String title) {
        clearScreen();
        System.out.println(LINE);
        System.out.println("\t" + title.toUpperCase());
        System.out.println(LINE);
        System.out.println();  // Extra blank line for readability
    }


    /**
    * Pauses execution and waits for user confirmation before proceeding
    * Provides consistent UX flow between operations
    */
    public void waitForInput() {
        System.out.println("Press # to return back to main menu. \tOR");
        System.out.print("\nPress ENTER to continue.......");
        // String userInput = 
        input.nextLine();
        clearScreen();
    }


    /**
    * Clears the console screen for better readability
    * Uses ANSI escape codes for cross-platform compatibility
    */
    public void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
            }
            System.out.flush();
        } catch (Exception e) {
            // Fallback to simple newlines if clearing fails
            System.out.println("\n".repeat(50));
        }
    }


    /**
     * Validates if a number falls within specified range
     * @param value The number to check
     * @param lower Minimum allowed value (inclusive)
     * @param upper Maximum allowed value (inclusive)
     * @return true if out of range (invalid), false if in range (valid)
     */
    public boolean isOutOfRange(int value, int lower, int upper) {
        if (value < lower || value > upper) {
            System.out.printf("\nERROR: Please select an option between %d and %d\n", lower, upper);
            System.out.println(SMALL_LINE);
            return true;
        }
        return false;
    }


    /**
     * Validates and parses integer input from user
     * @param message The prompt to display
     * @return Parsed integer or -1 for invalid input
     */
    public int getValidatedInteger(String message) {
        System.out.print(message);
        String userInput = input.nextLine().trim();
        
        // Handle empty input
        if (userInput.isEmpty()) {
            System.out.println("\nERROR: No input detected");
            System.out.println("Please enter a numeric value");
            return -1;
        }
        
        // Handle numeric conversion
        try {
            return Integer.parseInt(userInput);
        } catch (NumberFormatException e) {
            System.out.printf("\nERROR: '%s' is not a valid number\n", userInput);
            System.out.println("Please enter digits only.");
            return -1;
        }
    }


}
