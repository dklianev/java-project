package org.informatics.service;

import java.io.File;
import java.io.IOException;

import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Receipt;
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.service.impl.StoreServiceImpl;
import org.informatics.store.Store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StoreServiceImplTest {

    StoreServiceImpl storeService;
    Store mockStore;
    
    @TempDir
    File tempDir;

    @BeforeEach
    public void setup() {
        mockStore = Mockito.mock(Store.class);
        storeService = new StoreServiceImpl(mockStore);
    }

    @Test
    void whenSell_thenReturnReceipt() throws ProductNotFoundException, ProductExpiredException,
            InvalidQuantityException, InsufficientQuantityException, InsufficientBudgetException,
            IOException, CashDeskNotAssignedException {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);
        String productId = "P1";
        int quantity = 2;
        Customer mockCustomer = Mockito.mock(Customer.class);
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        
        when(mockStore.sell(mockCashier, productId, quantity, mockCustomer))
                .thenReturn(mockReceipt);
        
        // Act
        Receipt result = storeService.sell(mockCashier, productId, quantity, mockCustomer, tempDir);
        
        // Assert
        assertEquals(mockReceipt, result);
        verify(mockReceipt).save(tempDir);
    }
    
    @Test
    void whenSell_andStoreThrowsException_thenExceptionIsThrown() throws ProductNotFoundException,
            ProductExpiredException, InvalidQuantityException, InsufficientQuantityException,
            InsufficientBudgetException, IOException, CashDeskNotAssignedException {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);
        String productId = "P1";
        int quantity = 2;
        Customer mockCustomer = Mockito.mock(Customer.class);
        ProductNotFoundException expectedException = new ProductNotFoundException("P1");
        
        when(mockStore.sell(mockCashier, productId, quantity, mockCustomer))
                .thenThrow(expectedException);
        
        try {
            // Act
            storeService.sell(mockCashier, productId, quantity, mockCustomer, tempDir);
            fail("Expected ProductNotFoundException was not thrown");
        } catch (ProductNotFoundException e) {
            // Assert
            assertNotNull(e);
        }
    }
    
    @Test
    void whenSell_andReceiptSaveThrowsException_thenExceptionIsThrown() throws ProductNotFoundException,
            ProductExpiredException, InvalidQuantityException, InsufficientQuantityException,
            InsufficientBudgetException, IOException, CashDeskNotAssignedException {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);
        String productId = "P1";
        int quantity = 2;
        Customer mockCustomer = Mockito.mock(Customer.class);
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        IOException expectedException = new IOException("Save failed");
        
        when(mockStore.sell(mockCashier, productId, quantity, mockCustomer))
                .thenReturn(mockReceipt);
        doThrow(expectedException).when(mockReceipt).save(any(File.class));
        
        try {
            // Act
            storeService.sell(mockCashier, productId, quantity, mockCustomer, tempDir);
            fail("Expected IOException was not thrown");
        } catch (IOException e) {
            // Assert
            assertNotNull(e);
        }
    }
    
    @Test
    void whenTurnover_thenReturnStoreTurnover() {
        // Arrange
        double expectedTurnover = 1234.56;
        when(mockStore.turnover()).thenReturn(expectedTurnover);
        
        // Act
        double result = storeService.turnover();
        
        // Assert
        assertEquals(expectedTurnover, result);
    }
} 