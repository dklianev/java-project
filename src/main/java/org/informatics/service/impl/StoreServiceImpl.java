package org.informatics.service.impl;

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
import org.informatics.service.contract.StoreService;
import org.informatics.store.Store;

public class StoreServiceImpl implements StoreService {

    private final Store store;

    public StoreServiceImpl(Store s) {
        this.store = s;
    }

    @Override
    public Receipt sell(Cashier c, String id, int qty, Customer cust, File receiptDir)
            throws ProductNotFoundException, ProductExpiredException, InvalidQuantityException,
            InsufficientQuantityException, InsufficientBudgetException, IOException, CashDeskNotAssignedException {
        Receipt receipt = store.sell(c, id, qty, cust);
        receipt.save(receiptDir);
        return receipt;
    }

    @Override
    public double turnover() {
        return store.turnover();
    }
}
