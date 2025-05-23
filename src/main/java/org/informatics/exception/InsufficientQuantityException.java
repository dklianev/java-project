package org.informatics.exception;

public class InsufficientQuantityException extends Exception {

    public InsufficientQuantityException(String productId, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient quantity for product '%s'. Requested: %d, Available: %d",
                productId, requestedQuantity, availableQuantity));
    }
}
