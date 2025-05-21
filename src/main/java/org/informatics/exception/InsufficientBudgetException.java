package org.informatics.exception;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InsufficientBudgetException extends Exception {

    public InsufficientBudgetException(BigDecimal diff) {
        super("Customer lacks " + diff.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}
