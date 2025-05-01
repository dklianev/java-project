package org.informatics.config;

/**
 * StoreConfig is now instantiated per Store, allowing different mark-ups /
 * discount windows in different tests or store branches.
 */
public class StoreConfig {

    private final double groceriesMarkup;     // Renamed from foodMarkup
    private final double nonFoodsMarkup;      // Renamed from nonFoodMarkup
    private final int    daysForNearExpiryDiscount; // More descriptive name
    private final double discountPercentage;      // More descriptive name

    public StoreConfig() {
        this(0.20, 0.25, 5, 0.30); // Default values
    }

    public StoreConfig(double groceriesMarkup, double nonFoodsMarkup,
                       int daysForNearExpiryDiscount, double discountPercentage) {
        if (groceriesMarkup < 0 || nonFoodsMarkup < 0 || discountPercentage < 0 || discountPercentage > 1) {
            throw new IllegalArgumentException("Markups and discount percentage must be non-negative, discount <= 1.");
        }
        if (daysForNearExpiryDiscount < 0) {
            throw new IllegalArgumentException("Days for near expiry discount cannot be negative.");
        }
        this.groceriesMarkup     = groceriesMarkup;
        this.nonFoodsMarkup      = nonFoodsMarkup;
        this.daysForNearExpiryDiscount = daysForNearExpiryDiscount;
        this.discountPercentage  = discountPercentage;
    }

    /* getters */
    public double groceriesMarkup()          { return groceriesMarkup; }
    public double nonFoodsMarkup()           { return nonFoodsMarkup; }
    public int    daysForNearExpiryDiscount(){ return daysForNearExpiryDiscount; }
    public double discountPercentage()       { return discountPercentage; }

    /* ---------- STATIC wrappers ------------ */
    private static final StoreConfig _DEFAULT_INSTANCE = new StoreConfig();
    public static double groceriesMarkupStatic() { return _DEFAULT_INSTANCE.groceriesMarkup(); }
    public static double nonFoodsMarkupStatic()  { return _DEFAULT_INSTANCE.nonFoodsMarkup(); }
    public static int    daysForNearExpiryDiscountStatic() { return _DEFAULT_INSTANCE.daysForNearExpiryDiscount(); }
    public static double discountPercentageStatic()      { return _DEFAULT_INSTANCE.discountPercentage(); }
} 