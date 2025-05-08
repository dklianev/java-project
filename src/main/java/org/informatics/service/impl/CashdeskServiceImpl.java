package org.informatics.service.impl;

import java.util.List;
import java.util.Optional;

import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.service.contract.CashdeskService;
import org.informatics.store.Store;

public class CashdeskServiceImpl implements CashdeskService {

    private final Store store;

    public CashdeskServiceImpl(Store s) {
        this.store = s;
    }

    @Override
    public void addCashier(Cashier c) {
        store.addCashier(c);
    }

    @Override
    public List<Cashier> listCashiers() {
        return store.listCashiers();
    }

    @Override
    public void addCashDesk(CashDesk desk) {
        store.addCashDesk(desk);
    }

    @Override
    public List<CashDesk> listCashDesks() {
        return store.listCashDesks();
    }

    @Override
    public void assignCashierToDesk(String cashierId, String deskId) throws Exception {
        store.assignCashierToDesk(cashierId, deskId);
    }

    @Override
    public void releaseCashierFromDesk(String deskId) throws Exception {
        store.releaseCashierFromDesk(deskId);
    }

    @Override
    public Optional<CashDesk> getAssignedDeskForCashier(String cashierId) {
        return store.getAssignedDeskForCashier(cashierId);
    }

    @Override
    public Optional<CashDesk> findCashDeskById(String deskId) {
        return store.findCashDeskById(deskId);
    }

    @Override
    public Optional<Cashier> findCashierById(String cashierId) {
        return store.findCashierById(cashierId);
    }
}
