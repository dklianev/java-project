package org.informatics.service.contract;

import org.informatics.entity.Product;

public interface GoodsService {

    //Adds a product to the store.
    //Тrue if successful, false if product with same ID already exists
    boolean addProduct(Product p);

    java.util.List<Product> listProducts();

    Product find(String id);
}
