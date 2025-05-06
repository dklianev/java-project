package org.informatics.service.contract;

import java.io.File;
import java.io.IOException;

import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Receipt;
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;

public interface StoreService {

    Receipt sell(Cashier c, String id, int qty, Customer cust, File receiptDir)
            throws ProductNotFoundException, ProductExpiredException, InvalidQuantityException,
            InsufficientQuantityException, InsufficientBudgetException, IOException, CashDeskNotAssignedException;

    double turnover();
}
