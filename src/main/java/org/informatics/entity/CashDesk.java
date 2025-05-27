package org.informatics.entity;

import java.io.Serial;
import java.io.Serializable;

import org.informatics.exception.CashDeskOccupiedException;

public class CashDesk implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static int nextId = 1;  // Auto ID for desks

    private final String id;
    private Cashier currentCashier;

    public CashDesk() {
        this.id = "D" + nextId++;
        this.currentCashier = null;
    }

    public String getId() {
        return id;
    }

    public Cashier getCurrentCashier() {
        return currentCashier;
    }

    public boolean isOccupied() {
        return currentCashier != null;
    }

    // Assign cashier to desk
    public void assignCashier(Cashier cashier) throws CashDeskOccupiedException {
        if (this.currentCashier == null) {
            this.currentCashier = cashier;
        } else {
            throw new CashDeskOccupiedException(
                "Cash desk " + id + " is already occupied by " + this.currentCashier.getName());
        }
    }

    public void releaseCashier() {
        if (this.currentCashier != null) {
            System.out.println("Cashier " + this.currentCashier.getName() + " released from desk " + id);
            this.currentCashier = null;
        }
    }
} 