package org.informatics.store;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Product;
import org.informatics.exception.DuplicateProductException;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.service.impl.GoodsServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServiceLayerTest {

    private Store store;
    private GoodsServiceImpl goodsService;
    private CashdeskServiceImpl cashDeskService;
    private StoreConfig config;

    @BeforeEach
    public void setUp() {
        config = new StoreConfig(
            new BigDecimal("0.20"), 
            new BigDecimal("0.25"), 
            3, 
            new BigDecimal("0.30")
        );
        store = new Store(config);
        goodsService = new GoodsServiceImpl(store);
        cashDeskService = new CashdeskServiceImpl(store);
    }

    @Test
    void testGoodsServiceAddAndList() {
        try {
            // Create a product and add it using the goods service
            Product notebook = new NonFoodProduct("N1", "Notebook", new BigDecimal("1"), LocalDate.MAX, 5);
            goodsService.addProduct(notebook);

            // Verify the product was added
            assertEquals(1, goodsService.listProducts().size(), "GoodsService should list one product");

            // Add another product of different type
            Product milk = new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(5), 10);
            goodsService.addProduct(milk);

            // Verify both products are listed
            assertEquals(2, goodsService.listProducts().size(), "GoodsService should list both products");

            // Find specific product by ID
            Product foundProduct = goodsService.find("N1");
            assertNotNull(foundProduct, "Should find product by ID");
            assertEquals("Notebook", foundProduct.getName(), "Found product should have correct name");

        } catch (DuplicateProductException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testCashdeskServiceAddAndList() {
        // Add a cashier using cashdesk service
        Cashier bob = new Cashier("C1", "Bob", new BigDecimal("1000"));
        cashDeskService.addCashier(bob);

        // Verify cashier was added
        assertEquals(1, cashDeskService.listCashiers().size(), "CashDeskService should list one cashier");

        // Add another cashier
        Cashier alice = new Cashier("C2", "Alice", new BigDecimal("1200"));
        cashDeskService.addCashier(alice);

        // Verify both cashiers are listed
        assertEquals(2, cashDeskService.listCashiers().size(), "CashDeskService should list both cashiers");

        // Add cash desk
        CashDesk desk = new CashDesk();
        cashDeskService.addCashDesk(desk);

        // Verify cashdesk was added
        assertEquals(1, cashDeskService.listCashDesks().size(), "CashDeskService should list one cash desk");

        // Find cashier by ID
        assertEquals(bob, cashDeskService.findCashierById("C1").orElse(null), "Should find cashier by ID");
    }

    @Test
    void testCashdeskServiceAssignAndRelease() {
        try {
            // Add cashier and desk
            Cashier bob = new Cashier("C1", "Bob", new BigDecimal("1000"));
            cashDeskService.addCashier(bob);

            CashDesk desk = new CashDesk();
            cashDeskService.addCashDesk(desk);

            // Assign cashier to desk
            cashDeskService.assignCashierToDesk(bob.getId(), desk.getId());

            // Verify assignment
            assertEquals(bob, cashDeskService.getAssignedDeskForCashier(bob.getId())
                    .orElseThrow().getCurrentCashier(), "Cashier should be assigned to desk");

            // Release cashier from desk
            cashDeskService.releaseCashierFromDesk(desk.getId());

            // Verify release
            assertEquals(false, desk.isOccupied(), "Desk should not be occupied after release");

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
