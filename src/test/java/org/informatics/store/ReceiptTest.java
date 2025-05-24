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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ReceiptTest {

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
        customer = new Customer("CU1", "Test Customer", new BigDecimal("500"));
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

    @Test
    void testReceiptCreationWithCashier() {
        Receipt receipt = new Receipt(cashier);

        assertEquals(cashier, receipt.getCashier());
        assertNotNull(receipt.getTime());
        assertTrue(receipt.getNumber() > 0);
    }

    @Test
    void testReceiptWithNoItems() {
        Receipt receipt = new Receipt(cashier);

        assertEquals(0, receipt.getLines().size());
        assertEquals(BigDecimal.ZERO, receipt.total());
    }

    @Test
    void testReceiptWithSingleItem() {
        Receipt receipt = new Receipt(cashier);
        FoodProduct product = new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5);
        BigDecimal price = new BigDecimal("2.50");

        receipt.add(product, 2, price);

        assertEquals(1, receipt.getLines().size());
        assertEquals(price.multiply(new BigDecimal("2")), receipt.total());
    }

    @Test
    void testReceiptWithMultipleItems() {
        Receipt receipt = new Receipt(cashier);
        FoodProduct milk = new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5);
        NonFoodProduct soap = new NonFoodProduct("N1", "Soap", new BigDecimal("3.00"), LocalDate.now().plusYears(1), 3);

        receipt.add(milk, 2, new BigDecimal("2.40"));
        receipt.add(soap, 1, new BigDecimal("3.75"));

        assertEquals(2, receipt.getLines().size());
        assertEquals(new BigDecimal("8.55"), receipt.total()); // 2*2.40 + 1*3.75
    }

    @Test
    void testReceiptGeneratedAfterSale() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));

        Receipt receipt = store.sell(cashier, "F1", 2, customer);

        assertNotNull(receipt);
        assertEquals(cashier, receipt.getCashier());
        assertEquals(1, receipt.getLines().size());
    }

    @Test
    void testReceiptCounterIncrement() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));

        Receipt receipt1 = store.sell(cashier, "F1", 1, customer);
        Receipt receipt2 = store.sell(cashier, "F1", 1, customer);

        assertEquals(2, Receipt.getReceiptCount());
        assertEquals(2, store.listReceipts().size());
        assertTrue(receipt2.getNumber() > receipt1.getNumber());
    }

    @Test
    void testReceiptSaveAsTextFile() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt receipt = store.sell(cashier, "F1", 2, customer);

        receipt.save(tempDir);

        File textFile = new File(tempDir, "receipt-" + receipt.getNumber() + ".txt");
        assertTrue(textFile.exists());
    }

    @Test
    void testReceiptSaveAsSerializedFile() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt receipt = store.sell(cashier, "F1", 2, customer);

        receipt.save(tempDir);

        File serFile = new File(tempDir, "receipt-" + receipt.getNumber() + ".ser");
        assertTrue(serFile.exists());
    }

    @Test
    void testReceiptLoadFromSerializedFile() throws Exception {
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
    void testLoadAllReceiptsFromDirectory() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.00"), LocalDate.now().plusYears(1), 5));

        Receipt receipt1 = store.sell(cashier, "F1", 2, customer);
        Receipt receipt2 = store.sell(cashier, "N1", 1, customer);
        receipt1.save(tempDir);
        receipt2.save(tempDir);

        List<Receipt> loadedReceipts = fileService.loadAll(tempDir);

        assertEquals(2, loadedReceipts.size());
    }

    @Test
    void testReceiptToStringContainsRequiredFields() {
        Receipt receipt = new Receipt(cashier);
        FoodProduct product = new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5);
        receipt.add(product, 2, new BigDecimal("2.50"));

        String receiptString = receipt.toString();

        assertNotNull(receiptString);
        assertTrue(receiptString.contains("RECEIPT #"));
        assertTrue(receiptString.contains("Date:"));
        assertTrue(receiptString.contains("Cashier:"));
        assertTrue(receiptString.contains("Milk"));
        assertTrue(receiptString.contains("TOTAL:"));
    }

    @Test
    void testReceiptWithNullProductThrowsException() {
        Receipt receipt = new Receipt(cashier);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(null, 1, BigDecimal.ONE));
        assertEquals("Product cannot be null", exception.getMessage());
    }

    @Test
    void testReceiptWithNegativeQuantityThrowsException() {
        Receipt receipt = new Receipt(cashier);
        FoodProduct product = new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(product, 0, BigDecimal.ONE));
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    void testReceiptWithNegativePriceThrowsException() {
        Receipt receipt = new Receipt(cashier);
        FoodProduct product = new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(product, 1, new BigDecimal("-1.0")));
        assertEquals("Price cannot be negative", exception.getMessage());
    }
}
