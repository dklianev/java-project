package org.informatics.exception;

public class ProductNullException extends Exception {
    public ProductNullException() {
        super("Product cannot be null");
    }
} 