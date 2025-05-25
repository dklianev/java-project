package org.informatics.service.impl;

import java.util.List;

import org.informatics.entity.Product;
import org.informatics.service.contract.GoodsService;
import org.informatics.store.Store;

public class GoodsServiceImpl implements GoodsService {

    private final Store store;

    public GoodsServiceImpl(Store store) {
        this.store = store;
    }

    @Override
    public boolean addProduct(Product p) {
        return store.addProduct(p);
    }

    @Override
    public boolean restockProduct(String productId, int additionalQuantity) throws IllegalArgumentException {
        return store.restockProduct(productId, additionalQuantity);
    }

    @Override
    public List<Product> listProducts() {
        return store.listProducts();
    }

    @Override
    public Product find(String id) {
        return store.find(id);
    }
}
