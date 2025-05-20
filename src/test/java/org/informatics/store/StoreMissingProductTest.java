package org.informatics.store;

import java.io.IOException;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StoreMissingProductTest {
    
    private Store store;
    private Cashier cashier;
    private Customer customer;
    private CashDesk cashDesk;
    
    @BeforeEach
    public void setUp() {
        // Common configuration values
        StoreConfig config = new StoreConfig(0.2, 0.25, 3, 0.3);
        store = new Store(config);
        
        // Setup cashier and customer
        cashier = new Cashier("C1", "Bob", 1000);
        customer = new Customer("CU1", "Ann", 50);
        store.addCashier(cashier);
        
        // Setup cash desk and assign cashier
        cashDesk = new CashDesk();
        store.addCashDesk(cashDesk);
        try {
            store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
        } catch (Exception e) {
            fail("Failed to set up test environment: " + e.getMessage());
        }
    }
    
    @Test
    void testProductNotFound() {
        try {
            // Attempt to sell a product that doesn't exist in the store
            store.sell(cashier, "XYZ", 1, customer);
            fail("Expected ProductNotFoundException was not thrown");
        } catch (ProductNotFoundException ex) {
            // Test passed - expected exception
        } catch (IOException | InsufficientBudgetException | InsufficientQuantityException | 
                InvalidQuantityException | ProductExpiredException | CashDeskNotAssignedException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }
    
    @Test
    void testMultipleProductsNotFound() {
        // Test multiple non-existent products
        String[] nonExistentProductIds = {"XYZ", "ABC", "DEF"};
        
        for (String productId : nonExistentProductIds) {
            try {
                store.sell(cashier, productId, 1, customer);
                fail("Expected ProductNotFoundException was not thrown for product " + productId);
            } catch (ProductNotFoundException ex) {
                // Test passed - expected exception
            } catch (IOException | InsufficientBudgetException | InsufficientQuantityException | 
                    InvalidQuantityException | ProductExpiredException | CashDeskNotAssignedException ex) {
                fail("Unexpected exception: " + ex.getMessage());
            }
        }
    }
}
