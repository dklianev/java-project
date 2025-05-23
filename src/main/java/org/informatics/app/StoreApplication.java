package org.informatics.app;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Product;
import org.informatics.entity.Receipt;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.service.impl.FileServiceImpl;
import org.informatics.service.impl.GoodsServiceImpl;
import org.informatics.service.impl.StoreServiceImpl;
import org.informatics.store.Store;

// Main application class demonstrating the store management system functionality
public class StoreApplication {

    private final Store store;
    private final GoodsServiceImpl goodsService;
    private final CashdeskServiceImpl cashDeskService;
    private final StoreServiceImpl storeService;
    private final FileServiceImpl fileService;
    private final Scanner scanner;
    private final File receiptDir;

    public StoreApplication() {
        // Initialize store with configuration
        try {
            StoreConfig config = new StoreConfig(
                    new BigDecimal("0.20"), // 20% markup for groceries
                    new BigDecimal("0.25"), // 25% markup for non-food items
                    5, // 5 days before expiry for discount
                    new BigDecimal("0.30") // 30% discount for near-expiry items
            );

            store = new Store(config);
        } catch (Exception e) {
            System.err.println("Error initializing store configuration: " + e.getMessage());
            throw new RuntimeException("Failed to initialize store", e);
        }

        goodsService = new GoodsServiceImpl(store);
        cashDeskService = new CashdeskServiceImpl(store);
        storeService = new StoreServiceImpl(store);
        fileService = new FileServiceImpl();
        scanner = new Scanner(System.in);

        // Simplified directory creation
        receiptDir = new File("receipts");
        if (!receiptDir.exists()) {
            boolean created = receiptDir.mkdirs();
            if (!created) {
                System.err.println("Warning: Failed to create receipts directory");
            }
        }
    }

