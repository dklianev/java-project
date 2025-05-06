package org.informatics.service.contract;

import org.informatics.entity.Product;
import org.informatics.exception.DuplicateProductException;

public interface GoodsService {

    void addProduct(Product p) throws DuplicateProductException;

    java.util.List<Product> listProducts();

    Product find(String id);
}
