package org.informatics.exception;

public class NonPositiveQuantityException extends Exception {
    public NonPositiveQuantityException() {
        super("Quantity must be positive");
    }
} 