package org.informatics.service;

import java.io.File;
import java.io.IOException;

import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Receipt;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.service.contract.StoreService;
import org.informatics.service.impl.StoreServiceImpl;
import org.informatics.store.Store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoreServiceImplTest {

    private Store mockStore;
    private StoreService storeService;
    private Cashier mockCashier;
    private Customer mockCustomer;
    private Receipt mockReceipt;

    @BeforeEach
    public void setUp() {
        mockStore = mock(Store.class);
        storeService = new StoreServiceImpl(mockStore);
        mockCashier = mock(Cashier.class);
        mockCustomer = mock(Customer.class);
        mockReceipt = mock(Receipt.class);
    }

    @Test
    void whenSell_thenReturnReceipt() throws ProductNotFoundException, ProductExpiredException,
            InsufficientQuantityException, InsufficientBudgetException {
        // Arrange
        String productId = "P1";
        int quantity = 2;
        when(mockStore.sell(mockCashier, productId, quantity, mockCustomer)).thenReturn(mockReceipt);

        // Act
        Receipt result = storeService.sell(mockCashier, productId, quantity, mockCustomer);

        // Assert
        assertNotNull(result);
        assertEquals(mockReceipt, result);
        verify(mockStore).sell(mockCashier, productId, quantity, mockCustomer);
    }

    @Test
    void whenCreateReceipt_thenReturnReceipt() {
        // Arrange
        when(mockStore.createReceipt(mockCashier)).thenReturn(mockReceipt);

        // Act
        Receipt result = storeService.createReceipt(mockCashier);

        // Assert
        assertNotNull(result);
        assertEquals(mockReceipt, result);
        verify(mockStore).createReceipt(mockCashier);
    }

    @Test
    void whenSell_andStoreThrowsException_thenExceptionIsThrown() throws ProductNotFoundException,
            ProductExpiredException, InsufficientQuantityException, InsufficientBudgetException {
        // Arrange
        String productId = "P1";
        int quantity = 2;
        ProductNotFoundException expectedException = new ProductNotFoundException("P1");

        doThrow(expectedException)
                .when(mockStore).sell(mockCashier, productId, quantity, mockCustomer);

        try {
            // Act
            storeService.sell(mockCashier, productId, quantity, mockCustomer);
            fail("Expected ProductNotFoundException was not thrown");
        } catch (ProductNotFoundException e) {
            // Assert
            assertEquals(expectedException, e);
        }
    }

    @Test
    void whenSaveReceipt_thenReceiptSaveIsCalled() throws IOException {
        // Arrange
        File mockDir = mock(File.class);

        // Act
        storeService.saveReceipt(mockReceipt, mockDir);

        // Assert
        verify(mockReceipt).save(mockDir);
    }

    @Test
    void whenSaveReceipt_andReceiptSaveThrowsException_thenExceptionIsThrown() throws IOException {
        // Arrange
        File mockDir = mock(File.class);
        IOException expectedException = new IOException("Save failed");

        doThrow(expectedException).when(mockReceipt).save(mockDir);

        try {
            // Act
            storeService.saveReceipt(mockReceipt, mockDir);
            fail("Expected IOException was not thrown");
        } catch (IOException e) {
            // Assert
            assertEquals(expectedException, e);
        }
    }

    @Test
    void whenAddToReceipt_thenReturnUpdatedReceipt() throws ProductNotFoundException, ProductExpiredException,
            InsufficientQuantityException, InsufficientBudgetException {
        // Arrange
        String productId = "P1";
        int quantity = 1;
        Receipt updatedReceipt = mock(Receipt.class);
        when(mockStore.addToReceipt(mockReceipt, productId, quantity, mockCustomer)).thenReturn(updatedReceipt);

        // Act
        Receipt result = storeService.addToReceipt(mockReceipt, productId, quantity, mockCustomer);

        // Assert
        assertNotNull(result);
        assertEquals(updatedReceipt, result);
        verify(mockStore).addToReceipt(mockReceipt, productId, quantity, mockCustomer);
    }
}
