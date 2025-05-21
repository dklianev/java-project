package org.informatics.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.util.GoodsType;

public class FoodProduct extends Product {

    public FoodProduct(String id, String name, BigDecimal price, LocalDate exp, int qty) {
        super(id, name, price, GoodsType.GROCERIES, exp, qty);
    }
}
