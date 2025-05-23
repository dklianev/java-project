package org.informatics.store;

import java.io.IOException;
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
    public void setUp() {
        try {
            StoreConfig config = new StoreConfig(
                    new BigDecimal("0.20"),
                    new BigDecimal("0.25"),
                    3,
                    new BigDecimal("0.30")
            );
            store = new Store(config);
            cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
            customer = new Customer("CU1", "Test Customer", new BigDecimal("100"));
            store.addCashier(cashier);

            // Create cash desk and assign cashier to allow selling
            CashDesk desk = new CashDesk();
            store.addCashDesk(desk);
            try {
                store.assignCashierToDesk(cashier.getId(), desk.getId());
            } catch (Exception e) {
                fail("Failed to set up test environment: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            fail("Failed to set up store configuration: " + e.getMessage());
        }
    }

    @Test
    void testShouldThrowProductNotFoundException() {
        try {
            store.sell(cashier, "INVALID_ID", 1, customer);
            fail("Expected ProductNotFoundException was not thrown");
        } catch (ProductNotFoundException ex) {
            // Test passed - expected exception
        } catch (ProductExpiredException | InsufficientQuantityException
                | InsufficientBudgetException | IOException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowIllegalArgumentExceptionForZeroQuantity() {
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 10));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> store.sell(cashier, "P1", 0, customer));
        assertEquals("Quantity must be positive: 0", exception.getMessage());
    }

    @Test
    void testShouldThrowIllegalArgumentExceptionForNegativeQuantity() {
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 10));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> store.sell(cashier, "P1", -1, customer));
        assertEquals("Quantity must be positive: -1", exception.getMessage());
    }

    @Test
    void testShouldThrowInsufficientQuantityException() {
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 5));

        try {
            store.sell(cashier, "P1", 10, customer);
            fail("Expected InsufficientQuantityException was not thrown");
        } catch (InsufficientQuantityException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | ProductExpiredException
                | InsufficientBudgetException | IOException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowProductExpiredException() {
        store.addProduct(new FoodProduct("P1", "Expired Milk", new BigDecimal("2.0"), LocalDate.now().minusDays(1), 5));

        try {
            store.sell(cashier, "P1", 1, customer);
            fail("Expected ProductExpiredException was not thrown");
        } catch (ProductExpiredException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | InsufficientQuantityException
                | InsufficientBudgetException | IOException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowInsufficientBudgetException() {
        store.addProduct(new NonFoodProduct("P1", "Expensive Item", new BigDecimal("50.0"), LocalDate.MAX, 5));

        try {
            Customer poorCustomer = new Customer("CU2", "Poor Customer", new BigDecimal("10.0"));
            store.sell(cashier, "P1", 1, poorCustomer);
            fail("Expected InsufficientBudgetException was not thrown");
        } catch (InsufficientBudgetException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | ProductExpiredException
                | InsufficientQuantityException | IOException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldReturnFalseForDuplicateProduct() {
        // addProduct now returns boolean instead of throwing exception
        boolean firstAdd = store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 5));
        boolean secondAdd = store.addProduct(new FoodProduct("P1", "Another Product", new BigDecimal("3.0"), LocalDate.now().plusDays(10), 3));

        assertTrue(firstAdd, "First product should be added successfully");
        assertFalse(secondAdd, "Second product with same ID should return false");
    }

    @Test
    void testShouldThrowIllegalStateExceptionForUnassignedCashier() {
        Cashier unassignedCashier = new Cashier("C2", "Unassigned Cashier", new BigDecimal("1000"));
        store.addCashier(unassignedCashier);
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 10));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> store.sell(unassignedCashier, "P1", 1, customer));
        assertEquals("Cashier Unassigned Cashier is not assigned to an open cash desk.", exception.getMessage());
    }

    @Test
    void testShouldThrowIllegalArgumentExceptionForNullProduct() {
        Receipt receipt = new Receipt(cashier);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(null, 1, BigDecimal.ONE));
        assertEquals("Product cannot be null", exception.getMessage());
    }

    @Test
    void testShouldThrowIllegalArgumentExceptionForNonPositiveQuantity() {
        Receipt receipt = new Receipt(cashier);
        Product product = new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 5);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(product, 0, BigDecimal.ONE));
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    void testShouldThrowIllegalArgumentExceptionForNegativePrice() {
        Receipt receipt = new Receipt(cashier);
        Product product = new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 5);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> receipt.add(product, 1, new BigDecimal("-1.0")));
        assertEquals("Price cannot be negative", exception.getMessage());
    }

    @Test
    void testShouldThrowIllegalArgumentExceptionForNegativeMarkup() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new StoreConfig(new BigDecimal("-0.1"), new BigDecimal("0.25"), 3, new BigDecimal("0.30")));
        assertEquals("Groceries markup cannot be negative", exception.getMessage());
    }

    @Test
    void testShouldThrowIllegalArgumentExceptionForNegativeDays() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new StoreConfig(new BigDecimal("0.2"), new BigDecimal("0.25"), -1, new BigDecimal("0.30")));
        assertEquals("Days for near expiry discount cannot be negative", exception.getMessage());
    }
}
