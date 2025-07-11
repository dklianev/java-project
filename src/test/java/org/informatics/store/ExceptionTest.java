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
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExceptionTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;

    @BeforeEach
    void setUp() throws Exception {
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
        store.assignCashierToDesk(cashier.getId(), desk.getId());
    }

    @Test
    void testSaleWithExpiredProductThrowsExpiredException() {
        // Arrange
        store.addProduct(new FoodProduct("P1", "Expired Milk", new BigDecimal("2.00"), 
                LocalDate.now().minusDays(1), 5));

        // Act
        ProductExpiredException exception = assertThrows(ProductExpiredException.class,
                () -> store.sell(cashier, "P1", 1, customer));
        
        // Assert
        assertTrue(exception.getMessage().contains("P1"));
    }

    @Test
    void testSaleWithInsufficientQuantityThrowsException() {
        // Arrange
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 5));

        // Act
        InsufficientQuantityException exception = assertThrows(InsufficientQuantityException.class,
                () -> store.sell(cashier, "P1", 10, customer));
        
        // Assert
        assertTrue(exception.getMessage().contains("P1"));
        assertTrue(exception.getMessage().contains("10"));
        assertTrue(exception.getMessage().contains("5"));
    }

    @Test
    void testSaleWithInsufficientBudgetThrowsException() {
        // Arrange
        store.addProduct(new NonFoodProduct("P1", "Expensive Item", new BigDecimal("50.00"), 
                LocalDate.MAX, 5));
        Customer poorCustomer = new Customer("CU2", "Poor Customer", new BigDecimal("10.00"));

        // Act
        InsufficientBudgetException exception = assertThrows(InsufficientBudgetException.class,
                () -> store.sell(cashier, "P1", 1, poorCustomer));
        
        // Assert
        assertTrue(exception.getMessage().contains("Insufficient budget"));
    }

    @Test
    void testSaleWithInvalidProductIdThrowsNotFoundException() {
        // Arrange
        // No product setup needed - testing non-existent product

        // Act
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> store.sell(cashier, "INVALID_ID", 1, customer));
        
        // Assert
        assertTrue(exception.getMessage().contains("INVALID_ID"));
    }

    @Test
    void testSaleWithZeroQuantityThrowsIllegalArgumentException() {
        // Arrange
        store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 10));

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> store.sell(cashier, "P1", 0, customer));
        
        // Assert
        assertEquals("Quantity must be positive: 0", exception.getMessage());
    }

    @Test
    void testFindNonExistentProductReturnsNull() {
        // Arrange
        // No product setup needed - testing non-existent product

        // Act
        Product result = store.find("NON_EXISTENT");
        
        // Assert
        assertNull(result);
    }
}
