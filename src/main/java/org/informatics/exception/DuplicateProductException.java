package org.informatics.exception;

public class DuplicateProductException extends Exception {

    public DuplicateProductException(String id) {
        super("Duplicate product ID: " + (id));
    }
}
