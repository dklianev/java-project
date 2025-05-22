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
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.DuplicateProductException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.service.impl.FileServiceImpl;
import org.informatics.service.impl.GoodsServiceImpl;
import org.informatics.service.impl.StoreServiceImpl;
import org.informatics.store.Store;

//Main application class demonstrating the store management system functionality.
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
        StoreConfig config = new StoreConfig(
                new BigDecimal("0.20"), // 20% markup for groceries
                new BigDecimal("0.25"), // 25% markup for non-food items
                5, // 5 days before expiry for discount
                new BigDecimal("0.30") // 30% discount for near-expiry items
        );

        store = new Store(config);
        goodsService = new GoodsServiceImpl(store);
        cashDeskService = new CashdeskServiceImpl(store);
        storeService = new StoreServiceImpl(store);
        fileService = new FileServiceImpl();
        scanner = new Scanner(System.in);
        receiptDir = new File("receipts");
        receiptDir.mkdirs();
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
        try {
            cashDeskService.addCashier(new Cashier("C1", "John Smith", new BigDecimal("1200")));
            cashDeskService.addCashier(new Cashier("C2", "Mary Johnson", new BigDecimal("1150")));
            cashDeskService.addCashier(new Cashier("C3", "Peter Brown", new BigDecimal("1100")));

            // Add Cash Desks
            cashDeskService.addCashDesk(new CashDesk());
            cashDeskService.addCashDesk(new CashDesk());
            System.out.println("Added 2 cash desks.");

            // Add products
            LocalDate today = LocalDate.now();

            // Food products
            goodsService.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("1.5"), today.plusDays(10), 50));
            goodsService.addProduct(new FoodProduct("F2", "Bread", new BigDecimal("1.0"), today.plusDays(3), 40));
            goodsService.addProduct(new FoodProduct("F3", "Eggs", new BigDecimal("2.5"), today.plusDays(15), 30));
            goodsService.addProduct(new FoodProduct("F4", "Cheese", new BigDecimal("3.5"), today.plusDays(20), 25));
            goodsService.addProduct(new FoodProduct("F5", "Yogurt", new BigDecimal("1.2"), today.plusDays(4), 35));

            // Near expiry product
            goodsService.addProduct(new FoodProduct("F6", "Tomatoes", new BigDecimal("2.0"), today.plusDays(2), 15));

            // Non-food products
            goodsService.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("1.8"), today.plusYears(1), 40));
            goodsService.addProduct(new NonFoodProduct("N2", "Toothpaste", new BigDecimal("2.2"), today.plusYears(2), 30));
            goodsService.addProduct(new NonFoodProduct("N3", "Shampoo", new BigDecimal("4.0"), today.plusYears(1), 20));

            System.out.println("Store initialized with sample data.");
        } catch (DuplicateProductException e) {
            System.err.println("Error initializing store: " + e.getMessage());
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

    private void processChoice(int choice) throws Exception {
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
                    salePrice.doubleValue(),
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
            System.out.printf("%-5s %-20s $%-14.2f %-15s\n", c.getId(), c.getName(), c.getMonthlySalary().doubleValue(), deskInfo);
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

    private void assignCashierToSelectedDesk() throws Exception {
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

    private void releaseCashierFromSelectedDesk() throws Exception {
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

    private void makeSale() throws Exception {
        // 1. Select cashier
        listCashiers();
        String cashierId = getStringInput("Enter ID of cashier making the sale: ");
        Cashier selectedCashier = cashDeskService.findCashierById(cashierId)
                .orElse(null);

        if (selectedCashier == null) {
            System.out.println("Invalid cashier ID.");
            return;
        }

        // Check if cashier is assigned to an active desk
        Optional<CashDesk> activeDeskOpt = cashDeskService.getAssignedDeskForCashier(selectedCashier.getId());
        if (activeDeskOpt.isEmpty()) {
            System.out.println("Cashier " + selectedCashier.getName() + " is not currently assigned to an open cash desk.");
            System.out.println("Please assign the cashier to a desk first (Option 4).");
            return;
        }
        System.out.println("Cashier " + selectedCashier.getName() + " is at desk " + activeDeskOpt.get().getId() + ".");

        // 2. Create customer
        String customerId = "CU" + System.currentTimeMillis() % 10000;
        String customerName = getStringInput("Enter customer name: ");
        BigDecimal customerBalance = getBigDecimalInput("Enter customer balance: $");

        Customer customer = new Customer(customerId, customerName, customerBalance);
        System.out.printf("Initial customer balance: $%.2f\n", customer.getBalance().doubleValue());

        // 3. Create a single receipt for the entire purchase
        Receipt currentReceipt;
        try {
            currentReceipt = storeService.createReceipt(selectedCashier);
        } catch (CashDeskNotAssignedException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        // Ensure we have a valid receipt before continuing
        if (currentReceipt == null) {
            System.err.println("Failed to create receipt. Sale cancelled.");
            return;
        }

        // 4. Add products to receipt
        boolean addMoreProducts = true;

        while (addMoreProducts) {
            try {
                listProducts();
                String productId = getStringInput("Enter product ID to add (or 'done' to finish): ");

                if (productId.equalsIgnoreCase("done")) {
                    addMoreProducts = false;
                    // Save the final receipt once when done adding products
                    try {
                        storeService.saveReceipt(currentReceipt, receiptDir);
                        System.out.println("\n--- FINAL RECEIPT ---");
                        System.out.println(currentReceipt.toString());
                        System.out.println("---------------------");
                    } catch (IOException e) {
                        System.err.println("Error saving receipt: " + e.getMessage());
                    }
                    System.out.printf("Remaining customer balance: $%.2f\n", customer.getBalance().doubleValue());
                    continue;
                }

                Product product = store.find(productId);
                if (product == null) {
                    System.out.println("Product not found.");
                    continue;
                }

                int quantity = getIntInput("Enter quantity: ");
                if (quantity <= 0) {
                    System.out.println("Quantity must be positive.");
                    continue;
                }

                // Add to existing receipt
                currentReceipt = storeService.addToReceipt(currentReceipt, productId, quantity, customer);

                System.out.println("Product added successfully!");
                System.out.println("Current receipt total: $" + currentReceipt.total().doubleValue());

            } catch (ProductNotFoundException | ProductExpiredException
                    | InvalidQuantityException | InsufficientQuantityException
                    | InsufficientBudgetException e) {
                System.err.println("Sale Error: " + e.getMessage());
                if (e instanceof InsufficientBudgetException) {
                    addMoreProducts = false;
                }
            } catch (IOException e) {
                System.err.println("File Error: " + e.getMessage());
                addMoreProducts = false;
            }
        }
    }

    private void viewFinancialStatus() {
        System.out.println("\n=== FINANCIAL STATUS ===");
        System.out.printf("Total turnover (Revenue)         : $%.2f\n", store.turnover().doubleValue());
        System.out.printf("Cost of goods sold (COGS)      : $%.2f\n", store.costOfSoldGoods().doubleValue());
        System.out.printf("Gross Profit (Turnover - COGS) : $%.2f\n", 
                store.turnover().subtract(store.costOfSoldGoods()).doubleValue());
        System.out.printf("Monthly salary costs             : $%.2f\n", store.salaryExpenses().doubleValue());
        System.out.printf("Operating Profit                 : $%.2f\n", store.profit().doubleValue());
        System.out.println("---------------------------------------------------");
        System.out.printf("Total Cost of All Goods Supplied : $%.2f\n", store.getTotalCostOfAllGoodsSupplied().doubleValue());

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
        try {
            List<Receipt> receipts = fileService.loadAll(receiptDir);

            if (receipts.isEmpty()) {
                System.out.println("No receipts available.");
                return;
            }

            System.out.println("\n=== ALL RECEIPTS ===");
            for (Receipt r : receipts) {
                System.out.println("Receipt #" + r.getNumber() + " - Cashier: "
                        + r.getCashier().getName() + " - Total: $" + r.total().doubleValue());
            }

            int receiptNumber = getIntInput("Enter receipt number to view details (0 to cancel): ");
            if (receiptNumber > 0) {
                Receipt r = fileService.load(receiptDir, receiptNumber);
                if (r != null) {
                    System.out.println("\n" + r.toString());
                } else {
                    System.out.println("Receipt not found.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading receipts: " + e.getMessage());
        }
    }

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

    private BigDecimal getBigDecimalInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextBigDecimal()) {
            System.out.println("Please enter a valid number.");
            System.out.print(prompt);
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
