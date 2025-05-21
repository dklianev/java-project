package org.informatics.exception;

import java.math.BigDecimal;

public class InsufficientBudgetException extends Exception {

    public InsufficientBudgetException(BigDecimal diff) {
        super("Customer lacks " + diff.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
    }
}
