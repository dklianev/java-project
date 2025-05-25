package org.informatics.store;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Receipt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NearExpiryDiscountTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;

    @BeforeEach
    void setUp() {
        StoreConfig config = new StoreConfig(
                new BigDecimal("0.20"), // 20% food markup
                new BigDecimal("0.25"), // 25% non-food markup
                3, // 3 days for near expiry
                new BigDecimal("0.30") // 30% near expiry discount
        );
        store = new Store(config);
        cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
        customer = new Customer("CU1", "Test Customer", new BigDecimal("500"));
        
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
    void testFoodProductNearExpiryGetsDiscount() throws Exception {
        // Product expires in 2 days - should get 30% discount
        store.addProduct(new FoodProduct("F1", "Near Expiry Cheese", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(2), 5));

        Receipt receipt = store.sell(cashier, "F1", 1, customer);
        
        // Expected: (2.00 * 1.20) * (1 - 0.30) = 2.40 * 0.70 = 1.68
        BigDecimal expectedPrice = new BigDecimal("1.68");
        BigDecimal actualPrice = receipt.getLines().getFirst().price();
        
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }

    @Test
    void testNonFoodProductNearExpiryGetsDiscount() throws Exception {
        // Product expires in 1 day - should get 30% discount
        store.addProduct(new NonFoodProduct("N1", "Near Expiry Soap", new BigDecimal("3.00"), 
                LocalDate.now().plusDays(1), 3));

        Receipt receipt = store.sell(cashier, "N1", 1, customer);
        
        // Expected: (3.00 * 1.25) * (1 - 0.30) = 3.75 * 0.70 = 2.625
        BigDecimal expectedPrice = new BigDecimal("3.00")
            .multiply(new BigDecimal("1.25"))
            .multiply(new BigDecimal("0.70"));
        BigDecimal actualPrice = receipt.getLines().getFirst().price();
        
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }

    @Test
    void testProductJustOutsideBoundaryNoDiscount() throws Exception {
        // Product expires in 4 days - should NOT get discount
        store.addProduct(new FoodProduct("F3", "Regular Butter", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(4), 5));

        Receipt receipt = store.sell(cashier, "F3", 1, customer);
        
        // Should NOT get discount: 2.00 * 1.20 = 2.40
        BigDecimal expectedPrice = new BigDecimal("2.40");
        BigDecimal actualPrice = receipt.getLines().getFirst().price();
        
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }

    @Test
    void testProductFarFromExpiryNoDiscount() throws Exception {
        // Product expires in 10 days - should NOT get discount
        store.addProduct(new FoodProduct("F4", "Fresh Fruit", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(10), 5));

        Receipt receipt = store.sell(cashier, "F4", 1, customer);
        
        // Should NOT get discount: 2.00 * 1.20 = 2.40
        BigDecimal expectedPrice = new BigDecimal("2.40");
        BigDecimal actualPrice = receipt.getLines().getFirst().price();
        
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }
} 