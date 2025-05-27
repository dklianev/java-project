package org.informatics.service;

import org.informatics.entity.Product;
import org.informatics.service.impl.GoodsServiceImpl;
import org.informatics.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class GoodsServiceImplMockTest {

    GoodsServiceImpl goodsService;
    Store mockStore;

    @BeforeEach
    void setup() {
        mockStore = Mockito.mock(Store.class);
        goodsService = new GoodsServiceImpl(mockStore);
    }

    @Test
    void testAddProductDelegatesToStoreSuccessfully() {
        // Arrange
        Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockStore.addProduct(mockProduct)).thenReturn(true);
        
        // Act
        boolean result = goodsService.addProduct(mockProduct);
        
        // Assert
        assertTrue(result);
        Mockito.verify(mockStore).addProduct(mockProduct);
    }

    @Test
    void testRestockProductDelegatesToStoreSuccessfully() {
        // Arrange
        String productId = "P1";
        int quantity = 10;
        Mockito.when(mockStore.restockProduct(productId, quantity)).thenReturn(true);
        
        // Act
        boolean result = goodsService.restockProduct(productId, quantity);
        
        // Assert
        assertTrue(result);
        Mockito.verify(mockStore).restockProduct(productId, quantity);
    }

    @Test
    void testRestockProductWithNegativeQuantityThrowsException() {
        // Arrange
        String productId = "P1";
        int negativeQuantity = -5;
        Mockito.when(mockStore.restockProduct(productId, negativeQuantity))
               .thenThrow(new IllegalArgumentException("Quantity cannot be negative"));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                    () -> goodsService.restockProduct(productId, negativeQuantity));
        assertTrue(exception.getMessage().contains("Quantity cannot be negative"));
    }

    @Test
    void testFindProductDelegatesToStoreAndReturnsProduct() {
        // Arrange
        String productId = "P1";
        Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockStore.find(productId)).thenReturn(mockProduct);
        
        // Act
        Product result = goodsService.find(productId);
        
        // Assert
        assertEquals(mockProduct, result);
        Mockito.verify(mockStore).find(productId);
    }
} 