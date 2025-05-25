package org.informatics.config;

import java.math.BigDecimal;

// Store settings for pricing and discounts
public record StoreConfig(
        BigDecimal groceriesMarkup, // Markup for grocery items
        BigDecimal nonFoodsMarkup, // Markup for non-food items
        int daysForNearExpiryDiscount, // Days before expiry to apply discount
        BigDecimal discountPercentage // Discount percentage for near-expiry items
        ) {

    public StoreConfig    {
        if (groceriesMarkup.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Groceries markup cannot be negative");
        }
        if (nonFoodsMarkup.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Non-foods markup cannot be negative");
        }
        if (daysForNearExpiryDiscount < 0) {
            throw new IllegalArgumentException("Days for near expiry discount cannot be negative");
        }
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 1");
        }
    }
}
