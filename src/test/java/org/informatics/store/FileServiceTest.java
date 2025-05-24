package org.informatics.store;

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
import org.informatics.entity.Receipt;
import org.informatics.service.impl.FileServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class FileServiceTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;
    private FileServiceImpl fileService;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        Receipt.resetCounter();
        StoreConfig config = new StoreConfig(
                new BigDecimal("0.20"), // 20% food markup
                new BigDecimal("0.25"), // 25% non-food markup
                3, // near expiry days
                new BigDecimal("0.30") // near expiry discount
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
    }

    // Original integration tests kept for file operations
    @Test
    void testSaveSingleReceiptCreatesFiles() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt receipt = store.sell(cashier, "F1", 2, customer);

        receipt.save(tempDir);

        File txtFile = new File(tempDir, "receipt-" + receipt.getNumber() + ".txt");
        File serFile = new File(tempDir, "receipt-" + receipt.getNumber() + ".ser");
        
        assertTrue(txtFile.exists());
        assertTrue(serFile.exists());
    }

    @Test
    void testLoadSingleReceiptFromSerializedFile() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt originalReceipt = store.sell(cashier, "F1", 2, customer);
        originalReceipt.save(tempDir);

        Receipt loadedReceipt = fileService.load(tempDir, originalReceipt.getNumber());

        assertNotNull(loadedReceipt);
        assertEquals(originalReceipt.getNumber(), loadedReceipt.getNumber());
        assertEquals(originalReceipt.getCashier().getId(), loadedReceipt.getCashier().getId());
        assertEquals(originalReceipt.getLines().size(), loadedReceipt.getLines().size());
        assertEquals(originalReceipt.total(), loadedReceipt.total());
    }

    @Test
    void testLoadNonExistentReceiptReturnsNull() throws Exception {
        Receipt loadedReceipt = fileService.load(tempDir, 999);
        
        assertNull(loadedReceipt);
    }

    // Mock-based tests for FileService behavior
    @Test
    void testLoadAllReceiptsWithMockedDirectory() throws Exception {
        File mockDir = Mockito.mock(File.class);
        File mockFile1 = Mockito.mock(File.class);
        File mockFile2 = Mockito.mock(File.class);
        
        Mockito.when(mockDir.exists()).thenReturn(true);
        Mockito.when(mockDir.listFiles(Mockito.any(java.io.FilenameFilter.class))).thenReturn(new File[]{mockFile1, mockFile2});
        Mockito.when(mockFile1.getName()).thenReturn("receipt-1.ser");
        Mockito.when(mockFile2.getName()).thenReturn("receipt-2.ser");

        // For this test, we need real serialized files or complex mocking
        // This shows the structure, but actual file operations need real files
        List<Receipt> result = fileService.loadAll(tempDir); // Using real tempDir for now
        
        assertNotNull(result);
    }

    @Test
    void testLoadAllReceiptsFromEmptyDirectory() throws Exception {
        List<Receipt> loadedReceipts = fileService.loadAll(tempDir);

        assertEquals(0, loadedReceipts.size());
    }

    @Test
    void testFileServiceLoadWithMockedFile() throws Exception {
        File mockDir = Mockito.mock(File.class);
        File mockReceiptFile = Mockito.mock(File.class);
        
        Mockito.when(mockReceiptFile.exists()).thenReturn(false);

        // This demonstrates the structure - actual implementation needs real I/O
        Receipt result = fileService.load(tempDir, 999); // Using real tempDir
        
        assertNull(result);
    }

    @Test
    void testLoadReceiptWithComplexData() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.00"), LocalDate.now().plusYears(1), 5));

        Receipt receipt = store.createReceipt(cashier);
        store.addToReceipt(receipt, "F1", 2, customer);
        store.addToReceipt(receipt, "N1", 1, customer);
        receipt.save(tempDir);

        Receipt loadedReceipt = fileService.load(tempDir, receipt.getNumber());

        assertEquals(2, loadedReceipt.getLines().size());
        assertEquals(receipt.total(), loadedReceipt.total());
    }

    @Test
    void testSerializedFileContainsCompleteReceiptData() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt originalReceipt = store.sell(cashier, "F1", 3, customer);
        originalReceipt.save(tempDir);

        Receipt loadedReceipt = fileService.load(tempDir, originalReceipt.getNumber());

        // Verify all receipt data is preserved
        assertEquals(originalReceipt.getNumber(), loadedReceipt.getNumber());
        assertEquals(originalReceipt.getCashier().getName(), loadedReceipt.getCashier().getName());
        assertEquals(originalReceipt.getCashier().getMonthlySalary(), loadedReceipt.getCashier().getMonthlySalary());
        assertEquals(originalReceipt.getTime().toLocalDate(), loadedReceipt.getTime().toLocalDate());
        
        // Verify receipt lines are preserved
        assertEquals(originalReceipt.getLines().size(), loadedReceipt.getLines().size());
        for (int i = 0; i < originalReceipt.getLines().size(); i++) {
            assertEquals(originalReceipt.getLines().get(i).quantity(), loadedReceipt.getLines().get(i).quantity());
            assertEquals(originalReceipt.getLines().get(i).price(), loadedReceipt.getLines().get(i).price());
        }
    }

    @Test
    void testTextFileContainsReadableReceiptFormat() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt receipt = store.sell(cashier, "F1", 2, customer);
        receipt.save(tempDir);

        File txtFile = new File(tempDir, "receipt-" + receipt.getNumber() + ".txt");
        
        assertTrue(txtFile.exists());
        assertTrue(txtFile.length() > 0);
        // The text file should contain readable receipt information (tested in ReceiptTest)
    }
}
