package org.informatics.service.contract;

import java.util.List;
import java.util.Optional;

import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;

public interface CashdeskService {

    void addCashier(Cashier c);

    List<Cashier> listCashiers();

    void addCashDesk(CashDesk desk);

    List<CashDesk> listCashDesks();

    void assignCashierToDesk(String cashierId, String deskId) throws Exception;

    void releaseCashierFromDesk(String deskId) throws Exception;

    Optional<CashDesk> getAssignedDeskForCashier(String cashierId);

    Optional<CashDesk> findCashDeskById(String deskId);

    Optional<Cashier> findCashierById(String cashierId);
}
