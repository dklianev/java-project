package org.informatics.store;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class CashDeskManagementTest {

    private Store store;
    private Store mockStore;
    private CashDesk mockDesk;
    private Cashier mockCashier;

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
        mockDesk = Mockito.mock(CashDesk.class);
        mockCashier = Mockito.mock(Cashier.class);
    }

    // === MOCK TESTS ===
    @Test
    void testFindCashierByIdWithMock() {
        // Arrange
        Mockito.when(mockCashier.getId()).thenReturn("CASHIER1");
        Mockito.when(mockCashier.getName()).thenReturn("Mock Cashier");
        Mockito.when(mockStore.findCashierById("CASHIER1")).thenReturn(Optional.of(mockCashier));
        Mockito.when(mockStore.findCashierById("NOT_FOUND")).thenReturn(Optional.empty());
        
        // Act
        Optional<Cashier> found = mockStore.findCashierById("CASHIER1");
        Optional<Cashier> notFound = mockStore.findCashierById("NOT_FOUND");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("CASHIER1", found.get().getId());
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindCashDeskByIdWithMock() {
        // Arrange
        Mockito.when(mockDesk.getId()).thenReturn("DESK1");
        Mockito.when(mockDesk.isOccupied()).thenReturn(false);
        Mockito.when(mockStore.findCashDeskById("DESK1")).thenReturn(Optional.of(mockDesk));
        Mockito.when(mockStore.findCashDeskById("NOT_FOUND")).thenReturn(Optional.empty());
        
        // Act
        Optional<CashDesk> found = mockStore.findCashDeskById("DESK1");
        Optional<CashDesk> notFound = mockStore.findCashDeskById("NOT_FOUND");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("DESK1", found.get().getId());
        assertFalse(notFound.isPresent());
    }



    // === INTEGRATION TESTS ===
    @Test
    void testAddCashierSuccess() {
        // Arrange
        Cashier cashier = new Cashier("C1", "John Doe", new BigDecimal("1200"));
        
        // Act
        store.addCashier(cashier);
        
        // Assert
        List<Cashier> cashiers = store.listCashiers();
        assertEquals(1, cashiers.size());
        assertEquals("John Doe", cashiers.getFirst().getName());
        assertTrue(store.findCashierById("C1").isPresent());
    }



    @Test
    void testAddCashDeskSuccess() {
        // Arrange
        CashDesk desk = new CashDesk();
        
        // Act
        store.addCashDesk(desk);
        
        // Assert
        List<CashDesk> desks = store.listCashDesks();
        assertEquals(1, desks.size());
        assertFalse(desks.getFirst().isOccupied());
        assertFalse(desks.getFirst().isOpen());
        assertTrue(store.findCashDeskById(desk.getId()).isPresent());
    }

    @Test
    void testAssignCashierToDeskSuccess() throws Exception {
        // Arrange
        Cashier cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
        CashDesk desk = new CashDesk();
        store.addCashier(cashier);
        store.addCashDesk(desk);
        
        // Act
        store.assignCashierToDesk("C1", desk.getId());
        
        // Assert
        assertTrue(desk.isOccupied());
        assertTrue(desk.isOpen());
        assertEquals("C1", desk.getCurrentCashier().getId());
        assertTrue(store.getAssignedDeskForCashier("C1").isPresent());
        assertEquals(desk.getId(), store.getAssignedDeskForCashier("C1").get().getId());
    }

    @Test
    void testAssignCashierToOccupiedDeskThrowsException() throws Exception {
        // Arrange
        Cashier cashier1 = new Cashier("C1", "First Cashier", new BigDecimal("1000"));
        Cashier cashier2 = new Cashier("C2", "Second Cashier", new BigDecimal("1100"));
        CashDesk desk = new CashDesk();
        
        store.addCashier(cashier1);
        store.addCashier(cashier2);
        store.addCashDesk(desk);
        
        // First assignment succeeds
        store.assignCashierToDesk("C1", desk.getId());
        
        // Act & Assert - Second assignment should fail
        Exception exception = assertThrows(Exception.class, 
                () -> store.assignCashierToDesk("C2", desk.getId()));
        
        assertTrue(exception.getMessage().contains("already occupied"));
        assertTrue(exception.getMessage().contains("C1"));
    }

    @Test
    void testReleaseCashierFromDesk() throws Exception {
        // Arrange
        Cashier cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
        CashDesk desk = new CashDesk();
        store.addCashier(cashier);
        store.addCashDesk(desk);
        store.assignCashierToDesk("C1", desk.getId());
        
        // Check initial assignment
        assertTrue(desk.isOccupied());
        
        // Act
        store.releaseCashierFromDesk(desk.getId());
        
        // Assert
        assertFalse(desk.isOccupied());
        assertFalse(desk.isOpen());
        assertNull(desk.getCurrentCashier());
        assertFalse(store.getAssignedDeskForCashier("C1").isPresent());
    }

    @Test
    void testCashierReassignmentBetweenDesks() throws Exception {
        // Arrange
        Cashier cashier = new Cashier("C1", "Mobile Cashier", new BigDecimal("1200"));
        CashDesk desk1 = new CashDesk();
        CashDesk desk2 = new CashDesk();
        
        store.addCashier(cashier);
        store.addCashDesk(desk1);
        store.addCashDesk(desk2);
        
        // Initial assignment to desk1
        store.assignCashierToDesk("C1", desk1.getId());
        assertTrue(desk1.isOccupied());
        assertFalse(desk2.isOccupied());
        
        // Act - Release from first desk then assign to second desk
        store.releaseCashierFromDesk(desk1.getId());
        store.assignCashierToDesk("C1", desk2.getId());
        
        // Assert
        assertFalse(desk1.isOccupied()); // Released from desk1
        assertTrue(desk2.isOccupied());  // Assigned to desk2
        assertEquals("C1", desk2.getCurrentCashier().getId());
        assertEquals(desk2.getId(), store.getAssignedDeskForCashier("C1").orElseThrow().getId());
    }


} 