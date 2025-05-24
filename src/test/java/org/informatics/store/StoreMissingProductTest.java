package org.informatics.store;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.Product;
import org.informatics.entity.Receipt;
import org.informatics.exception.ProductNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoreMissingProductTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;

    @BeforeEach
    void setUp() {
        StoreConfig config = new StoreConfig(
                new BigDecimal("0.20"), // 20% food markup
                new BigDecimal("0.25"), // 25% non-food markup
                3, // near expiry days
                new BigDecimal("0.30") // near expiry discount
        );
        store = new Store(config);
        cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
        customer = new Customer("CU1", "Test Customer", new BigDecimal("100"));
        
        store.addCashier(cashier);
        CashDesk desk = new CashDesk();
        store.addCashDesk(desk);
        
        try {
            store.assignCashierToDesk(cashier.getId(), desk.getId());
        } catch (Exception e) {
            fail("Failed to set up test environment: " + e.getMessage());
        }
    }

    @Test
    void testSaleWithInvalidProductIdThrowsNotFoundException() {
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> store.sell(cashier, "INVALID_ID", 1, customer));
        
        assertTrue(exception.getMessage().contains("INVALID_ID"));
    }

    @Test
    void testSaleWithMultipleInvalidProductIds() {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 10));

        String[] invalidIds = {"INVALID_1", "INVALID_2", "INVALID_3"};

        for (String productId : invalidIds) {
            ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                    () -> store.sell(cashier, productId, 1, customer));
            
            assertTrue(exception.getMessage().contains(productId));
        }
    }

    @Test
    void testFindNonExistentProductReturnsNull() {
        Product result = store.find("NON_EXISTENT");
        
        assertNull(result);
    }

    @Test
    void testFindExistingProductReturnsProduct() {
        FoodProduct milk = new FoodProduct("F1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 10);
        store.addProduct(milk);

        Product result = store.find("F1");

        assertNotNull(result);
        assertEquals("F1", result.getId());
        assertEquals("Milk", result.getName());
    }

    @Test
    void testSaleWithValidProductAfterInvalidAttempt() throws Exception {
        // First try invalid product
        assertThrows(ProductNotFoundException.class,
                () -> store.sell(cashier, "INVALID", 1, customer));

        // Then add valid product and try again
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 10));

        Receipt receipt = store.sell(cashier, "F1", 1, customer);

        assertNotNull(receipt);
        assertEquals(1, receipt.getLines().size());
    }

    @Test
    void testAddToReceiptWithInvalidProductThrowsNotFoundException() {
        Receipt receipt = store.createReceipt(cashier);

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> store.addToReceipt(receipt, "INVALID_ID", 1, customer));
        
        assertTrue(exception.getMessage().contains("INVALID_ID"));
    }
}
