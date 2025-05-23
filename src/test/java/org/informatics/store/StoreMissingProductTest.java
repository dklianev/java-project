package org.informatics.store;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StoreMissingProductTest {

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
    void testShouldThrowProductNotFoundExceptionForInvalidId() {
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
    void testShouldThrowProductNotFoundExceptionForMultipleProducts() {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 10));

        String[] invalidIds = {"INVALID_1", "INVALID_2", "INVALID_3"};

        for (String productId : invalidIds) {
            try {
                store.sell(cashier, productId, 1, customer);
                fail("Expected ProductNotFoundException was not thrown for product " + productId);
            } catch (ProductNotFoundException ex) {
                // Test passed - expected exception for this product
            } catch (ProductExpiredException | InsufficientQuantityException
                    | InsufficientBudgetException | IOException ex) {
                fail("Unexpected exception for product " + productId + ": " + ex.getMessage());
            }
        }
    }
}
