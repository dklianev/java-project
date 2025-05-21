package org.informatics.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.util.GoodsType;

public class NonFoodProduct extends Product {

    public NonFoodProduct(String id, String name, BigDecimal price, LocalDate exp, int qty) {
        super(id, name, price, GoodsType.NON_FOODS, exp, qty);
    }
}
