package org.informatics.service.contract;

import java.util.List;

import org.informatics.entity.Product;

public interface GoodsService {

    boolean addProduct(Product p);

    boolean restockProduct(String productId, int additionalQuantity) throws IllegalArgumentException;

    List<Product> listProducts();

    Product find(String id);
}
