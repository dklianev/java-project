package org.informatics.entity;

import java.time.LocalDate;

import org.informatics.util.GoodsType;

public class FoodProduct extends Product {

    public FoodProduct(String id, String name, double price, LocalDate exp, int qty) {
        super(id, name, price, GoodsType.GROCERIES, exp, qty);
    }
}
