package org.informatics.exception;

import java.math.BigDecimal;

public class InsufficientBudgetException extends Exception {
    
    public InsufficientBudgetException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient budget. Required: %.2f, Available: %.2f", 
              required, available));
    }
}
