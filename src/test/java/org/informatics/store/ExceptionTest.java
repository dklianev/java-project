package org.informatics.store;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Product;
import org.informatics.entity.Receipt;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExceptionTest {

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
    void testSaleWithNonExistentProductThrowsNotFoundException() {
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> store.sell(cashier, "INVALID_ID", 1, customer));
        
        assertTrue(exception.getMessage().contains("INVALID_ID"));
    }

    @Test
    void testSaleWithExpiredProductThrowsExpiredException() {
        store.addProduct(new FoodProduct("P1", "Expired Milk", new BigDecimal("2.00"), 
                LocalDate.now().minusDays(1), 5));

        ProductExpiredException exception = assertThrows(ProductExpiredException.class,
                () -> store.sell(cashier, "P1", 1, customer));
        
        assertTrue(exception.getMessage().contains("P1"));
    }

    @Test
    void testSaleWithInsufficientQuantityThrowsException() {
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 5));

        InsufficientQuantityException exception = assertThrows(InsufficientQuantityException.class,
                () -> store.sell(cashier, "P1", 10, customer));
        
        assertTrue(exception.getMessage().contains("P1"));
        assertTrue(exception.getMessage().contains("10"));
        assertTrue(exception.getMessage().contains("5"));
    }

    @Test
    void testSaleWithInsufficientBudgetThrowsException() {
        store.addProduct(new NonFoodProduct("P1", "Expensive Item", new BigDecimal("50.00"), 
                LocalDate.MAX, 5));
        Customer poorCustomer = new Customer("CU2", "Poor Customer", new BigDecimal("10.00"));

        InsufficientBudgetException exception = assertThrows(InsufficientBudgetException.class,
                () -> store.sell(cashier, "P1", 1, poorCustomer));
        
        assertTrue(exception.getMessage().contains("Insufficient budget"));
    }

    @Test
    void testSaleWithZeroQuantityThrowsIllegalArgumentException() {
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 10));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> store.sell(cashier, "P1", 0, customer));
        
        assertEquals("Quantity must be positive: 0", exception.getMessage());
    }

    @Test
    void testSaleWithNegativeQuantityThrowsIllegalArgumentException() {
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 10));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> store.sell(cashier, "P1", -1, customer));
        
        assertEquals("Quantity must be positive: -1", exception.getMessage());
    }

    @Test
    void testSaleWithUnassignedCashierThrowsIllegalStateException() {
        Cashier unassignedCashier = new Cashier("C2", "Unassigned Cashier", new BigDecimal("1000"));
        store.addCashier(unassignedCashier);
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 10));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> store.sell(unassignedCashier, "P1", 1, customer));
        
        assertEquals("Cashier Unassigned Cashier is not assigned to an open cash desk.", 
                exception.getMessage());
    }

    @Test
    void testAddDuplicateProductReturnsFalse() {
        FoodProduct product1 = new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 5);
        FoodProduct product2 = new FoodProduct("P1", "Another Product", new BigDecimal("3.00"), 
                LocalDate.now().plusDays(10), 3);

        boolean firstAdd = store.addProduct(product1);
        boolean secondAdd = store.addProduct(product2);

        assertTrue(firstAdd);
        assertFalse(secondAdd);
    }

    @Test
    void testReceiptWithNullProductThrowsIllegalArgumentException() {
        Receipt receipt = new Receipt(cashier);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(null, 1, BigDecimal.ONE));
        
        assertEquals("Product cannot be null", exception.getMessage());
    }

    @Test
    void testReceiptWithZeroQuantityThrowsIllegalArgumentException() {
        Receipt receipt = new Receipt(cashier);
        Product product = new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 5);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(product, 0, BigDecimal.ONE));
        
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    void testReceiptWithNegativePriceThrowsIllegalArgumentException() {
        Receipt receipt = new Receipt(cashier);
        Product product = new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 5);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(product, 1, new BigDecimal("-1.0")));
        
        assertEquals("Price cannot be negative", exception.getMessage());
    }

    @Test
    void testStoreConfigWithNegativeGroceriesMarkupThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new StoreConfig(new BigDecimal("-0.1"), new BigDecimal("0.25"), 
                        3, new BigDecimal("0.30")));
        
        assertEquals("Groceries markup cannot be negative", exception.getMessage());
    }

    @Test
    void testStoreConfigWithNegativeNonFoodMarkupThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new StoreConfig(new BigDecimal("0.20"), new BigDecimal("-0.1"), 
                        3, new BigDecimal("0.30")));
        
        assertEquals("Non-foods markup cannot be negative", exception.getMessage());
    }

    @Test
    void testStoreConfigWithNegativeDaysThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new StoreConfig(new BigDecimal("0.20"), new BigDecimal("0.25"), 
                        -1, new BigDecimal("0.30")));
        
        assertEquals("Days for near expiry discount cannot be negative", exception.getMessage());
    }

    @Test
    void testStoreConfigWithInvalidDiscountPercentageThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new StoreConfig(new BigDecimal("0.20"), new BigDecimal("0.25"), 
                        3, new BigDecimal("1.5")));
        
        assertEquals("Discount percentage must be between 0 and 1", exception.getMessage());
    }
}
