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
import org.informatics.service.contract.CashdeskService;
import org.informatics.service.contract.FileService;
import org.informatics.service.contract.FinancialService;
import org.informatics.service.contract.GoodsService;
import org.informatics.service.contract.StoreService;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.service.impl.FileServiceImpl;
import org.informatics.service.impl.FinancialServiceImpl;
import org.informatics.service.impl.GoodsServiceImpl;
import org.informatics.service.impl.StoreServiceImpl;
import org.informatics.store.Store;

/**
 * Store System Demo
 */
public class StoreDemo {
    
    // Config values
    private static final BigDecimal FOOD_MARKUP_RATE = new BigDecimal("0.20");
    private static final BigDecimal NON_FOOD_MARKUP_RATE = new BigDecimal("0.25");
    private static final int NEAR_EXPIRY_DAYS_THRESHOLD = 3;
    private static final BigDecimal NEAR_EXPIRY_DISCOUNT_RATE = new BigDecimal("0.30");

    // Services
    private static StoreService storeService;
    private static GoodsService goodsService;
    private static CashdeskService cashdeskService;
    private static FinancialService financialService;
    private static FileService fileService;

    public static void main(String[] args) {
        System.out.println("=== STORE DEMO ===\n");

        try {
            // Setup store
            // 1. Setup store with config
            StoreConfig config = new StoreConfig(
                    FOOD_MARKUP_RATE,           // 20% food markup
                    NON_FOOD_MARKUP_RATE,       // 25% non-food markup
                    NEAR_EXPIRY_DAYS_THRESHOLD, // 3 days for near-expiry discount
                    NEAR_EXPIRY_DISCOUNT_RATE   // 30% discount
            );
            Store store = new Store(config);
            
            // 2. Initialize services
            initializeServices(store);
            System.out.println("✓ Store and services initialized");

            // 3. Show store config
            demonstrateStoreConfiguration();

            // Add cashiers and products
            // 4. Setup cashiers and cash desks
            setupCashiersAndDesks();

            // 5. Add products to inventory
            addProducts();
            showInventory("INITIAL INVENTORY");

            // 6. Product search
            demonstrateProductSearch();

            // Create customers
            // 7. Create customers
            Customer customer1 = new Customer("CUST1", "Ivan Petrov", new BigDecimal("200"));
            Customer customer2 = new Customer("CUST2", "Maria Dimitrova", new BigDecimal("100"));
            System.out.println("✓ Created customers with budgets");

            // Sales
            // 8. Simple single-item sales
            performSingleItemSales(customer1, customer2);

            // 9. Multi-item receipt
            performMultiItemSale(customer1);

            // Other tasks
            // 10. Cashdesk work
            demonstrateCashdeskManagement();

            // 11. Show updated inventory after sales
            showInventory("INVENTORY AFTER SALES");

            // Reports and files
            // 12. File work
            demonstrateFileOperations();

            // 13. Financial summary
            showFinancialSummary();

            // Test exceptions
            // 14. Exception handling
            demonstrateExceptions(customer1);

            System.out.println("\n=== DEMO COMPLETED ===");

        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
        }
    }

    private static void initializeServices(Store store) {
        storeService = new StoreServiceImpl(store);
        goodsService = new GoodsServiceImpl(store);
        cashdeskService = new CashdeskServiceImpl(store);
        financialService = new FinancialServiceImpl(store);
        fileService = new FileServiceImpl();
    }

    private static void setupCashiersAndDesks() throws Exception {
        Cashier cashier1 = new Cashier("C1", "Petya Nikolova", new BigDecimal("120"));
        Cashier cashier2 = new Cashier("C2", "Georgi Stoyanov", new BigDecimal("110"));
        cashdeskService.addCashier(cashier1);
        cashdeskService.addCashier(cashier2);

        CashDesk desk1 = new CashDesk();
        CashDesk desk2 = new CashDesk();
        cashdeskService.addCashDesk(desk1);
        cashdeskService.addCashDesk(desk2);
        
        cashdeskService.assignCashierToDesk(cashier1.getId(), desk1.getId());
        System.out.println("✓ Added 2 cashiers and 2 cash desks, assigned Petya to desk " + desk1.getId());
    }

