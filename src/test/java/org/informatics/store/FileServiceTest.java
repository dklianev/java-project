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
import org.informatics.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class FileServiceTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;
    private FileServiceImpl fileService;

    @TempDir
    File tempDir;

    @BeforeEach
    public void setUp() {
        StoreConfig config = new StoreConfig(
            new BigDecimal("0.20"), 
            new BigDecimal("0.25"), 
            3, 
            new BigDecimal("0.30")
        );
        store = new Store(config);
        cashier = new Cashier("C1", "Bob", new BigDecimal("1000"));
        customer = new Customer("CU1", "Ann", new BigDecimal("100"));
        fileService = new FileServiceImpl();

        // Create cash desk and assign cashier
        CashDesk cashDesk = new CashDesk();
        store.addCashier(cashier);
        store.addCashDesk(cashDesk);

        try {
            store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
        } catch (Exception e) {
            fail("Failed to set up test environment: " + e.getMessage());
        }
    }

    @Test
    void testSaveAndLoadReceipt() {
        try {
            // Setup product and receipt
            Product notebook = new NonFoodProduct("N1", "Notebook", new BigDecimal("1"), LocalDate.MAX, 5);
            store.addProduct(notebook);

            // Generate a receipt by selling the product
            Receipt receipt = store.sell(cashier, "N1", 1, customer);

            // Save the receipt
            fileService.save(receipt, tempDir);

            // Verify loading all receipts returns the correct amount
            List<Receipt> loadedReceipts = fileService.loadAll(tempDir);
            assertEquals(1, loadedReceipts.size(), "Should load 1 receipt from the directory");

            // Verify loading specific receipt
            Receipt loadedReceipt = fileService.load(tempDir, receipt.getNumber());
            assertNotNull(loadedReceipt, "Should be able to load the saved receipt");
            assertEquals(receipt.getNumber(), loadedReceipt.getNumber(), "Receipt number should match");

        } catch (DuplicateProductException | ProductNotFoundException | ProductExpiredException
                | InvalidQuantityException | InsufficientQuantityException | InsufficientBudgetException
                | IOException | CashDeskNotAssignedException | ClassNotFoundException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testLoadingNonExistentReceipt() {
        try {
            // Attempt to load a receipt that doesn't exist
            Receipt nonExistentReceipt = fileService.load(tempDir, 999);

            // Should return null if receipt not found
            assertNull(nonExistentReceipt, "Should return null for non-existent receipt");

        } catch (IOException | ClassNotFoundException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
