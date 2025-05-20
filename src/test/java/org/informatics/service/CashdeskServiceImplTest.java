package org.informatics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.store.Store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class CashdeskServiceImplTest {

    CashdeskServiceImpl cashdeskService;
    Store mockStore;

    @BeforeEach
    public void setup() {
        mockStore = Mockito.mock(Store.class);
        cashdeskService = new CashdeskServiceImpl(mockStore);
    }

    @Test
    void whenAddCashier_thenStoreAddCashierIsCalled() {
        // Arrange
        Cashier mockCashier = Mockito.mock(Cashier.class);

        // Act
        cashdeskService.addCashier(mockCashier);

        // Assert
        Mockito.verify(mockStore).addCashier(mockCashier);
    }

    @Test
    void whenListCashiers_thenReturnCashiersList() {
        // Arrange
        List<Cashier> cashiers = new ArrayList<>();
        Cashier mockCashier1 = Mockito.mock(Cashier.class);
        Cashier mockCashier2 = Mockito.mock(Cashier.class);
        cashiers.add(mockCashier1);
        cashiers.add(mockCashier2);

        when(mockStore.listCashiers()).thenReturn(cashiers);

        // Act
        List<Cashier> result = cashdeskService.listCashiers();

        // Assert
        assertEquals(2, result.size());
        assertEquals(cashiers, result);
    }

    @Test
    void whenAddCashDesk_thenStoreAddCashDeskIsCalled() {
        // Arrange
        CashDesk mockCashDesk = Mockito.mock(CashDesk.class);

        // Act
        cashdeskService.addCashDesk(mockCashDesk);

        // Assert
        Mockito.verify(mockStore).addCashDesk(mockCashDesk);
    }

    @Test
    void whenListCashDesks_thenReturnCashDesksList() {
        // Arrange
        List<CashDesk> cashDesks = new ArrayList<>();
        CashDesk mockCashDesk1 = Mockito.mock(CashDesk.class);
        CashDesk mockCashDesk2 = Mockito.mock(CashDesk.class);
        cashDesks.add(mockCashDesk1);
        cashDesks.add(mockCashDesk2);

        when(mockStore.listCashDesks()).thenReturn(cashDesks);

        // Act
        List<CashDesk> result = cashdeskService.listCashDesks();

        // Assert
        assertEquals(2, result.size());
        assertEquals(cashDesks, result);
    }

    @Test
    void whenAssignCashierToDesk_thenStoreAssignCashierToDeskIsCalled() throws Exception {
        // Arrange
        String cashierId = "C1";
        String deskId = "D1";

        // Act
        cashdeskService.assignCashierToDesk(cashierId, deskId);

        // Assert
        Mockito.verify(mockStore).assignCashierToDesk(cashierId, deskId);
    }

    @Test
    void whenAssignCashierToDesk_andExceptionOccurs_thenExceptionIsThrown() throws Exception {
        // Arrange
        String cashierId = "C1";
        String deskId = "D1";
        Exception expectedException = new Exception("Assignment error");

        doThrow(expectedException)
                .when(mockStore).assignCashierToDesk(cashierId, deskId);

        try {
            // Act
            cashdeskService.assignCashierToDesk(cashierId, deskId);
            fail("Expected Exception was not thrown");
        } catch (Exception e) {
            // Assert
            assertEquals(expectedException, e);
        }
    }

    @Test
    void whenReleaseCashierFromDesk_thenStoreReleaseCashierFromDeskIsCalled() throws Exception {
        // Arrange
        String deskId = "D1";

        // Act
        cashdeskService.releaseCashierFromDesk(deskId);

        // Assert
        Mockito.verify(mockStore).releaseCashierFromDesk(deskId);
    }

    @Test
    void whenReleaseCashierFromDesk_andExceptionOccurs_thenExceptionIsThrown() throws Exception {
        // Arrange
        String deskId = "D1";
        Exception expectedException = new Exception("Release error");

        doThrow(expectedException)
                .when(mockStore).releaseCashierFromDesk(deskId);

        try {
            // Act
            cashdeskService.releaseCashierFromDesk(deskId);
            fail("Expected Exception was not thrown");
        } catch (Exception e) {
            // Assert
            assertEquals(expectedException, e);
        }
    }

    @Test
    void whenGetAssignedDeskForCashier_thenReturnOptionalCashDesk() {
        // Arrange
        String cashierId = "C1";
        CashDesk mockCashDesk = Mockito.mock(CashDesk.class);
        Optional<CashDesk> expectedResult = Optional.of(mockCashDesk);

        when(mockStore.getAssignedDeskForCashier(cashierId)).thenReturn(expectedResult);

        // Act
        Optional<CashDesk> result = cashdeskService.getAssignedDeskForCashier(cashierId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockCashDesk, result.get());
    }

    @Test
    void whenFindCashDeskById_thenReturnOptionalCashDesk() {
        // Arrange
        String deskId = "D1";
        CashDesk mockCashDesk = Mockito.mock(CashDesk.class);
        Optional<CashDesk> expectedResult = Optional.of(mockCashDesk);

        when(mockStore.findCashDeskById(deskId)).thenReturn(expectedResult);

        // Act
        Optional<CashDesk> result = cashdeskService.findCashDeskById(deskId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockCashDesk, result.get());
    }

    @Test
    void whenFindCashierById_thenReturnOptionalCashier() {
        // Arrange
        String cashierId = "C1";
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Optional<Cashier> expectedResult = Optional.of(mockCashier);

        when(mockStore.findCashierById(cashierId)).thenReturn(expectedResult);

        // Act
        Optional<Cashier> result = cashdeskService.findCashierById(cashierId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockCashier, result.get());
    }
} 