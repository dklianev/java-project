package org.informatics.entity;

import org.informatics.exception.InsufficientBudgetException;

public class Customer extends Person {

    private double balance;

    public Customer(String id, String name, double balance) {
        super(id, name);
        this.balance = balance;
    }

    public void pay(double amt) throws InsufficientBudgetException {
        if (balance < amt) {
            throw new InsufficientBudgetException(amt - balance);
        }
        balance -= amt;
    }

    public double getBalance() {
        return balance;
    }
}
