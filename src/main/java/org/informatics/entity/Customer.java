package org.informatics.entity;

import java.math.BigDecimal;

import org.informatics.exception.InsufficientBudgetException;

public class Customer extends Person {

    private BigDecimal balance;

    public Customer(String id, String name, BigDecimal balance) {
        super(id, name);
        this.balance = balance;
    }

    // Check if customer has enough money
    public void pay(BigDecimal amt) throws InsufficientBudgetException {
        if (balance.compareTo(amt) < 0) {
            throw new InsufficientBudgetException(amt, balance);
        }
        balance = balance.subtract(amt);
    }
}
