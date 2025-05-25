package org.informatics.app;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Product;
import org.informatics.entity.Receipt;
import org.informatics.service.impl.FileServiceImpl;
import org.informatics.store.Store;

/**
 * Comprehensive demonstration of the Store Management System
 * Shows all core functionality in a clear, structured way
 */
public class StoreDemo {
    
    // Business configuration constants
    private static final BigDecimal FOOD_MARKUP_RATE = new BigDecimal("0.20");
    private static final BigDecimal NON_FOOD_MARKUP_RATE = new BigDecimal("0.25");
    private static final int NEAR_EXPIRY_DAYS_THRESHOLD = 3;
    private static final BigDecimal NEAR_EXPIRY_DISCOUNT_RATE = new BigDecimal("0.30");

    public static void main(String[] args) {
        System.out.println("=== COMPREHENSIVE STORE MANAGEMENT DEMO ===\n");

        try {
            // 1. Initialize store with configuration
            StoreConfig config = new StoreConfig(
                    FOOD_MARKUP_RATE,           // 20% food markup
                    NON_FOOD_MARKUP_RATE,       // 25% non-food markup
                    NEAR_EXPIRY_DAYS_THRESHOLD, // 3 days for near-expiry discount
                    NEAR_EXPIRY_DISCOUNT_RATE   // 30% discount
            );
            Store store = new Store(config);
            System.out.println("✓ Store initialized with configuration");

            // 2. Setup cashiers and cash desks
            setupCashiersAndDesks(store);

            // 3. Add products and show inventory
            addProducts(store);
            showInventory(store, "INITIAL INVENTORY");

            // 4. Create customers
            Customer customer1 = new Customer("CUST1", "Ivan Petrov", new BigDecimal("200"));
            Customer customer2 = new Customer("CUST2", "Maria Dimitrova", new BigDecimal("100"));
            System.out.println("✓ Created customers with budgets");

            // 5. Simple single-item sales
            performSingleItemSales(store, customer1, customer2);

            // 6. Multi-item receipt demonstration
            performMultiItemSale(store, customer1);

            // 7. Show updated inventory
            showInventory(store, "INVENTORY AFTER SALES");

            // 8. File operations demonstration
            demonstrateFileOperations(store);

            // 9. Financial summary
            showDetailedFinancialSummary(store);

            // 10. Exception handling
            demonstrateExceptions(store, store.listCashiers().get(0), customer1);

            System.out.println("\n=== DEMO COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupCashiersAndDesks(Store store) throws Exception {
        Cashier cashier1 = new Cashier("C1", "Petya Nikolova", new BigDecimal("120"));
        Cashier cashier2 = new Cashier("C2", "Georgi Stoyanov", new BigDecimal("110"));
        store.addCashier(cashier1);
        store.addCashier(cashier2);

        CashDesk desk1 = new CashDesk();
        CashDesk desk2 = new CashDesk();
        store.addCashDesk(desk1);
        store.addCashDesk(desk2);
        
        store.assignCashierToDesk(cashier1.getId(), desk1.getId());
        System.out.println("✓ Added 2 cashiers and 2 cash desks, assigned Petya to desk " + desk1.getId());
    }

    private static void addProducts(Store store) {
        LocalDate today = LocalDate.now();

        // Regular food products
        store.addProduct(new FoodProduct("F1", "Whole Milk", new BigDecimal("2.50"), today.plusDays(7), 20));
        store.addProduct(new FoodProduct("F2", "White Bread", new BigDecimal("1.80"), today.plusDays(5), 15));

        // Near-expiry food product (should get discount)
        store.addProduct(new FoodProduct("F3", "Greek Yogurt", new BigDecimal("3.00"), today.plusDays(2), 10));

        // Non-food products
        store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("4.00"), today.plusYears(1), 25));
        store.addProduct(new NonFoodProduct("N2", "Toothpaste", new BigDecimal("5.50"), today.plusYears(2), 30));
    }

    private static void showInventory(Store store, String title) {
        System.out.println("\n=== " + title + " ===");
        List<Product> products = store.listProducts();
        System.out.printf("%-4s %-15s %-8s %-10s %-10s\n", "ID", "Name", "Type", "Price", "Quantity");
        System.out.println("--------------------------------------------------");
        
        LocalDate today = LocalDate.now();
        for (Product p : products) {
            BigDecimal salePrice = p.salePrice(store.getConfig(), today);
            String type = p.getType().toString().substring(0, 4); // First 4 chars
            System.out.printf("%-4s %-15s %-8s $%-9.2f %-10d\n",
                p.getId(), p.getName(), type, salePrice, p.getQuantity());
        }
    }

    private static void performSingleItemSales(Store store, Customer customer1, Customer customer2) throws Exception {
        System.out.println("\n=== SINGLE-ITEM SALES ===");
        Cashier cashier = store.listCashiers().get(0);

        // Sale 1: Regular food product
        Receipt receipt1 = store.sell(cashier, "F1", 2, customer1);
        System.out.println("Sale 1: " + receipt1.getLines().get(0).product().getName() 
                + " x2 = $" + receipt1.total());

        // Sale 2: Near-expiry product (should get discount)
        Receipt receipt2 = store.sell(cashier, "F3", 1, customer1);
        System.out.println("Sale 2: " + receipt2.getLines().get(0).product().getName() 
                + " (near-expiry discount) = $" + receipt2.total());

        // Sale 3: Non-food product
        Receipt receipt3 = store.sell(cashier, "N1", 1, customer2);
        System.out.println("Sale 3: " + receipt3.getLines().get(0).product().getName() 
                + " = $" + receipt3.total());

        // Additional sales for better turnover
        Receipt receipt4 = store.sell(cashier, "F2", 3, customer1); // More white bread
        System.out.println("Sale 4: " + receipt4.getLines().get(0).product().getName() 
                + " x3 = $" + receipt4.total());

        Receipt receipt5 = store.sell(cashier, "N2", 2, customer1); // More toothpaste
        System.out.println("Sale 5: " + receipt5.getLines().get(0).product().getName() 
                + " x2 = $" + receipt5.total());
    }

