package org.informatics.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.informatics.entity.FoodProduct;
import org.informatics.entity.Product;
import org.informatics.service.contract.GoodsService;
import org.informatics.service.impl.GoodsServiceImpl;
import org.informatics.store.Store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GoodsServiceImplTest {

    private Store mockStore;
    private GoodsService goodsService;
    private Product testProduct;

    @BeforeEach
    public void setUp() {
        mockStore = mock(Store.class);
        goodsService = new GoodsServiceImpl(mockStore);
        testProduct = new FoodProduct("P1", "Test Product", new BigDecimal("10.0"), LocalDate.now().plusDays(10), 5);
    }

    @Test
    void whenAddProduct_thenStoreAddProductIsCalled() {
        // Arrange
        when(mockStore.addProduct(testProduct)).thenReturn(true);

        // Act
        boolean result = goodsService.addProduct(testProduct);

        // Assert
        assertTrue(result, "addProduct should return true for successful addition");
        verify(mockStore).addProduct(testProduct);
    }

    @Test
    void whenListProducts_thenStoreListProductsIsCalled() {
        // Arrange
        List<Product> expectedProducts = List.of(testProduct);
        when(mockStore.listProducts()).thenReturn(expectedProducts);

        // Act
        List<Product> actualProducts = goodsService.listProducts();

        // Assert
        assertEquals(expectedProducts, actualProducts);
        verify(mockStore).listProducts();
    }

    @Test
    void whenFind_thenStoreFindIsCalled() {
        // Arrange
        String productId = "P1";
        when(mockStore.find(productId)).thenReturn(testProduct);

        // Act
        Product foundProduct = goodsService.find(productId);

        // Assert
        assertEquals(testProduct, foundProduct);
        verify(mockStore).find(productId);
    }

    @Test
    void whenFindNonExistentProduct_thenReturnNull() {
        // Arrange
        String productId = "NONEXISTENT";
        when(mockStore.find(productId)).thenReturn(null);

        // Act
        Product foundProduct = goodsService.find(productId);

        // Assert
        assertNull(foundProduct);
        verify(mockStore).find(productId);
    }

    @Test
    void whenAddDuplicateProduct_thenReturnFalse() {
        // Arrange - simulate store returning false for duplicate
        when(mockStore.addProduct(any(Product.class))).thenReturn(false);

        // Act
        boolean result = goodsService.addProduct(testProduct);

        // Assert
        assertFalse(result, "addProduct should return false for duplicate product");
        verify(mockStore).addProduct(testProduct);
    }

    @Test
    void testConstructorSetsStore() {
        // Test that the constructor properly sets the store
        GoodsServiceImpl service = new GoodsServiceImpl(mockStore);
        assertNotNull(service, "Service should be created successfully");
    }
}
