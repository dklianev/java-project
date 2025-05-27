package org.informatics.service;

import org.informatics.service.impl.FinancialServiceImpl;
import org.informatics.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FinancialServiceImplMockTest {

    FinancialServiceImpl financialService;
    Store mockStore;

    @BeforeEach
    void setup() {
        mockStore = Mockito.mock(Store.class);
        financialService = new FinancialServiceImpl(mockStore);
    }

    @Test
    void testTurnoverDelegatesToStoreAndReturnsCorrectValue() {
        // Arrange
        BigDecimal expectedTurnover = new BigDecimal("150.50");
        Mockito.when(mockStore.turnover()).thenReturn(expectedTurnover);
        
        // Act
        BigDecimal result = financialService.turnover();
        
        // Assert
        assertEquals(expectedTurnover, result);
        Mockito.verify(mockStore).turnover();
    }

    @Test
    void testProfitWithPositiveValueIndicatesProfitableBusiness() {
        // Arrange
        BigDecimal positiveProfit = new BigDecimal("100.50");
        Mockito.when(mockStore.profit()).thenReturn(positiveProfit);
        
        // Act
        BigDecimal result = financialService.profit();
        
        // Assert
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(positiveProfit, result);
        Mockito.verify(mockStore).profit();
    }

    @Test
    void testProfitWithNegativeValueIndicatesBusinessLoss() {
        // Arrange
        BigDecimal negativeProfit = new BigDecimal("-50.25");
        Mockito.when(mockStore.profit()).thenReturn(negativeProfit);
        
        // Act
        BigDecimal result = financialService.profit();
        
        // Assert
        assertTrue(result.compareTo(BigDecimal.ZERO) < 0);
        assertEquals(negativeProfit, result);
        Mockito.verify(mockStore).profit();
    }

    @Test
    void testGetSoldItemsDelegatesToStoreAndReturnsCorrectMap() {
        // Arrange
        Map<String, Integer> expectedSoldItems = Map.of(
            "P1", 10,
            "P2", 5,
            "P3", 15
        );
        Mockito.when(mockStore.getSoldItems()).thenReturn(expectedSoldItems);
        
        // Act
        Map<String, Integer> result = financialService.getSoldItems();
        
        // Assert
        assertEquals(expectedSoldItems, result);
        assertEquals(3, result.size());
        assertEquals(10, result.get("P1"));
        assertEquals(5, result.get("P2"));
        assertEquals(15, result.get("P3"));
        Mockito.verify(mockStore).getSoldItems();
    }
} 