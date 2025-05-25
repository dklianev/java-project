package org.informatics.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.informatics.service.contract.FinancialService;
import org.informatics.store.Store;

public class FinancialServiceImpl implements FinancialService {

    private final Store store;

    public FinancialServiceImpl(Store store) {
        this.store = store;
    }

    @Override
    public BigDecimal turnover() {
        return store.turnover();
    }

    @Override
    public BigDecimal salaryExpenses() {
        return store.salaryExpenses();
    }

    @Override
    public BigDecimal costOfSoldGoods() {
        return store.costOfSoldGoods();
    }

    @Override
    public BigDecimal getTotalCostOfAllGoodsSupplied() {
        return store.getTotalCostOfAllGoodsSupplied();
    }

    @Override
    public BigDecimal profit() {
        return store.profit();
    }

    @Override
    public int getReceiptCount() {
        return store.getReceiptCount();
    }

    @Override
    public Map<String, Integer> getSoldItems() {
        return store.getSoldItems();
    }
} 