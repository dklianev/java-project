package org.informatics.service.contract;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Receipt;
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.NegativePriceException;
import org.informatics.exception.NonPositiveQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.exception.ProductNullException;

public interface StoreService {

    Receipt sell(Cashier c, String id, int qty, Customer cust, File receiptDir)
            throws ProductNotFoundException, ProductExpiredException, InvalidQuantityException,
            InsufficientQuantityException, InsufficientBudgetException, IOException, CashDeskNotAssignedException,
            ProductNullException, NonPositiveQuantityException, NegativePriceException;
    
    Receipt createReceipt(Cashier c) throws CashDeskNotAssignedException;
    
    Receipt addToReceipt(Receipt receipt, String productId, int qty, Customer cust)
            throws ProductNotFoundException, ProductExpiredException, InvalidQuantityException,
            InsufficientQuantityException, InsufficientBudgetException, IOException,
            ProductNullException, NonPositiveQuantityException, NegativePriceException;
    
    void saveReceipt(Receipt receipt, File receiptDir) throws IOException;

    BigDecimal turnover();
}
