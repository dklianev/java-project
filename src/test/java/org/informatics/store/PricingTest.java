package org.informatics.store;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.Product;
import org.informatics.util.GoodsType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PricingTest {

    private StoreConfig mockConfig;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        mockConfig = Mockito.mock(StoreConfig.class);
        testDate = LocalDate.now();
    }

    @Test
    void testFoodMarkup20Percent() {
        // Arrange
        Mockito.when(mockConfig.groceriesMarkup()).thenReturn(new BigDecimal("0.20"));
        Mockito.when(mockConfig.nonFoodsMarkup()).thenReturn(new BigDecimal("0.25"));
        Mockito.when(mockConfig.daysForNearExpiryDiscount()).thenReturn(3);
        Mockito.when(mockConfig.discountPercentage()).thenReturn(new BigDecimal("0.30"));
        
        // Create real product to test pricing logic
        Product foodProduct = new Product("F1", "Test Food", new BigDecimal("10.00"), 
                GoodsType.GROCERIES, testDate.plusDays(10), 5);
        
        // Act
        BigDecimal salePrice = foodProduct.salePrice(mockConfig, testDate);
        
        // Assert - 10.00 * 1.20 = 12.00 (no near-expiry discount)
        assertEquals(0, new BigDecimal("12.00").compareTo(salePrice));
    }

    @Test
    void testNonFoodMarkup25Percent() {
        // Arrange
        Mockito.when(mockConfig.groceriesMarkup()).thenReturn(new BigDecimal("0.20"));
        Mockito.when(mockConfig.nonFoodsMarkup()).thenReturn(new BigDecimal("0.25"));
        Mockito.when(mockConfig.daysForNearExpiryDiscount()).thenReturn(3);
        Mockito.when(mockConfig.discountPercentage()).thenReturn(new BigDecimal("0.30"));
        
        // Create real product to test pricing logic
        Product nonFoodProduct = new Product("N1", "Test NonFood", new BigDecimal("8.00"), 
                GoodsType.NON_FOODS, testDate.plusYears(1), 3);
        
        // Act
        BigDecimal salePrice = nonFoodProduct.salePrice(mockConfig, testDate);
        
        // Assert - 8.00 * 1.25 = 10.00 (no near-expiry discount)
        assertEquals(0, new BigDecimal("10.00").compareTo(salePrice));
    }

    @Test
    void testNearExpiryDiscount30Percent() {
        // Arrange
        Mockito.when(mockConfig.groceriesMarkup()).thenReturn(new BigDecimal("0.20"));
        Mockito.when(mockConfig.daysForNearExpiryDiscount()).thenReturn(3);
        Mockito.when(mockConfig.discountPercentage()).thenReturn(new BigDecimal("0.30"));
        
        // Create product expiring in 2 days (within 3-day threshold)
        Product nearExpiryProduct = new Product("F2", "Near Expiry Food", new BigDecimal("10.00"), 
                GoodsType.GROCERIES, testDate.plusDays(2), 5);
        
        // Act
        BigDecimal salePrice = nearExpiryProduct.salePrice(mockConfig, testDate);
        
        // Assert - (10.00 * 1.20) * (1 - 0.30) = 12.00 * 0.70 = 8.40
        assertEquals(0, new BigDecimal("8.40").compareTo(salePrice));
    }


} 