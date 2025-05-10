package org.informatics.store;

import java.io.File;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Product;
import org.informatics.entity.Receipt;
import org.informatics.service.impl.FileServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileServiceTest {
    @TempDir
    File tempDir;

    @Test
    void saveAndLoadReceipt() throws Exception {
        FileServiceImpl fs = new FileServiceImpl();
        Store store = new Store(new StoreConfig(0.2, 0.25, 3, 0.3));
        Cashier c = new Cashier("C", "Bob", 1000);
        store.addCashier(c);
        Product p = new NonFoodProduct("N", "Notebook", 1, LocalDate.MAX, 5);
        store.addProduct(p);
        Customer cust = new Customer("CU", "Ann", 10);
        Receipt r = store.sell(c, "N", 1, cust);
        fs.save(r, tempDir);
        assertEquals(1, fs.loadAll(tempDir).size());
    }
}
