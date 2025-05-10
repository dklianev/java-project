package org.informatics.store;

import java.io.IOException;

import org.informatics.config.StoreConfig;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class StoreMissingProductTest {
    
    @Test
    void productNotFound() {
        Store store = new Store(new StoreConfig(0.2, 0.25, 3, 0.3));
        Cashier cashier = new Cashier("C", "Bob", 1000);
        store.addCashier(cashier);
        Customer cust = new Customer("CU", "Ann", 50);
        
        try {
            store.sell(cashier, "XYZ", 1, cust);
            fail("Expected ProductNotFoundException was not thrown");
        } catch (ProductNotFoundException ex) {
            // Test passed - expected exception
        } catch (IOException | InsufficientBudgetException | InsufficientQuantityException | InvalidQuantityException | ProductExpiredException | CashDeskNotAssignedException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }
}
