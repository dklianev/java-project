package org.informatics.service;

import java.util.ArrayList;
import java.util.List;

import org.informatics.entity.Product;
import org.informatics.exception.DuplicateProductException;
import org.informatics.service.impl.GoodsServiceImpl;
import org.informatics.store.Store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class GoodsServiceImplTest {

    GoodsServiceImpl goodsService;
    Store mockStore;

    @BeforeEach
    public void setup() {
        mockStore = Mockito.mock(Store.class);
        goodsService = new GoodsServiceImpl(mockStore);
    }

    @Test
    void whenListProducts_thenReturnProductsList() {
        // Arrange
        List<Product> products = new ArrayList<>();
        Product mockProduct1 = Mockito.mock(Product.class);
        Product mockProduct2 = Mockito.mock(Product.class);
        products.add(mockProduct1);
        products.add(mockProduct2);
        
        when(mockStore.listProducts()).thenReturn(products);
        
        // Act
        List<Product> result = goodsService.listProducts();
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(products, result);
    }
    
    @Test
    void whenFindExistingProduct_thenReturnProduct() {
        // Arrange
        String productId = "P1";
        Product mockProduct = Mockito.mock(Product.class);
        
        when(mockStore.find(productId)).thenReturn(mockProduct);
        
        // Act
        Product result = goodsService.find(productId);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockProduct, result);
    }
    
    @Test
    void whenFindNonExistingProduct_thenReturnNull() {
        // Arrange
        String productId = "NONEXISTENT";
        
        when(mockStore.find(productId)).thenReturn(null);
        
        // Act
        Product result = goodsService.find(productId);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void whenAddProduct_thenStoreAddProductIsCalled() throws DuplicateProductException {
        // Arrange
        Product mockProduct = Mockito.mock(Product.class);
        
        // Act
        goodsService.addProduct(mockProduct);
        
        // Assert
        Mockito.verify(mockStore).addProduct(mockProduct);
    }
    
    @Test
    void whenAddDuplicateProduct_thenDuplicateProductExceptionIsThrown() throws DuplicateProductException {
        // Arrange
        Product mockProduct = Mockito.mock(Product.class);
        DuplicateProductException expectedException = new DuplicateProductException("P1");
        
        doThrow(expectedException)
               .when(mockStore).addProduct(mockProduct);
        
        try {
            // Act
            goodsService.addProduct(mockProduct);
            fail("Expected DuplicateProductException was not thrown");
        } catch (DuplicateProductException e) {
            // Assert
            assertNotNull(e);
        }
    }
} 