    public void start() {
        try {
            System.out.println("============================================");
            System.out.println("      STORE MANAGEMENT SYSTEM");
            System.out.println("============================================");

            // Initialize store with sample data
            initializeStore();

            int choice;
            do {
                printMenu();
                choice = getIntInput("Select option: ");

                try {
                    processChoice(choice);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }

                System.out.println();
            } while (choice != 0);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private void initializeStore() {
        // Add cashiers
        cashDeskService.addCashier(new Cashier("C1", "John Smith", new BigDecimal("1200")));
        cashDeskService.addCashier(new Cashier("C2", "Mary Johnson", new BigDecimal("1150")));
        cashDeskService.addCashier(new Cashier("C3", "Peter Brown", new BigDecimal("1100")));

        // Add Cash Desks
        cashDeskService.addCashDesk(new CashDesk());
        cashDeskService.addCashDesk(new CashDesk());
        System.out.println("Added 2 cash desks.");

        // Add products with validation handling
        LocalDate today = LocalDate.now();

        // Food products
        addProductSafely(new FoodProduct("F1", "Milk", new BigDecimal("1.5"), today.plusDays(10), 50));
        addProductSafely(new FoodProduct("F2", "Bread", new BigDecimal("1.0"), today.plusDays(3), 40));
        addProductSafely(new FoodProduct("F3", "Eggs", new BigDecimal("2.5"), today.plusDays(15), 30));
        addProductSafely(new FoodProduct("F4", "Cheese", new BigDecimal("3.5"), today.plusDays(20), 25));
        addProductSafely(new FoodProduct("F5", "Yogurt", new BigDecimal("1.2"), today.plusDays(4), 35));

        // Near expiry product
        addProductSafely(new FoodProduct("F6", "Tomatoes", new BigDecimal("2.0"), today.plusDays(2), 15));

        // Non-food products
        addProductSafely(new NonFoodProduct("N1", "Soap", new BigDecimal("1.8"), today.plusYears(1), 40));
        addProductSafely(new NonFoodProduct("N2", "Toothpaste", new BigDecimal("2.2"), today.plusYears(2), 30));
        addProductSafely(new NonFoodProduct("N3", "Shampoo", new BigDecimal("4.0"), today.plusYears(1), 20));

        System.out.println("Store initialized with sample data.");
    }

    private void addProductSafely(Product product) {
        if (!goodsService.addProduct(product)) {
            System.err.println("Warning: Product with ID " + product.getId() + " already exists, skipping.");
        }
    }

    private void printMenu() {
        System.out.println("\n=== MENU ===");
        System.out.println("1. List all products");
        System.out.println("2. List all cashiers");
        System.out.println("3. List all cash desks");
        System.out.println("4. Assign cashier to desk");
        System.out.println("5. Release cashier from desk");
        System.out.println("6. Make a sale");
        System.out.println("7. View store financial status");
        System.out.println("8. View all receipts");
        System.out.println("9. View receipt count");
        System.out.println("0. Exit");
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1 ->
                listProducts();
            case 2 ->
                listCashiers();
            case 3 ->
                listCashDesks();
            case 4 ->
                assignCashierToSelectedDesk();
            case 5 ->
                releaseCashierFromSelectedDesk();
            case 6 ->
                makeSale();
            case 7 ->
                viewFinancialStatus();
            case 8 ->
                viewAllReceipts();
            case 9 ->
                System.out.println("Total receipts issued: " + store.getReceiptCount());
            case 0 ->
                System.out.println("Exiting application...");
            default ->
                System.out.println("Invalid option, please try again.");
        }
    }

    private void listProducts() {
        List<Product> products = goodsService.listProducts();

        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }

        System.out.println("\n=== PRODUCT INVENTORY ===");
        System.out.printf("%-5s %-20s %-10s %-12s %-10s %-10s\n",
                "ID", "Name", "Type", "Price", "Expiry", "Quantity");
        System.out.println("-------------------------------------------------------------------------");

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Product p : products) {
            BigDecimal salePrice = p.salePrice(store.getConfig(), today);
            String type = p.getType().toString();
            System.out.printf("%-5s %-20s %-10s $%-11.2f %-10s %-10d\n",
                    p.getId(),
                    p.getName(),
                    type,
                    salePrice,
                    p.getExpiry().format(formatter),
                    p.getQuantity());
        }
    }

    private void listCashiers() {
        List<Cashier> cashiers = cashDeskService.listCashiers();

        if (cashiers.isEmpty()) {
            System.out.println("No cashiers available.");
            return;
        }

        System.out.println("\n=== CASHIERS ===");
        System.out.printf("%-5s %-20s %-15s %-15s\n", "ID", "Name", "Monthly Salary", "Assigned Desk");
        System.out.println("---------------------------------------------------------------");

        for (Cashier c : cashiers) {
            Optional<CashDesk> assignedDesk = cashDeskService.getAssignedDeskForCashier(c.getId());
            String deskInfo = assignedDesk.map(CashDesk::getId).orElse("None");
            System.out.printf("%-5s %-20s $%-14.2f %-15s\n", c.getId(), c.getName(), c.getMonthlySalary(), deskInfo);
        }
    }

    private void listCashDesks() {
        List<CashDesk> desks = cashDeskService.listCashDesks();
        if (desks.isEmpty()) {
            System.out.println("No cash desks available.");
            return;
        }
        System.out.println("\n=== CASH DESKS ===");
        System.out.printf("%-5s %-10s %-25s\n", "ID", "Is Open?", "Current Cashier");
        System.out.println("---------------------------------------------");
        for (CashDesk desk : desks) {
            String cashierName = desk.getCurrentCashier() != null ? desk.getCurrentCashier().getName() + " (" + desk.getCurrentCashier().getId() + ")" : "None";
            System.out.printf("%-5s %-10s %-25s\n", desk.getId(), desk.isOpen(), cashierName);
        }
    }

    private void assignCashierToSelectedDesk() {
        listCashiers();
        String cashierId = getStringInput("Enter ID of cashier to assign: ");
        Optional<Cashier> cashierOpt = cashDeskService.findCashierById(cashierId);
        if (cashierOpt.isEmpty()) {
            System.out.println("Cashier not found.");
            return;
        }

        listCashDesks();
        String deskId = getStringInput("Enter ID of cash desk to assign to: ");
        Optional<CashDesk> deskOpt = cashDeskService.findCashDeskById(deskId);
        if (deskOpt.isEmpty()) {
            System.out.println("Cash desk not found.");
            return;
        }

        try {
            cashDeskService.assignCashierToDesk(cashierId, deskId);
        } catch (Exception e) {
            System.err.println("Error assigning cashier: " + e.getMessage());
        }
    }

    private void releaseCashierFromSelectedDesk() {
        listCashDesks();
        String deskId = getStringInput("Enter ID of cash desk to release: ");
        Optional<CashDesk> deskOpt = cashDeskService.findCashDeskById(deskId);
        if (deskOpt.isEmpty()) {
            System.out.println("Cash desk not found or no cashier assigned.");
            return;
        }
        if (!deskOpt.get().isOccupied()) {
            System.out.println("Desk " + deskId + " is already free.");
            return;
        }

        try {
            cashDeskService.releaseCashierFromDesk(deskId);
        } catch (Exception e) {
            System.err.println("Error releasing cashier: " + e.getMessage());
        }
    }

    private void makeSale() {
        System.out.println("\n=== MAKE A SALE ===");

        // Select cashier
        listCashiers();
        String cashierId = getStringInput("Enter Cashier ID: ");

        Optional<Cashier> cashierOpt = store.findCashierById(cashierId);
        if (cashierOpt.isEmpty()) {
            System.err.println("Cashier not found with ID: " + cashierId);
            return;
        }

        Cashier cashier = cashierOpt.get();

        // Check if cashier is assigned to an active desk
        Optional<CashDesk> assignedDesk = store.getAssignedDeskForCashier(cashierId);
        if (assignedDesk.isEmpty()) {
            System.err.println("Cashier " + cashier.getName() + " is not assigned to any open cash desk.");
            System.err.println("Please assign the cashier to a desk first (Option 4).");
            return;
        }

        // Create customer with balance
        BigDecimal customerBalance = getCustomerBalanceInput();
        Customer customer = new Customer("CUST-001", "Walk-in Customer", customerBalance);

        // Create receipt for the purchase
        Receipt receipt;
        try {
            receipt = storeService.createReceipt(cashier);
        } catch (Exception e) {
            System.err.println("Error creating receipt: " + e.getMessage());
            return;
        }

        // Ensure we have a valid receipt before continuing
        if (receipt == null) {
            System.err.println("Failed to create receipt. Cannot proceed with sale.");
            return;
        }

        // Add products to receipt
        String choice;
        do {
            try {
                listProducts();
                String productId = getStringInput("Enter Product ID to add: ");
                int quantity = getIntInput("Enter quantity: ");

                try {
                    storeService.addToReceipt(receipt, productId, quantity, customer);

                    // Save receipt file after each addition
                    try {
                        receipt.save(receiptDir);
                        System.out.println("Product added successfully to receipt.");
                    } catch (IOException e) {
                        System.err.println("Warning: Could not save receipt file: " + e.getMessage());
                    }
                } catch (InsufficientBudgetException e) {
                    System.err.println("Insufficient customer funds: " + e.getMessage());

                    // Complete sale with items already added
                    if (!receipt.getLines().isEmpty()) {
                        System.out.println("Customer can afford items already added to receipt.");
                        System.out.println("Final Receipt:\n" + receipt);
                        return;
                    }
                } catch (ProductNotFoundException e) {
                    System.err.println("Product not found: " + e.getMessage());
                } catch (ProductExpiredException e) {
                    System.err.println("Product expired: " + e.getMessage());
                } catch (InsufficientQuantityException e) {
                    System.err.println("Insufficient quantity: " + e.getMessage());
                }

                // Ask if user wants to add more items
                choice = getStringInput("Add another product? (y/n): ").toLowerCase();
            } catch (RuntimeException e) {
                System.err.println("Error processing sale: " + e.getMessage());
                choice = "n";
            }
        } while ("y".equals(choice) || "yes".equals(choice));

        // Add to existing receipt
        if (!receipt.getLines().isEmpty()) {
            System.out.println("\n=== FINAL RECEIPT ===");
            System.out.println(receipt);

            try {
                // Save final receipt
                receipt.save(receiptDir);
                System.out.println("Receipt saved successfully.");
            } catch (IOException e) {
                System.err.println("Warning: Could not save receipt file: " + e.getMessage());
            }
        } else {
            System.out.println("No items were added to the receipt.");
        }
    }

    private void viewFinancialStatus() {
        System.out.println("\n=== FINANCIAL STATUS ===");
        System.out.printf("Total turnover (Revenue)         : $%.2f\n", store.turnover());
        System.out.printf("Cost of goods sold (COGS)      : $%.2f\n", store.costOfSoldGoods());
        System.out.printf("Gross Profit (Turnover - COGS) : $%.2f\n",
                store.turnover().subtract(store.costOfSoldGoods()));
        System.out.printf("Monthly salary costs             : $%.2f\n", store.salaryExpenses());
        System.out.printf("Operating Profit                 : $%.2f\n", store.profit());
        System.out.println("---------------------------------------------------");
        System.out.printf("Total Cost of All Goods Supplied : $%.2f\n", store.getTotalCostOfAllGoodsSupplied());

        Map<String, Integer> soldItems = store.getSoldItems();
        if (!soldItems.isEmpty()) {
            System.out.println("\n=== SOLD ITEMS ===");
            System.out.printf("%-5s %-20s %-10s\n", "ID", "Quantity", "Product");
            System.out.println("----------------------------------------");

            for (Map.Entry<String, Integer> entry : soldItems.entrySet()) {
                Product p = store.find(entry.getKey());
                String productName = p != null ? p.getName() : "Unknown";
                System.out.printf("%-5s %-20d %-10s\n", entry.getKey(), entry.getValue(), productName);
            }
        }
    }

    private void viewAllReceipts() {
        // Simplified file operations with better error handling
        try {
            List<Receipt> receipts = fileService.loadAll(receiptDir);

            if (receipts.isEmpty()) {
                System.out.println("No receipts available.");
                return;
            }

            System.out.println("\n=== ALL RECEIPTS ===");
            for (Receipt r : receipts) {
                System.out.println("Receipt #" + r.getNumber() + " - Cashier: "
                        + r.getCashier().getName() + " - Total: $" + String.format("%.2f", r.total()));
            }

            int receiptNumber = getIntInput("Enter receipt number to view details (0 to cancel): ");
            if (receiptNumber > 0) {
                Receipt r = fileService.load(receiptDir, receiptNumber);
                if (r != null) {
                    System.out.println("\n" + r);
                } else {
                    System.out.println("Receipt not found.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading receipts: " + e.getMessage());
        }
    }

    // Input validation with retry loop for integer values
    private int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            System.out.print(prompt);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    // Input validation with retry loop for BigDecimal values
    private BigDecimal getCustomerBalanceInput() {
        System.out.print("Enter customer balance: $");
        while (!scanner.hasNextBigDecimal()) {
            System.out.println("Please enter a valid number.");
            System.out.print("Enter customer balance: $");
            scanner.next();
        }
        BigDecimal value = scanner.nextBigDecimal();
        scanner.nextLine();
        return value;
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static void main(String[] args) {
        StoreApplication app = new StoreApplication();
        app.start();
    }
}
