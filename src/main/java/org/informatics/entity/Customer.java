package org.informatics.entity;

import java.math.BigDecimal;

import org.informatics.exception.InsufficientBudgetException;

public class Customer extends Person {

    private BigDecimal balance;

    public Customer(String id, String name, BigDecimal balance) {
        super(id, name);
        this.balance = balance;
    }

    public void pay(BigDecimal amt) throws InsufficientBudgetException {
        if (balance.compareTo(amt) < 0) {
            throw new InsufficientBudgetException(amt.subtract(balance));
        }
        balance = balance.subtract(amt);
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
}
