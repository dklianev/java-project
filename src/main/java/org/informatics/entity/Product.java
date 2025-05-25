package org.informatics.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.util.GoodsType;

public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String name;
    private final BigDecimal purchasePrice;
    private final GoodsType type;
    private final LocalDate expiry;
    private int qty;

    public Product(String id, String name, BigDecimal price, GoodsType type, LocalDate exp, int qty) {
        this.id = id;
        this.name = name;
        this.purchasePrice = price;
        this.type = type;
        this.expiry = exp;
        this.qty = qty;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public GoodsType getType() {
        return type;
    }

    public int getQuantity() {
        return qty;
    }

    // Can accept negative values for sales (reducing inventory)
    public void addQuantity(int d) {
        qty += d;
    }

    // Product is expired if expiry date is today or earlier
    public boolean isExpired(LocalDate today) {
        return !expiry.isAfter(today);
    }

    // Calculate sale price with markup and discount
    public BigDecimal salePrice(StoreConfig cfg, LocalDate today) {
        // Apply markup for category
        BigDecimal markup = type == GoodsType.GROCERIES
                ? cfg.groceriesMarkup() : cfg.nonFoodsMarkup();

        BigDecimal price = purchasePrice.multiply(
                BigDecimal.ONE.add(markup));

        // Apply near-expiry discount if product is close to expiration
        if (!isExpired(today)
                && !expiry.minusDays(cfg.daysForNearExpiryDiscount()).isAfter(today)) {
            price = price.multiply(
                    BigDecimal.ONE.subtract(cfg.discountPercentage()));
        }

        return price;
    }
}
