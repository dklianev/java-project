package org.informatics.entity;

import java.time.LocalDate;

import org.informatics.util.GoodsType;

public class NonFoodProduct extends Product {

    public NonFoodProduct(String id, String name, double price, LocalDate exp, int qty) {
        super(id, name, price, GoodsType.NON_FOODS, exp, qty);
    }
}
