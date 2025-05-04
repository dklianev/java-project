package org.informatics.exception;

public class ProductNotFoundException extends Exception {

    public ProductNotFoundException(String id) {
        super("Product not found: " + (id));
    }
}
