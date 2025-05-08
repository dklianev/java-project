package org.informatics.service.impl;

import java.util.List;

import org.informatics.entity.Product;
import org.informatics.exception.DuplicateProductException;
import org.informatics.service.contract.GoodsService;
import org.informatics.store.Store;

public class GoodsServiceImpl implements GoodsService {

    private final Store store;

    public GoodsServiceImpl(Store s) {
        this.store = s;
    }

    @Override
    public void addProduct(Product p) throws DuplicateProductException {
        store.addProduct(p);
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
