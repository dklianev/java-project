package org.informatics.config;

import java.math.BigDecimal;

public class StoreConfig {

    private final BigDecimal groceriesMarkup;     // Markup for grocery items
    private final BigDecimal nonFoodsMarkup;      // Markup for non-food items
    private final int daysForNearExpiryDiscount;  // Days before expiry to apply discount
    private final BigDecimal discountPercentage;  // Discount percentage for near-expiry items

    public StoreConfig() {
        this(
            new BigDecimal("0.20"), 
            new BigDecimal("0.25"), 
            5, 
            new BigDecimal("0.30")
        ); // Default values
    }

    public StoreConfig(BigDecimal groceriesMarkup, BigDecimal nonFoodsMarkup,
            int daysForNearExpiryDiscount, BigDecimal discountPercentage) {
        if (groceriesMarkup.compareTo(BigDecimal.ZERO) < 0 
                || nonFoodsMarkup.compareTo(BigDecimal.ZERO) < 0 
                || discountPercentage.compareTo(BigDecimal.ZERO) < 0 
                || discountPercentage.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Markups and discount percentage must be non-negative, discount <= 1.");
        }
        if (daysForNearExpiryDiscount < 0) {
            throw new IllegalArgumentException("Days for near expiry discount cannot be negative.");
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

    // Static wrappers
    private static final StoreConfig _DEFAULT_INSTANCE = new StoreConfig();

    public static BigDecimal groceriesMarkupStatic() {
        return _DEFAULT_INSTANCE.groceriesMarkup();
    }

    public static BigDecimal nonFoodsMarkupStatic() {
        return _DEFAULT_INSTANCE.nonFoodsMarkup();
    }

    public static int daysForNearExpiryDiscountStatic() {
        return _DEFAULT_INSTANCE.daysForNearExpiryDiscount();
    }

    public static BigDecimal discountPercentageStatic() {
        return _DEFAULT_INSTANCE.discountPercentage();
    }
}
