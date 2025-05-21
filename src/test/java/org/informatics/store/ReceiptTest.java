package org.informatics.store;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Receipt;
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.DuplicateProductException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.service.impl.FileServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ReceiptTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;
    private CashDesk cashDesk;
    private FileServiceImpl fileService;

    @TempDir
    File tempDir;

    @BeforeEach
    public void setUp() {
        // Reset the receipt counter before each test
        Receipt.resetCounter();
        
        // Common setup for all tests
        StoreConfig config = new StoreConfig(
            new BigDecimal("0.20"), 
            new BigDecimal("0.25"), 
            3, 
            new BigDecimal("0.30")
        );
        store = new Store(config);
        cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
        customer = new Customer("CU1", "Test Customer", new BigDecimal("200"));
        fileService = new FileServiceImpl();

        // Create cash desk and assign cashier
        cashDesk = new CashDesk();
        store.addCashier(cashier);
        store.addCashDesk(cashDesk);

        try {
            store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
        } catch (Exception e) {
            fail("Failed to set up test environment: " + e.getMessage());
        }
    }

    @Test
    void testReceiptCreationAndFields() {
        Receipt receipt = new Receipt(cashier);

        assertEquals(cashier, receipt.getCashier(), "Receipt should have the correct cashier");
        assertNotNull(receipt.getTime(), "Receipt should have a timestamp");
        assertTrue(receipt.getNumber() > 0, "Receipt should have a positive receipt number");
        assertEquals(0, receipt.getLines().size(), "New receipt should have no items");
        assertEquals(BigDecimal.ZERO, receipt.total(), "New receipt should have zero total");
    }

    @Test
    void testReceiptAddItemAndTotal() {
        // Create a receipt and add items
        Receipt receipt = new Receipt(cashier);

        // Add a product to the receipt
        BigDecimal price = new BigDecimal("2.50");
        receipt.add(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 5), 2, price);

        // Check the receipt details
        assertEquals(1, receipt.getLines().size(), "Receipt should have one item");
        assertEquals(price.multiply(new BigDecimal("2")), receipt.total(), "Receipt total should be calculated correctly");
    }

    @Test
    void testReceiptGenerationAfterSale() {
        try {
            // Add products to the store
            store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 10));
            store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.0"), LocalDate.now().plusYears(1), 5));

            // Sell products and get the receipt
            Receipt receipt1 = store.sell(cashier, "F1", 2, customer);
            Receipt receipt2 = store.sell(cashier, "N1", 1, customer);

            // Check receipt counts
            assertEquals(2, Receipt.getReceiptCount(), "Receipt counter should be incremented");
            assertEquals(2, store.listReceipts().size(), "Store should track all receipts");

            // Validate the receipts
            assertNotNull(receipt1, "Receipt should be generated after sale");
            assertNotNull(receipt2, "Receipt should be generated after sale");
            assertEquals(cashier, receipt1.getCashier(), "Receipt should have the correct cashier");
            assertEquals(1, receipt1.getLines().size(), "Receipt should have one product line");

        } catch (DuplicateProductException | ProductNotFoundException | ProductExpiredException
                | InvalidQuantityException | InsufficientQuantityException | InsufficientBudgetException
                | IOException | CashDeskNotAssignedException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testReceiptSaveAndLoad() {
        try {
            // Add products to store
            store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 10));
            store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.0"), LocalDate.now().plusYears(1), 5));

            // Make sales and get receipts
            Receipt receipt1 = store.sell(cashier, "F1", 2, customer);
            Receipt receipt2 = store.sell(cashier, "N1", 1, customer);

            // Save receipts
            receipt1.save(tempDir);
            receipt2.save(tempDir);

            // Verify files were created
            File textFile1 = new File(tempDir, "receipt-" + receipt1.getNumber() + ".txt");
            File serFile1 = new File(tempDir, "receipt-" + receipt1.getNumber() + ".ser");
            File textFile2 = new File(tempDir, "receipt-" + receipt2.getNumber() + ".txt");
            File serFile2 = new File(tempDir, "receipt-" + receipt2.getNumber() + ".ser");

            assertTrue(textFile1.exists(), "Text receipt file should be created");
            assertTrue(serFile1.exists(), "Serialized receipt file should be created");
            assertTrue(textFile2.exists(), "Text receipt file should be created");
            assertTrue(serFile2.exists(), "Serialized receipt file should be created");

            // Load receipts back
            Receipt loadedReceipt = fileService.load(tempDir, receipt1.getNumber());

            assertNotNull(loadedReceipt, "Should be able to load saved receipt");
            assertEquals(receipt1.getNumber(), loadedReceipt.getNumber(), "Loaded receipt should have the same number");
            assertEquals(receipt1.getCashier().getId(), loadedReceipt.getCashier().getId(), "Loaded receipt should have the same cashier");
            assertEquals(receipt1.getLines().size(), loadedReceipt.getLines().size(), "Loaded receipt should have the same number of items");
            assertEquals(receipt1.total(), loadedReceipt.total(), "Loaded receipt should have the same total");

            // Load all receipts
            List<Receipt> loadedReceipts = fileService.loadAll(tempDir);
            assertEquals(2, loadedReceipts.size(), "Should load all receipts from directory");

        } catch (DuplicateProductException | ProductNotFoundException | ProductExpiredException
                | InvalidQuantityException | InsufficientQuantityException | InsufficientBudgetException
                | IOException | CashDeskNotAssignedException | ClassNotFoundException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testReceiptToString() {
        Receipt receipt = new Receipt(cashier);
        receipt.add(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 5), 2, new BigDecimal("2.50"));

        String receiptString = receipt.toString();

        assertNotNull(receiptString, "Receipt toString should not be null");
        assertTrue(receiptString.contains("RECEIPT #"), "Receipt should include receipt number");
        assertTrue(receiptString.contains("Date:"), "Receipt should include date");
        assertTrue(receiptString.contains("Cashier:"), "Receipt should include cashier");
        assertTrue(receiptString.contains("Milk"), "Receipt should include product name");
        assertTrue(receiptString.contains("TOTAL:"), "Receipt should include total");
    }
}
