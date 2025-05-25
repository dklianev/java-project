package org.informatics.service.contract;

import java.math.BigDecimal;
import java.util.Map;

public interface FinancialService {

    BigDecimal turnover();

    BigDecimal salaryExpenses();

    BigDecimal costOfSoldGoods();

    BigDecimal getTotalCostOfAllGoodsSupplied();

    BigDecimal profit();

    int getReceiptCount();

    Map<String, Integer> getSoldItems();
} 