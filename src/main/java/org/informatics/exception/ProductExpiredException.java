package org.informatics.exception;

public class ProductExpiredException extends Exception {

    public ProductExpiredException(String id) {
        super("Product expired: " + (id));
    }
}
