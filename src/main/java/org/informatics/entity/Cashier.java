package org.informatics.entity;

import java.math.BigDecimal;

public class Cashier extends Person {

    private final BigDecimal monthlySalary;

    public Cashier(String id, String name, BigDecimal sal) {
        super(id, name);
        this.monthlySalary = sal;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }
}
