package org.informatics.service.impl;

import java.util.List;

import org.informatics.entity.Receipt;
import org.informatics.service.contract.ReceiptService;
import org.informatics.store.Store;

public class ReceiptServiceImpl implements ReceiptService {

    private final Store store;

    public ReceiptServiceImpl(Store s) {
        this.store = s;
    }

    @Override
    public List<Receipt> listReceipts() {
        return store.listReceipts();
    }
}
