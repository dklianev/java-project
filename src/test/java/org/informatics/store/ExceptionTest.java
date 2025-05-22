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
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.DuplicateProductException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidConfigurationException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.NegativePriceException;
import org.informatics.exception.NonPositiveQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.exception.ProductNullException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        } catch (InvalidConfigurationException e) {
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
        } catch (ProductExpiredException | InvalidQuantityException | InsufficientQuantityException
                | InsufficientBudgetException | IOException | CashDeskNotAssignedException
                | ProductNullException | NonPositiveQuantityException | NegativePriceException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowInvalidQuantityExceptionForZeroQuantity() {
        try {
            store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 10));
        } catch (DuplicateProductException ex) {
            fail("Unexpected exception when adding product: " + ex.getMessage());
        }
        
        try {
            store.sell(cashier, "P1", 0, customer);
            fail("Expected InvalidQuantityException was not thrown for zero quantity");
        } catch (InvalidQuantityException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | ProductExpiredException | InsufficientQuantityException
                | InsufficientBudgetException | IOException | CashDeskNotAssignedException
                | ProductNullException | NonPositiveQuantityException | NegativePriceException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowInvalidQuantityExceptionForNegativeQuantity() {
        try {
            store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 10));
        } catch (DuplicateProductException ex) {
            fail("Unexpected exception when adding product: " + ex.getMessage());
        }
        
        try {
            store.sell(cashier, "P1", -1, customer);
            fail("Expected InvalidQuantityException was not thrown for negative quantity");
        } catch (InvalidQuantityException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | ProductExpiredException | InsufficientQuantityException
                | InsufficientBudgetException | IOException | CashDeskNotAssignedException
                | ProductNullException | NonPositiveQuantityException | NegativePriceException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowInsufficientQuantityException() {
        try {
            store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 5));
        } catch (DuplicateProductException ex) {
            fail("Unexpected exception when adding product: " + ex.getMessage());
        }
        
        try {
            store.sell(cashier, "P1", 10, customer);
            fail("Expected InsufficientQuantityException was not thrown");
        } catch (InsufficientQuantityException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | ProductExpiredException | InvalidQuantityException
                | InsufficientBudgetException | IOException | CashDeskNotAssignedException
                | ProductNullException | NonPositiveQuantityException | NegativePriceException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowProductExpiredException() {
        try {
            store.addProduct(new FoodProduct("P1", "Expired Milk", new BigDecimal("2.0"), LocalDate.now().minusDays(1), 5));
        } catch (DuplicateProductException ex) {
            fail("Unexpected exception when adding product: " + ex.getMessage());
        }
        
        try {
            store.sell(cashier, "P1", 1, customer);
            fail("Expected ProductExpiredException was not thrown");
        } catch (ProductExpiredException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | InvalidQuantityException | InsufficientQuantityException
                | InsufficientBudgetException | IOException | CashDeskNotAssignedException
                | ProductNullException | NonPositiveQuantityException | NegativePriceException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowInsufficientBudgetException() {
        try {
            store.addProduct(new NonFoodProduct("P1", "Expensive Item", new BigDecimal("50.0"), LocalDate.MAX, 5));
        } catch (DuplicateProductException ex) {
            fail("Unexpected exception when adding product: " + ex.getMessage());
        }
        
        try {
            Customer poorCustomer = new Customer("CU2", "Poor Customer", new BigDecimal("10.0"));
            store.sell(cashier, "P1", 1, poorCustomer);
            fail("Expected InsufficientBudgetException was not thrown");
        } catch (InsufficientBudgetException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | ProductExpiredException | InvalidQuantityException
                | InsufficientQuantityException | IOException | CashDeskNotAssignedException
                | ProductNullException | NonPositiveQuantityException | NegativePriceException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    void testShouldThrowDuplicateProductException() {
        try {
            store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 5));
            store.addProduct(new FoodProduct("P1", "Another Product", new BigDecimal("3.0"), LocalDate.now().plusDays(10), 3));
            fail("Expected DuplicateProductException was not thrown");
        } catch (DuplicateProductException ex) {
            // Test passed - expected exception
        }
    }

    @Test
    void testShouldThrowCashDeskNotAssignedException() {
        Cashier unassignedCashier = new Cashier("C2", "Unassigned Cashier", new BigDecimal("1000"));
        store.addCashier(unassignedCashier);
        
        try {
            store.addProduct(new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 10));
        } catch (DuplicateProductException ex) {
            fail("Unexpected exception when adding product: " + ex.getMessage());
        }
        
        try {
            store.sell(unassignedCashier, "P1", 1, customer);
            fail("Expected CashDeskNotAssignedException was not thrown");
        } catch (CashDeskNotAssignedException ex) {
            // Test passed - expected exception
        } catch (ProductNotFoundException | ProductExpiredException | InvalidQuantityException
                | InsufficientQuantityException | InsufficientBudgetException | IOException
                | ProductNullException | NonPositiveQuantityException | NegativePriceException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }
    
    @Test
    void testShouldThrowProductNullException() {
        Receipt receipt = new Receipt(cashier);
        ProductNullException exception = assertThrows(ProductNullException.class, 
            () -> receipt.add(null, 1, BigDecimal.ONE));
        assertEquals("Product cannot be null", exception.getMessage());
    }
    
    @Test
    void testShouldThrowNonPositiveQuantityException() {
        Receipt receipt = new Receipt(cashier);
        Product product = new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 5);
        NonPositiveQuantityException exception = assertThrows(NonPositiveQuantityException.class, 
            () -> receipt.add(product, 0, BigDecimal.ONE));
        assertEquals("Quantity must be positive", exception.getMessage());
    }
    
    @Test
    void testShouldThrowNegativePriceException() {
        Receipt receipt = new Receipt(cashier);
        Product product = new FoodProduct("P1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 5);
        NegativePriceException exception = assertThrows(NegativePriceException.class, 
            () -> receipt.add(product, 1, new BigDecimal("-1.0")));
        assertEquals("Price cannot be negative", exception.getMessage());
    }
    
    @Test
    void testShouldThrowInvalidConfigurationExceptionForNegativeMarkup() {
        InvalidConfigurationException exception = assertThrows(InvalidConfigurationException.class, 
            () -> new StoreConfig(new BigDecimal("-0.1"), new BigDecimal("0.25"), 3, new BigDecimal("0.30")));
        assertEquals("Markups and discount percentage must be non-negative, discount <= 1.", exception.getMessage());
    }
    
    @Test
    void testShouldThrowInvalidConfigurationExceptionForNegativeDays() {
        InvalidConfigurationException exception = assertThrows(InvalidConfigurationException.class, 
            () -> new StoreConfig(new BigDecimal("0.2"), new BigDecimal("0.25"), -1, new BigDecimal("0.30")));
        assertEquals("Days for near expiry discount cannot be negative.", exception.getMessage());
    }
}
