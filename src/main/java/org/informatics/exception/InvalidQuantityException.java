package org.informatics.exception;

public class InvalidQuantityException extends Exception {

    public InvalidQuantityException(int qty) {
        super("Invalid quantity: " + (qty));
    }
}
