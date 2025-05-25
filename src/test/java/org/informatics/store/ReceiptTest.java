package org.informatics.store;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.Receipt;
import org.informatics.service.impl.FileServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReceiptTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;
    private FileServiceImpl fileService;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() throws Exception {
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
        store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
    }

    @Test
    void testReceiptCreationWithCashier() {
        // Arrange & Act
        Receipt receipt = new Receipt(cashier);

        // Assert
        assertEquals(cashier, receipt.getCashier());
        assertNotNull(receipt.getTime());
        assertTrue(receipt.getNumber() > 0);
    }

    @Test
    void testReceiptWithSingleItem() {
        // Arrange
        Receipt receipt = new Receipt(cashier);
        FoodProduct product = new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5);
        BigDecimal price = new BigDecimal("2.50");

        // Act
        receipt.add(product, 2, price);

        // Assert
        assertEquals(1, receipt.getLines().size());
        assertEquals(price.multiply(new BigDecimal("2")), receipt.total());
    }

    @Test
    void testReceiptGeneratedAfterSale() throws Exception {
        // Arrange
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));

        // Act
        Receipt receipt = store.sell(cashier, "F1", 2, customer);

        // Assert
        assertNotNull(receipt);
        assertEquals(cashier, receipt.getCashier());
        assertEquals(1, receipt.getLines().size());
    }

    @Test
    void testReceiptSaveAsTextFile() throws Exception {
        // Arrange
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt receipt = store.sell(cashier, "F1", 2, customer);

        // Act
        receipt.save(tempDir);

        // Assert
        File textFile = new File(tempDir, "receipt-" + receipt.getNumber() + ".txt");
        assertTrue(textFile.exists());
    }

    @Test
    void testReceiptSaveAsSerializedFile() throws Exception {
        // Arrange
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt receipt = store.sell(cashier, "F1", 2, customer);

        // Act
        receipt.save(tempDir);

        // Assert
        File serFile = new File(tempDir, "receipt-" + receipt.getNumber() + ".ser");
        assertTrue(serFile.exists());
    }

    @Test
    void testReceiptLoadFromSerializedFile() throws Exception {
        // Arrange
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        Receipt originalReceipt = store.sell(cashier, "F1", 2, customer);
        originalReceipt.save(tempDir);

        // Act
        Receipt loadedReceipt = fileService.load(tempDir, originalReceipt.getNumber());

        // Assert
        assertNotNull(loadedReceipt);
        assertEquals(originalReceipt.getNumber(), loadedReceipt.getNumber());
        assertEquals(originalReceipt.getCashier().getId(), loadedReceipt.getCashier().getId());
        assertEquals(originalReceipt.getLines().size(), loadedReceipt.getLines().size());
        assertEquals(originalReceipt.total(), loadedReceipt.total());
    }
}
