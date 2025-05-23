package org.informatics.exception;

public class ProductNotFoundException extends Exception {

    public ProductNotFoundException(String productId) {
        super("Product with ID '" + productId + "' not found.");
    }
}
