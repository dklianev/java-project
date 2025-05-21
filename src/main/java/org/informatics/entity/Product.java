package org.informatics.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.util.GoodsType;

public class Product implements Serializable, Comparable<Product> {
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

    public void addQuantity(int d) {
        qty += d;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public boolean isExpired(LocalDate today) {
        return !expiry.isAfter(today);
    }

    public BigDecimal salePrice(StoreConfig cfg, LocalDate today) {
        BigDecimal markup = type == GoodsType.GROCERIES ? 
                cfg.groceriesMarkup() : cfg.nonFoodsMarkup();
        
        BigDecimal price = purchasePrice.multiply(
                BigDecimal.ONE.add(markup));

        if (!isExpired(today)
                && expiry.minusDays(cfg.daysForNearExpiryDiscount()).isBefore(today)) {
            price = price.multiply(
                    BigDecimal.ONE.subtract(cfg.discountPercentage()));
        }

        return price;
    }

    @Override
    public int compareTo(Product o) {
        return name.compareToIgnoreCase(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Product p) {
            return id.equals(p.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + id + ") qty:" + qty;
    }
}
