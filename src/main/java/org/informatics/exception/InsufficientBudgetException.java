package org.informatics.exception;

public class InsufficientBudgetException extends Exception {

    public InsufficientBudgetException(double diff) {
        super("Customer lacks " + String.format("%.2f", diff));
    }
}
