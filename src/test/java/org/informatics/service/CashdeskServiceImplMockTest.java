package org.informatics.service;

import org.informatics.entity.Cashier;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CashdeskServiceImplMockTest {

    CashdeskServiceImpl cashdeskService;
    Store mockStore;

    @BeforeEach
    void setup() {
        mockStore = Mockito.mock(Store.class);
        cashdeskService = new CashdeskServiceImpl(mockStore);
    }

    @Test
    void testAddCashierDelegatesToStore() {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);
        
        // Act
        cashdeskService.addCashier(mockCashier);
        
        // Assert
        Mockito.verify(mockStore).addCashier(mockCashier);
    }

    @Test
    void testAssignCashierToDeskDelegatesToStore() throws Exception {
        // Arrange
        String cashierId = "C1";
        String deskId = "D1";
        
        // Act
        cashdeskService.assignCashierToDesk(cashierId, deskId);
        
        // Assert
        Mockito.verify(mockStore).assignCashierToDesk(cashierId, deskId);
    }

    @Test
    void testAssignCashierToDeskWithInvalidIdThrowsException() throws Exception {
        // Arrange
        String cashierId = "INVALID";
        String deskId = "D1";
        Mockito.doThrow(new Exception("Cashier not found"))
               .when(mockStore).assignCashierToDesk(cashierId, deskId);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, 
                    () -> cashdeskService.assignCashierToDesk(cashierId, deskId));
        assertTrue(exception.getMessage().contains("Cashier not found"));
    }

    @Test
    void testFindCashierByIdDelegatesToStoreAndReturnsCashier() {
        // Arrange
        String cashierId = "C1";
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Optional<Cashier> expectedCashier = Optional.of(mockCashier);
        Mockito.when(mockStore.findCashierById(cashierId)).thenReturn(expectedCashier);
        
        // Act
        Optional<Cashier> result = cashdeskService.findCashierById(cashierId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockCashier, result.get());
        Mockito.verify(mockStore).findCashierById(cashierId);
    }
} 