package org.informatics.store;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.Product;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BasicOperationsTest {

    private Store store;
    private Store mockStore;

    @BeforeEach
    void setUp() {
        StoreConfig config = new StoreConfig(
                new BigDecimal("0.20"), // 20% food markup
                new BigDecimal("0.25"), // 25% non-food markup
                3, // near expiry days
                new BigDecimal("0.30") // near expiry discount
        );
        store = new Store(config);
        mockStore = Mockito.mock(Store.class);
    }

    // === MOCK TESTS ===
    @Test
    void testAddProductWithMock() {
        // Arrange
        Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockProduct.getId()).thenReturn("MOCK1");
        Mockito.when(mockStore.addProduct(mockProduct)).thenReturn(true);
        
        // Act
        boolean result = mockStore.addProduct(mockProduct);
        
        // Assert
        assertTrue(result);
        Mockito.verify(mockStore).addProduct(mockProduct);
    }



    // === INTEGRATION TESTS ===
    @Test
    void testAddProductSuccess() {
        // Arrange
        Product product = new FoodProduct("F1", "Test Milk", new BigDecimal("2.50"), 
                LocalDate.now().plusDays(7), 10);
        
        // Act
        boolean result = store.addProduct(product);
        
        // Assert
        assertTrue(result);
        assertEquals(1, store.listProducts().size());
        assertEquals("F1", store.find("F1").getId());
    }

    @Test
    void testAddDuplicateProductFails() {
        // Arrange
        Product product1 = new FoodProduct("F1", "First Milk", new BigDecimal("2.50"), 
                LocalDate.now().plusDays(7), 10);
        Product product2 = new FoodProduct("F1", "Duplicate Milk", new BigDecimal("3.00"), 
                LocalDate.now().plusDays(5), 5);
        
        // Act
        boolean firstAdd = store.addProduct(product1);
        boolean secondAdd = store.addProduct(product2);
        
        // Assert
        assertTrue(firstAdd);
        assertFalse(secondAdd);
        assertEquals(1, store.listProducts().size());
        assertEquals("First Milk", store.find("F1").getName()); // Original product remains
    }

    @Test
    void testProductQuantityReductionAfterSale() throws Exception {
        // Arrange
        Product product = new FoodProduct("F1", "Test Bread", new BigDecimal("1.80"), 
                LocalDate.now().plusDays(5), 15);
        store.addProduct(product);
        
        Cashier cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
        Customer customer = new Customer("CU1", "Test Customer", new BigDecimal("100"));
        CashDesk desk = new CashDesk();
        
        store.addCashier(cashier);
        store.addCashDesk(desk);
        store.assignCashierToDesk(cashier.getId(), desk.getId());
        
        // Act
        store.sell(cashier, "F1", 3, customer); // Sell 3 items
        
        // Assert
        assertEquals(12, store.find("F1").getQuantity()); // 15 - 3 = 12
    }


} 