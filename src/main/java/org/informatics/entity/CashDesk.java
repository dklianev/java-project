package org.informatics.entity;

import java.io.Serializable;

public class CashDesk implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int nextId = 1;

    private final String id;
    private Cashier currentCashier;
    private boolean isOpen;

    public CashDesk() {
        this.id = "D" + nextId++;
        this.currentCashier = null;
        this.isOpen = false; // A desk must be explicitly opened by a cashier
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

    public boolean isOccupied() {
        return currentCashier != null;
    }

    public void assignCashier(Cashier cashier) {
        if (this.currentCashier == null) {
            this.currentCashier = cashier;
            this.isOpen = true;
        } else {
            // Optionally throw an exception or handle if desk is already occupied
            System.err.println("Cash desk " + id + " is already occupied by " + this.currentCashier.getName());
        }
    }

    public void releaseCashier() {
        if (this.currentCashier != null) {
            System.out.println("Cashier " + this.currentCashier.getName() + " released from desk " + id);
            this.currentCashier = null;
            this.isOpen = false;
        }
    }

    @Override
    public String toString() {
        return "CashDesk [id=" + id + ", currentCashier=" + (currentCashier != null ? currentCashier.getName() : "None") + ", isOpen=" + isOpen + "]";
    }
} 