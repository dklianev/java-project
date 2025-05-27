package org.informatics.service;

import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Receipt;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.service.impl.StoreServiceImpl;
import org.informatics.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StoreServiceImplMockTest {

    StoreServiceImpl storeService;
    Store mockStore;

    @BeforeEach
    void setup() {
        mockStore = Mockito.mock(Store.class);
        storeService = new StoreServiceImpl(mockStore);
    }

    @Test
    void testSellProductDelegatesToStoreAndReturnsReceipt() throws Exception {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Customer mockCustomer = Mockito.mock(Customer.class);
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        String productId = "P1";
        int quantity = 2;
        Mockito.when(mockStore.sell(mockCashier, productId, quantity, mockCustomer))
               .thenReturn(mockReceipt);
        
        // Act
        Receipt result = storeService.sell(mockCashier, productId, quantity, mockCustomer);
        
        // Assert
        assertEquals(mockReceipt, result);
        Mockito.verify(mockStore).sell(mockCashier, productId, quantity, mockCustomer);
    }

    @Test
    void testSellNonExistentProductThrowsProductNotFoundException() throws Exception {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Customer mockCustomer = Mockito.mock(Customer.class);
        String productId = "NONEXISTENT";
        int quantity = 1;
        Mockito.when(mockStore.sell(mockCashier, productId, quantity, mockCustomer))
               .thenThrow(new ProductNotFoundException("Product not found"));
        
        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, 
                    () -> storeService.sell(mockCashier, productId, quantity, mockCustomer));
        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    void testSellInsufficientQuantityThrowsInsufficientQuantityException() throws Exception {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Customer mockCustomer = Mockito.mock(Customer.class);
        String productId = "P1";
        int quantity = 100;
        Mockito.when(mockStore.sell(mockCashier, productId, quantity, mockCustomer))
               .thenThrow(new InsufficientQuantityException(productId, quantity, 5));
        
        // Act & Assert
        InsufficientQuantityException exception = assertThrows(InsufficientQuantityException.class, 
                    () -> storeService.sell(mockCashier, productId, quantity, mockCustomer));
        assertTrue(exception.getMessage().contains(productId));
    }

    @Test
    void testCreateReceiptDelegatesToStoreAndReturnsReceipt() {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        Mockito.when(mockStore.createReceipt(mockCashier)).thenReturn(mockReceipt);
        
        // Act
        Receipt result = storeService.createReceipt(mockCashier);
        
        // Assert
        assertEquals(mockReceipt, result);
        Mockito.verify(mockStore).createReceipt(mockCashier);
    }

    @Test
    void testSaveReceiptCallsReceiptSaveMethod() throws IOException {
        // Arrange
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        File mockDir = Mockito.mock(File.class);
        
        // Act
        storeService.saveReceipt(mockReceipt, mockDir);
        
        // Assert
        Mockito.verify(mockReceipt).save(mockDir);
    }
} 