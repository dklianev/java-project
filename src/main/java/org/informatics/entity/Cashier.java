package org.informatics.entity;

public class Cashier extends Person {

    private final double monthlySalary;

    public Cashier(String id, String name, double sal) {
        super(id, name);
        this.monthlySalary = sal;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }
}
