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
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
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

public class FileServiceTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;
    private FileServiceImpl fileService;

    @TempDir
    File tempDir;

    @BeforeEach
    public void setUp() {
        try {
            Receipt.resetCounter();
            
            StoreConfig config = new StoreConfig(
                new BigDecimal("0.20"), 
                new BigDecimal("0.25"), 
                3, 
                new BigDecimal("0.30")
            );
            store = new Store(config);
            cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
            customer = new Customer("CU1", "Test Customer", new BigDecimal("1000"));
            fileService = new FileServiceImpl();

            store.addCashier(cashier);

            CashDesk cashDesk = new CashDesk();
            store.addCashDesk(cashDesk);

            try {
                store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
            } catch (Exception e) {
                fail("Failed to set up test environment: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            fail("Failed to set up store configuration: " + e.getMessage());
        }
    }

    @Test
    void testFileServiceOperations() {
        try {
            // Add products to store
            store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 10));
            store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.0"), LocalDate.now().plusYears(1), 5));

            // Make sales to generate receipts
            Receipt receipt1 = store.sell(cashier, "F1", 2, customer);
            Receipt receipt2 = store.sell(cashier, "N1", 1, customer);

            // Save receipts using fileService
            receipt1.save(tempDir);
            receipt2.save(tempDir);

            // Load all receipts
            List<Receipt> loadedReceipts = fileService.loadAll(tempDir);

            // Validate loaded receipts
            assertEquals(2, loadedReceipts.size(), "Should load all saved receipts");

            // Load specific receipt
            Receipt loadedReceipt = fileService.load(tempDir, receipt1.getNumber());
            assertNotNull(loadedReceipt, "Should load specific receipt by number");
            assertEquals(receipt1.getNumber(), loadedReceipt.getNumber(), "Loaded receipt should have correct number");
            assertEquals(receipt1.getCashier().getId(), loadedReceipt.getCashier().getId(), "Loaded receipt should have correct cashier");

            // Verify files exist
            File txtFile = new File(tempDir, "receipt-" + receipt1.getNumber() + ".txt");
            File serFile = new File(tempDir, "receipt-" + receipt1.getNumber() + ".ser");
            assertTrue(txtFile.exists(), "Text file should exist");
            assertTrue(serFile.exists(), "Serialized file should exist");

        } catch (ProductNotFoundException | ProductExpiredException | InsufficientQuantityException 
                | InsufficientBudgetException | IOException | ClassNotFoundException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