    private static void addProducts() {
        LocalDate today = LocalDate.now();

        // Regular food products
        boolean success1 = goodsService.addProduct(new FoodProduct("F1", "Whole Milk", new BigDecimal("2.50"), today.plusDays(7), 20));
        boolean success2 = goodsService.addProduct(new FoodProduct("F2", "White Bread", new BigDecimal("1.80"), today.plusDays(5), 15));

        // Near-expiry food product (should get discount)
        boolean success3 = goodsService.addProduct(new FoodProduct("F3", "Greek Yogurt", new BigDecimal("3.00"), today.plusDays(2), 10));

        // Non-food products
        boolean success4 = goodsService.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("4.00"), today.plusYears(1), 25));
        boolean success5 = goodsService.addProduct(new NonFoodProduct("N2", "Toothpaste", new BigDecimal("5.50"), today.plusYears(2), 30));
        
        // Show results
        int successCount = (success1 ? 1 : 0) + (success2 ? 1 : 0) + (success3 ? 1 : 0) + (success4 ? 1 : 0) + (success5 ? 1 : 0);
        System.out.println("✓ Added " + successCount + "/5 products to inventory");
        
        // Try duplicate ID
        boolean duplicateAttempt = goodsService.addProduct(new FoodProduct("F1", "Duplicate Milk", new BigDecimal("3.00"), today.plusDays(5), 10));
        if (!duplicateAttempt) {
            System.out.println("✓ Correctly prevented duplicate product ID 'F1'");
        }
    }

    private static void showInventory(String title) {
        System.out.println("\n=== " + title + " ===");
        List<Product> products = goodsService.listProducts();
        System.out.printf("%-4s %-15s %-8s %-10s %-10s\n", "ID", "Name", "Type", "Price", "Quantity");
        System.out.println("--------------------------------------------------");
        
        for (Product p : products) {
            System.out.printf("%-4s %-15s %-8s $%-9.2f %-10d\n",
                p.getId(), p.getName(), p.getType().toString().substring(0, 4), 
                p.getPurchasePrice(), p.getQuantity());
        }
    }

    private static void performSingleItemSales(Customer customer1, Customer customer2) throws Exception {
        System.out.println("\n=== SINGLE-ITEM SALES ===");
        Cashier cashier = cashdeskService.listCashiers().getFirst();

        // Sale 1: Regular food product
        Receipt receipt1 = storeService.sell(cashier, "F1", 2, customer1);
        System.out.println("Sale 1: " + receipt1.getLines().getFirst().product().getName()
                + " x2 = $" + receipt1.total());

        // Sale 2: Near-expiry product (should get discount)
        Receipt receipt2 = storeService.sell(cashier, "F3", 1, customer1);
        System.out.println("Sale 2: " + receipt2.getLines().getFirst().product().getName()
                + " (near-expiry discount) = $" + receipt2.total());

        // Sale 3: Non-food product
        Receipt receipt3 = storeService.sell(cashier, "N1", 1, customer2);
        System.out.println("Sale 3: " + receipt3.getLines().getFirst().product().getName()
                + " = $" + receipt3.total());

        // More sales
        Receipt receipt4 = storeService.sell(cashier, "F2", 3, customer1); // More white bread
        System.out.println("Sale 4: " + receipt4.getLines().getFirst().product().getName()
                + " x3 = $" + receipt4.total());

        Receipt receipt5 = storeService.sell(cashier, "N2", 2, customer1); // More toothpaste
        System.out.println("Sale 5: " + receipt5.getLines().getFirst().product().getName()
                + " x2 = $" + receipt5.total());
    }

    private static void performMultiItemSale(Customer customer) throws Exception {
        System.out.println("\n=== MULTI-ITEM RECEIPT DEMO ===");
        Cashier cashier = cashdeskService.listCashiers().getFirst();

        // Create receipt and add multiple items
        Receipt multiReceipt = storeService.createReceipt(cashier);
        
        Receipt updatedReceipt1 = storeService.addToReceipt(multiReceipt, "F2", 2, customer); // White Bread x2
        Receipt updatedReceipt2 = storeService.addToReceipt(updatedReceipt1, "N2", 1, customer); // Toothpaste x1
        Receipt finalReceipt = storeService.addToReceipt(updatedReceipt2, "F1", 1, customer); // Whole Milk x1

        System.out.println("Multi-item sale completed. Items: " + finalReceipt.getLines().size() 
                + ", Total: $" + finalReceipt.total());
        
        System.out.println("\n=== MULTI-ITEM RECEIPT ===");
        System.out.println(finalReceipt);
    }

    private static void demonstrateFileOperations() {
        System.out.println("\n=== FILE WORK DEMO ===");
        File receiptDir = new File("receipts");
        if (!receiptDir.exists() && !receiptDir.mkdirs()) {
            System.out.println("Warning: Could not create receipts directory");
            return;
        }

        try {
            // Save all receipts
            List<Receipt> receipts = storeService.listReceipts();
            for (Receipt r : receipts) {
                storeService.saveReceipt(r, receiptDir);
            }
            System.out.println("✓ Saved " + receipts.size() + " receipts to files (.txt and .ser)");

            // Load files
            List<Receipt> loadedReceipts = fileService.loadAll(receiptDir);
            System.out.println("✓ Loaded " + loadedReceipts.size() + " receipts from files");

            // Load specific receipt
            if (!loadedReceipts.isEmpty()) {
                Receipt firstReceipt = loadedReceipts.getFirst();
                Receipt loadedSpecific = fileService.load(receiptDir, firstReceipt.getNumber());
                System.out.println("✓ Loaded receipt #" + loadedSpecific.getNumber() 
                        + " with total $" + loadedSpecific.total());
            }

        } catch (Exception e) {
            System.err.println("File error: " + e.getMessage());
        }
    }

    private static void demonstrateStoreConfiguration() {
        System.out.println("\n=== STORE CONFIG DEMO ===");
        StoreConfig config = storeService.getConfig();
        
        System.out.println("--- Store Settings ---");
        System.out.printf("Food Markup Rate:           %.1f%%\n", config.groceriesMarkup().multiply(new BigDecimal("100")));
        System.out.printf("Non-Food Markup Rate:       %.1f%%\n", config.nonFoodsMarkup().multiply(new BigDecimal("100")));
        System.out.printf("Near-Expiry Threshold:      %d days\n", config.daysForNearExpiryDiscount());
        System.out.printf("Near-Expiry Discount:       %.1f%%\n", config.discountPercentage().multiply(new BigDecimal("100")));
        
        System.out.println("✓ Store config shown");
    }

    private static void showFinancialSummary() {
        System.out.println("\n=== FINANCIAL SUMMARY ===");
        System.out.printf("Total Turnover:         $%8.2f\n", financialService.turnover());
        System.out.printf("Salary Expenses:        $%8.2f\n", financialService.salaryExpenses());
        System.out.printf("Cost of Sold Goods:     $%8.2f\n", financialService.costOfSoldGoods());
        System.out.printf("Total Goods Investment: $%8.2f\n", financialService.getTotalCostOfAllGoodsSupplied());
        System.out.printf("Profit/Loss:            $%8.2f\n", financialService.profit());
        System.out.printf("Total Receipts Issued:  %8d\n", financialService.getReceiptCount());
        System.out.printf("Total Cashiers:         %8d\n", cashdeskService.listCashiers().size());
        System.out.printf("Total Products:         %8d\n", goodsService.listProducts().size());
        
        // Sales report by product
        System.out.println("\n--- Sales Report by Product ---");
        var soldItems = financialService.getSoldItems();
        if (soldItems.isEmpty()) {
            System.out.println("○ No sales recorded yet");
        } else {
            System.out.printf("%-4s %-15s %-10s\n", "ID", "Product Name", "Qty Sold");
            System.out.println("-------------------------------");
            for (var entry : soldItems.entrySet()) {
                String productId = entry.getKey();
                int quantitySold = entry.getValue();
                Product product = goodsService.find(productId);
                String productName = product != null ? product.getName() : "Unknown";
                System.out.printf("%-4s %-15s %-10d\n", productId, productName, quantitySold);
            }
            System.out.println("✓ Sales report generated");
        }
    }

    private static void demonstrateCashdeskManagement() throws Exception {
        System.out.println("\n=== CASHDESK DEMO ===");
        
        // 1. Show current cashier assignments
        System.out.println("\n--- Current Cashier Assignments ---");
        List<Cashier> allCashiers = cashdeskService.listCashiers();
        for (Cashier cashier : allCashiers) {
            var assignedDesk = cashdeskService.getAssignedDeskForCashier(cashier.getId());
            if (assignedDesk.isPresent()) {
                System.out.println("✓ " + cashier.getName() + " (ID: " + cashier.getId() + 
                        ") is assigned to desk " + assignedDesk.get().getId());
            } else {
                System.out.println("○ " + cashier.getName() + " (ID: " + cashier.getId() + 
                        ") is not assigned to any desk");
            }
        }

        // 2. Find cashier and desk by ID
        System.out.println("\n--- Search Tests ---");
        var foundCashier = cashdeskService.findCashierById("C1");
        foundCashier.ifPresent(cashier -> System.out.println("✓ Found cashier C1: " + cashier.getName()));
        
        List<CashDesk> allDesks = cashdeskService.listCashDesks();
        if (!allDesks.isEmpty()) {
            String firstDeskId = allDesks.getFirst().getId();
            var foundDesk = cashdeskService.findCashDeskById(firstDeskId);
            foundDesk.ifPresent(cashDesk -> System.out.println("✓ Found desk " + firstDeskId +
                    " (Occupied: " + cashDesk.isOccupied() + ")"));
        }

        // 3. Move Petya to second desk
        System.out.println("\n--- Cashier Reassignment Demo ---");
        if (allDesks.size() >= 2) {
            String secondDeskId = allDesks.get(1).getId();
            System.out.println("Moving Petya from current desk to desk " + secondDeskId);
            
            // Release from current desk
            var petyaCurrentDesk = cashdeskService.getAssignedDeskForCashier("C1");
            if (petyaCurrentDesk.isPresent()) {
                cashdeskService.releaseCashierFromDesk(petyaCurrentDesk.get().getId());
                System.out.println("✓ Released Petya from desk " + petyaCurrentDesk.get().getId());
            }
            
            // Assign to new desk
            cashdeskService.assignCashierToDesk("C1", secondDeskId);
            System.out.println("✓ Assigned Petya to desk " + secondDeskId);
            
            // Verify the change
            var newAssignment = cashdeskService.getAssignedDeskForCashier("C1");
            newAssignment.ifPresent(cashDesk -> System.out.println("✓ Verified: Petya is now on desk " + cashDesk.getId()));
        }

        // 4. Assign second cashier (Georgi) to the first desk
        System.out.println("\n--- Assign Second Cashier ---");
        if (!allDesks.isEmpty()) {
            String firstDeskId = allDesks.getFirst().getId();
            cashdeskService.assignCashierToDesk("C2", firstDeskId);
            System.out.println("✓ Assigned Georgi to desk " + firstDeskId);
        }

        // 5. Show final assignments
        System.out.println("\n--- Final Cashier Assignments ---");
        for (Cashier cashier : allCashiers) {
            var assignedDesk = cashdeskService.getAssignedDeskForCashier(cashier.getId());
            assignedDesk.ifPresent(cashDesk -> System.out.println("✓ " + cashier.getName() + " → Desk " + cashDesk.getId()));
        }
        
        System.out.println("✓ Cashdesk work completed");
    }

    private static void demonstrateExceptions(Customer customer) {
        System.out.println("\n=== EXCEPTION HANDLING DEMO ===");

        try {
            // Try to sell non-existent product
            Cashier cashier = cashdeskService.listCashiers().getFirst();
            storeService.sell(cashier, "INVALID", 1, customer);
        } catch (Exception e) {
            System.out.println("✓ ProductNotFoundException: " + e.getMessage());
        }

        try {
            // Try to sell more than available
            Cashier cashier = cashdeskService.listCashiers().getFirst();
            storeService.sell(cashier, "F1", 100, customer);
        } catch (Exception e) {
            System.out.println("✓ InsufficientQuantityException: " + e.getMessage());
        }

        try {
            // Try to sell expensive item to customer with insufficient funds
            Customer poorCustomer = new Customer("POOR", "Poor Customer", new BigDecimal("1.00"));
            Cashier cashier = cashdeskService.listCashiers().getFirst();
            storeService.sell(cashier, "N2", 1, poorCustomer);
        } catch (Exception e) {
            System.out.println("✓ InsufficientBudgetException: " + e.getMessage());
        }

        try {
            // Try to sell with unassigned cashier
            // Create a NEW cashier for this test since C2 (Georgi) is already assigned
            Cashier unassignedCashier = new Cashier("C_TEMP", "Temporary Cashier", new BigDecimal("100"));
            cashdeskService.addCashier(unassignedCashier);
            // Note: We don't assign this cashier to any desk, so they remain unassigned
            storeService.sell(unassignedCashier, "F1", 1, customer);
        } catch (Exception e) {
            System.out.println("✓ IllegalStateException: " + e.getMessage());
        }

        try {
            // Try to sell expired product
            LocalDate yesterday = LocalDate.now().minusDays(1);
            boolean expiredAdded = goodsService.addProduct(new FoodProduct("EXP1", "Expired Milk", new BigDecimal("2.00"), yesterday, 5));
            if (expiredAdded) {
                Cashier cashier = cashdeskService.listCashiers().getFirst();
                storeService.sell(cashier, "EXP1", 1, customer);
            }
        } catch (Exception e) {
            System.out.println("✓ ProductExpiredException: " + e.getMessage());
        }

        try {
            // Try to assign cashier to occupied desk
            Cashier newCashier = new Cashier("C3", "Stefan Ivanov", new BigDecimal("1000"));
            cashdeskService.addCashier(newCashier);
            
            // Try to assign Stefan to the first desk (which should be occupied by Georgi after reassignment)
            CashDesk occupiedDesk = cashdeskService.listCashDesks().getFirst(); 
            cashdeskService.assignCashierToDesk(newCashier.getId(), occupiedDesk.getId());
        } catch (Exception e) {
            System.out.println("✓ CashDeskOccupiedException: " + e.getMessage());
        }

        try {
            // Try to sell with zero/negative quantity
            Cashier cashier = cashdeskService.listCashiers().getFirst();
            storeService.sell(cashier, "F1", 0, customer);
        } catch (Exception e) {
            System.out.println("✓ IllegalArgumentException: " + e.getMessage());
        }
    }

    private static void demonstrateProductSearch() {
        System.out.println("\n=== PRODUCT SEARCH DEMO ===");
        
        // Show total products
        List<Product> allProducts = goodsService.listProducts();
        System.out.println("✓ Total products in inventory: " + allProducts.size());
        
        // Find products by ID
        System.out.println("\n--- Product Lookup by ID ---");
        String[] productIds = {"F1", "N1", "F3", "INVALID"};
        
        for (String productId : productIds) {
            Product foundProduct = goodsService.find(productId);
            if (foundProduct != null) {
                System.out.printf("✓ Found %-3s: %-15s ($%.2f, qty: %d)\n", 
                    productId, foundProduct.getName(), 
                    foundProduct.getPurchasePrice(), foundProduct.getQuantity());
            } else {
                System.out.println("○ Product ID '" + productId + "' not found");
            }
        }
        
        // Show product categories
        System.out.println("\n--- Product Categories ---");
        long foodCount = allProducts.stream()
            .filter(p -> p.getType().toString().equals("GROCERIES"))
            .count();
        long nonFoodCount = allProducts.size() - foodCount;
        
        System.out.println("✓ Food products: " + foodCount);
        System.out.println("✓ Non-food products: " + nonFoodCount);
        
        System.out.println("✓ Product search completed");
    }
} 