    private static void performMultiItemSale(Store store, Customer customer) throws Exception {
        System.out.println("\n=== MULTI-ITEM RECEIPT DEMO ===");
        Cashier cashier = store.listCashiers().get(0);

        // Create receipt and add multiple items
        Receipt multiReceipt = store.createReceipt(cashier);
        
        store.addToReceipt(multiReceipt, "F2", 2, customer); // White Bread x2
        store.addToReceipt(multiReceipt, "N2", 1, customer); // Toothpaste x1
        store.addToReceipt(multiReceipt, "F1", 1, customer); // Whole Milk x1

        System.out.println("Multi-item sale completed. Items: " + multiReceipt.getLines().size() 
                + ", Total: $" + multiReceipt.total());
        
        System.out.println("\n=== DETAILED MULTI-ITEM RECEIPT ===");
        System.out.println(multiReceipt.toString());
    }

    private static void demonstrateFileOperations(Store store) {
        System.out.println("\n=== FILE OPERATIONS DEMO ===");
        File receiptDir = new File("receipts");
        receiptDir.mkdirs();

        try {
            // Save all receipts
            List<Receipt> receipts = store.listReceipts();
            for (Receipt r : receipts) {
                r.save(receiptDir);
            }
            System.out.println("✓ Saved " + receipts.size() + " receipts to files (.txt and .ser)");

            // Demonstrate file loading
            FileServiceImpl fileService = new FileServiceImpl();
            List<Receipt> loadedReceipts = fileService.loadAll(receiptDir);
            System.out.println("✓ Loaded " + loadedReceipts.size() + " receipts from files");

            // Load specific receipt
            if (!loadedReceipts.isEmpty()) {
                Receipt firstReceipt = loadedReceipts.get(0);
                Receipt loadedSpecific = fileService.load(receiptDir, firstReceipt.getNumber());
                System.out.println("✓ Successfully loaded receipt #" + loadedSpecific.getNumber() 
                        + " with total $" + loadedSpecific.total());
            }

        } catch (Exception e) {
            System.err.println("File operations error: " + e.getMessage());
        }
    }

    private static void showDetailedFinancialSummary(Store store) {
        System.out.println("\n=== DETAILED FINANCIAL SUMMARY ===");
        System.out.printf("Total Turnover:         $%8.2f\n", store.turnover());
        System.out.printf("Salary Expenses:        $%8.2f\n", store.salaryExpenses());
        System.out.printf("Cost of Sold Goods:     $%8.2f\n", store.costOfSoldGoods());
        System.out.printf("Profit/Loss:            $%8.2f\n", store.profit());
        System.out.printf("Total Receipts Issued:  %8d\n", store.getReceiptCount());
        System.out.printf("Total Cashiers:         %8d\n", store.listCashiers().size());
        System.out.printf("Total Products:         %8d\n", store.listProducts().size());
    }

    private static void demonstrateExceptions(Store store, Cashier cashier, Customer customer) {
        System.out.println("\n=== EXCEPTION HANDLING DEMO ===");

        try {
            // Try to sell non-existent product
            store.sell(cashier, "INVALID", 1, customer);
        } catch (Exception e) {
            System.out.println("✓ ProductNotFoundException: " + e.getMessage());
        }

        try {
            // Try to sell more than available
            store.sell(cashier, "F1", 100, customer);
        } catch (Exception e) {
            System.out.println("✓ InsufficientQuantityException: " + e.getMessage());
        }

        try {
            // Try to sell expensive item to customer with insufficient funds
            Customer poorCustomer = new Customer("POOR", "Poor Customer", new BigDecimal("1.00"));
            store.sell(cashier, "N2", 1, poorCustomer);
        } catch (Exception e) {
            System.out.println("✓ InsufficientBudgetException: " + e.getMessage());
        }

        try {
            // Try to sell with unassigned cashier
            Cashier unassignedCashier = store.listCashiers().get(1); // Georgi is not assigned
            store.sell(unassignedCashier, "F1", 1, customer);
        } catch (Exception e) {
            System.out.println("✓ IllegalStateException: " + e.getMessage());
        }

        try {
            // Try to sell expired product
            LocalDate yesterday = LocalDate.now().minusDays(1);
            store.addProduct(new FoodProduct("EXP1", "Expired Milk", new BigDecimal("2.00"), yesterday, 5));
            store.sell(cashier, "EXP1", 1, customer);
        } catch (Exception e) {
            System.out.println("✓ ProductExpiredException: " + e.getMessage());
        }

        try {
            // Try to assign cashier to occupied desk
            Cashier newCashier = new Cashier("C3", "Stefan Ivanov", new BigDecimal("1000"));
            store.addCashier(newCashier);
            
            // Petya is already assigned to desk D1, try to assign Stefan to same desk
            CashDesk occupiedDesk = store.listCashDesks().get(0); // Desk where Petya works
            store.assignCashierToDesk(newCashier.getId(), occupiedDesk.getId());
        } catch (Exception e) {
            System.out.println("✓ CashDeskOccupiedException: " + e.getMessage());
        }

        try {
            // Try to sell with zero/negative quantity
            store.sell(cashier, "F1", 0, customer);
        } catch (Exception e) {
            System.out.println("✓ IllegalArgumentException: " + e.getMessage());
        }
    }
} 