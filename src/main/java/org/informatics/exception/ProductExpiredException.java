package org.informatics.exception;

public class ProductExpiredException extends Exception {

    public ProductExpiredException(String productId) {
        super("Product with ID '" + productId + "' has expired.");
    }
}
