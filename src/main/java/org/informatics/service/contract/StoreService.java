package org.informatics.service.contract;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.informatics.config.StoreConfig;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Receipt;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;

public interface StoreService {

    Receipt sell(Cashier cashier, String productId, int quantity, Customer customer)
            throws ProductNotFoundException, ProductExpiredException, InsufficientQuantityException, InsufficientBudgetException;

    Receipt addToReceipt(Receipt receipt, String productId, int quantity, Customer customer)
            throws ProductNotFoundException, ProductExpiredException, InsufficientQuantityException, InsufficientBudgetException;

    Receipt createReceipt(Cashier c);

    void saveReceipt(Receipt receipt, File dir) throws IOException;

    List<Receipt> listReceipts();

    StoreConfig getConfig();
}
