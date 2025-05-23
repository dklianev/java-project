package org.informatics.store;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.Product;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.service.impl.GoodsServiceImpl;
import org.informatics.service.impl.StoreServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServiceLayerTest {

    private GoodsServiceImpl goodsService;
    private CashdeskServiceImpl cashdeskService;
    private StoreServiceImpl storeService;

    @BeforeEach
    public void setUp() {
        try {
            StoreConfig config = new StoreConfig(
                new BigDecimal("0.20"), 
                new BigDecimal("0.25"), 
                3, 
                new BigDecimal("0.30")
            );
            Store store = new Store(config);
            goodsService = new GoodsServiceImpl(store);
            cashdeskService = new CashdeskServiceImpl(store);
            storeService = new StoreServiceImpl(store);
        } catch (IllegalArgumentException e) {
            fail("Failed to set up store configuration: " + e.getMessage());
        }
    }

    @Test
    void testGoodsServiceOperations() {
        // Test adding products
        Product product1 = new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 5);
        Product product2 = new FoodProduct("F2", "Bread", new BigDecimal("1.5"), LocalDate.now().plusDays(3), 10);

        boolean result1 = goodsService.addProduct(product1);
        boolean result2 = goodsService.addProduct(product2);
        
        assertTrue(result1, "First product should be added successfully");
        assertTrue(result2, "Second product should be added successfully");

        // Test adding duplicate product
        boolean duplicateResult = goodsService.addProduct(product1);
        assertFalse(duplicateResult, "Duplicate product should return false");

        // Test listing products
        List<Product> products = goodsService.listProducts();
        assertEquals(2, products.size(), "Should have 2 products in store");

        // Test finding products
        Product foundProduct = goodsService.find("F1");
        assertNotNull(foundProduct, "Should find existing product");
        assertEquals("Milk", foundProduct.getName(), "Found product should have correct name");

        Product notFoundProduct = goodsService.find("NONEXISTENT");
        assertNull(notFoundProduct, "Should return null for non-existent product");
    }

    @Test
    void testCashdeskServiceOperations() {
        // Test adding cashiers
        Cashier cashier1 = new Cashier("C1", "John", new BigDecimal("1000"));
        Cashier cashier2 = new Cashier("C2", "Jane", new BigDecimal("1200"));

        cashdeskService.addCashier(cashier1);
        cashdeskService.addCashier(cashier2);

        List<Cashier> cashiers = cashdeskService.listCashiers();
        assertEquals(2, cashiers.size(), "Should have 2 cashiers");

        // Test adding cash desks
        CashDesk desk1 = new CashDesk();
        CashDesk desk2 = new CashDesk();

        cashdeskService.addCashDesk(desk1);
        cashdeskService.addCashDesk(desk2);

        List<CashDesk> desks = cashdeskService.listCashDesks();
        assertEquals(2, desks.size(), "Should have 2 cash desks");

        // Test assigning cashier to desk
        try {
            cashdeskService.assignCashierToDesk("C1", desk1.getId());
            assertTrue(desk1.isOccupied(), "Desk should be occupied after assignment");
            assertEquals("C1", desk1.getCurrentCashier().getId(), "Desk should have correct cashier");
        } catch (Exception e) {
            fail("Failed to assign cashier to desk: " + e.getMessage());
        }
    }

    @Test
    void testStoreServiceOperations() {
        // Setup
        Cashier cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
        
        cashdeskService.addCashier(cashier);
        CashDesk desk = new CashDesk();
        cashdeskService.addCashDesk(desk);
        
        try {
            cashdeskService.assignCashierToDesk(cashier.getId(), desk.getId());
        } catch (Exception e) {
            fail("Failed to set up cashier desk assignment: " + e.getMessage());
        }

        // Test creating receipt
        try {
            storeService.createReceipt(cashier);
            // Receipt creation should succeed
        } catch (Exception e) {
            fail("Failed to create receipt: " + e.getMessage());
        }
    }
}
