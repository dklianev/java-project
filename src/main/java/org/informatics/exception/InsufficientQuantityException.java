package org.informatics.exception;

public class InsufficientQuantityException extends Exception {

    public InsufficientQuantityException(String id, int req, int avail) {
        super("Need " + (req - avail) + " more of " + id + " (only " + avail + " in stock)");
    }
}
