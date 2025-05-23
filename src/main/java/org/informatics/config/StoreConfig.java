package org.informatics.config;

import java.math.BigDecimal;

import org.informatics.exception.InvalidConfigurationException;

public class StoreConfig {

    private final BigDecimal groceriesMarkup;     // Markup for grocery items
    private final BigDecimal nonFoodsMarkup;      // Markup for non-food items
    private final int daysForNearExpiryDiscount;  // Days before expiry to apply discount
    private final BigDecimal discountPercentage;  // Discount percentage for near-expiry items

    public StoreConfig(BigDecimal groceriesMarkup, BigDecimal nonFoodsMarkup,
            int daysForNearExpiryDiscount, BigDecimal discountPercentage) throws InvalidConfigurationException {
        if (groceriesMarkup.compareTo(BigDecimal.ZERO) < 0 
                || nonFoodsMarkup.compareTo(BigDecimal.ZERO) < 0 
                || discountPercentage.compareTo(BigDecimal.ZERO) < 0 
                || discountPercentage.compareTo(BigDecimal.ONE) > 0) {
            throw new InvalidConfigurationException("Markups and discount percentage must be non-negative, discount <= 1.");
        }
        if (daysForNearExpiryDiscount < 0) {
            throw new InvalidConfigurationException("Days for near expiry discount cannot be negative.");
        }
        this.groceriesMarkup = groceriesMarkup;
        this.nonFoodsMarkup = nonFoodsMarkup;
        this.daysForNearExpiryDiscount = daysForNearExpiryDiscount;
        this.discountPercentage = discountPercentage;
    }

    // Getters
    public BigDecimal groceriesMarkup() {
        return groceriesMarkup;
    }

    public BigDecimal nonFoodsMarkup() {
        return nonFoodsMarkup;
    }

    public int daysForNearExpiryDiscount() {
        return daysForNearExpiryDiscount;
    }

    public BigDecimal discountPercentage() {
        return discountPercentage;
    }
}
