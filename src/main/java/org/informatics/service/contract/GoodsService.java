package org.informatics.service.contract;

import org.informatics.entity.Product;

public interface GoodsService {

    boolean addProduct(Product p);

    java.util.List<Product> listProducts();

    Product find(String id);
}
