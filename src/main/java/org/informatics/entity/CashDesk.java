package org.informatics.entity;

import java.io.Serial;
import java.io.Serializable;

import org.informatics.exception.CashDeskOccupiedException;

public class CashDesk implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static int nextId = 1;  // Auto-incrementing ID generator for all desks

    private final String id;
    private Cashier currentCashier;
    private boolean isOpen;         // Desk is open only when a cashier is assigned

    public CashDesk() {
        this.id = "D" + nextId++;
        this.currentCashier = null;
        this.isOpen = false;
    }

    public String getId() {
        return id;
    }

    public Cashier getCurrentCashier() {
        return currentCashier;
    }

    public boolean isOpen() {
        return isOpen;
    }

    // Different from isOpen: desk can be occupied but not open
    public boolean isOccupied() {
        return currentCashier != null;
    }

    // Assigns cashier to desk, ensures only one cashier per desk
    public void assignCashier(Cashier cashier) throws CashDeskOccupiedException {
        if (this.currentCashier == null) {
            this.currentCashier = cashier;
            this.isOpen = true;
        } else {
            throw new CashDeskOccupiedException(
                "Cash desk " + id + " is already occupied by " + this.currentCashier.getName());
        }
    }

    public void releaseCashier() {
        if (this.currentCashier != null) {
            System.out.println("Cashier " + this.currentCashier.getName() + " released from desk " + id);
            this.currentCashier = null;
            this.isOpen = false;
        }
    }
} 