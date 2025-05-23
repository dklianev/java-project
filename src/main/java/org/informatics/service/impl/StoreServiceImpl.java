package org.informatics.service.impl;

import java.io.File;
import java.io.IOException;

import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Receipt;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import org.informatics.service.contract.StoreService;
import org.informatics.store.Store;

public class StoreServiceImpl implements StoreService {

    private final Store store;

    public StoreServiceImpl(Store store) {
        this.store = store;
    }

    @Override
    public Receipt sell(Cashier cashier, String productId, int quantity, Customer customer)
            throws ProductNotFoundException, ProductExpiredException, InsufficientQuantityException, InsufficientBudgetException, IOException {
        return store.sell(cashier, productId, quantity, customer);
    }

    @Override
    public Receipt addToReceipt(Receipt receipt, String productId, int quantity, Customer customer)
            throws ProductNotFoundException, ProductExpiredException, InsufficientQuantityException, InsufficientBudgetException {
        return store.addToReceipt(receipt, productId, quantity, customer);
    }

    @Override
    public Receipt createReceipt(Cashier c) {
        return store.createReceipt(c);
    }

    @Override
    public void saveReceipt(Receipt receipt, File dir) throws IOException {
        receipt.save(dir);
    }
}
