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
    private StoreConfig config;

    @BeforeEach
    void setUp() {
        config = new StoreConfig(
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
        store.addProduct(new FoodProduct("F1", "Near Expiry Milk", new BigDecimal("2.00"), 
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
    void testProductExactlyOnBoundaryGetsDiscount() throws Exception {
        // Product expires in exactly 3 days - should get discount
        store.addProduct(new FoodProduct("F2", "Boundary Product", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(3), 5));

        Receipt receipt = store.sell(cashier, "F2", 1, customer);
        
        // Should get discount: (2.00 * 1.20) * 0.70 = 1.68
        BigDecimal expectedPrice = new BigDecimal("1.68");
        BigDecimal actualPrice = receipt.getLines().getFirst().price();
        
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }

    @Test
    void testProductJustOutsideBoundaryNoDiscount() throws Exception {
        // Product expires in 4 days - should NOT get discount
        store.addProduct(new FoodProduct("F3", "No Discount Product", new BigDecimal("2.00"), 
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
        store.addProduct(new FoodProduct("F4", "Fresh Product", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(10), 5));

        Receipt receipt = store.sell(cashier, "F4", 1, customer);
        
        // Should NOT get discount: 2.00 * 1.20 = 2.40
        BigDecimal expectedPrice = new BigDecimal("2.40");
        BigDecimal actualPrice = receipt.getLines().getFirst().price();
        
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }

    @Test
    void testDifferentDiscountConfiguration() throws Exception {
        // Test with different discount settings: 7 days, 50% discount
        StoreConfig customConfig = new StoreConfig(
                new BigDecimal("0.20"), 
                new BigDecimal("0.25"), 
                7, // 7 days for near expiry
                new BigDecimal("0.50") // 50% discount
        );
        Store customStore = new Store(customConfig);
        customStore.addCashier(cashier);
        CashDesk desk = new CashDesk();
        customStore.addCashDesk(desk);
        customStore.assignCashierToDesk(cashier.getId(), desk.getId());

        // Product expires in 5 days - should get 50% discount with 7-day config
        customStore.addProduct(new FoodProduct("F5", "Custom Discount Product", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(5), 5));

        Receipt receipt = customStore.sell(cashier, "F5", 1, customer);
        
        // Expected: (2.00 * 1.20) * (1 - 0.50) = 2.40 * 0.50 = 1.20
        BigDecimal expectedPrice = new BigDecimal("1.20");
        BigDecimal actualPrice = receipt.getLines().getFirst().price();
        
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }

    @Test
    void testZeroDiscountConfiguration() throws Exception {
        // Test with 0% discount
        StoreConfig noDiscountConfig = new StoreConfig(
                new BigDecimal("0.20"), 
                new BigDecimal("0.25"), 
                3, 
                BigDecimal.ZERO // 0% discount
        );
        Store noDiscountStore = new Store(noDiscountConfig);
        noDiscountStore.addCashier(cashier);
        CashDesk desk = new CashDesk();
        noDiscountStore.addCashDesk(desk);
        noDiscountStore.assignCashierToDesk(cashier.getId(), desk.getId());

        // Product expires in 2 days but discount is 0%
        noDiscountStore.addProduct(new FoodProduct("F6", "No Discount Product", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(2), 5));

        Receipt receipt = noDiscountStore.sell(cashier, "F6", 1, customer);
        
        // Should only have markup, no discount: 2.00 * 1.20 = 2.40
        BigDecimal expectedPrice = new BigDecimal("2.40");
        BigDecimal actualPrice = receipt.getLines().getFirst().price();
        
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }

    @Test
    void testMultipleNearExpiryProductsInSameReceipt() throws Exception {
        store.addProduct(new FoodProduct("F7", "Near Expiry Milk", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(2), 5));
        store.addProduct(new NonFoodProduct("N2", "Near Expiry Soap", new BigDecimal("4.00"), 
                LocalDate.now().plusDays(1), 3));

        Receipt receipt = store.createReceipt(cashier);
        store.addToReceipt(receipt, "F7", 1, customer);
        store.addToReceipt(receipt, "N2", 1, customer);
        
        assertEquals(2, receipt.getLines().size());
        
        // Milk: (2.00 * 1.20) * 0.70 = 1.68
        BigDecimal expectedMilkPrice = new BigDecimal("1.68");
        assertEquals(0, expectedMilkPrice.compareTo(receipt.getLines().get(0).price()));
        
        // Soap: (4.00 * 1.25) * 0.70 = 3.50
        BigDecimal expectedSoapPrice = new BigDecimal("3.50");
        assertEquals(0, expectedSoapPrice.compareTo(receipt.getLines().get(1).price()));
    }

    @Test
    void testNearExpiryProductDirectPriceCalculation() {
        // Test the Product.salePrice() method directly
        FoodProduct nearExpiryProduct = new FoodProduct("F8", "Direct Test", new BigDecimal("2.00"), 
                LocalDate.now().plusDays(2), 5);

        BigDecimal salePrice = nearExpiryProduct.salePrice(config, LocalDate.now());
        
        // Expected: (2.00 * 1.20) * (1 - 0.30) = 1.68
        BigDecimal expectedPrice = new BigDecimal("1.68");
        assertEquals(0, expectedPrice.compareTo(salePrice));
    }
} 