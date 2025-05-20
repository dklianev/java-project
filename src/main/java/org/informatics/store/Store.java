package org.informatics.store;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Product;
import org.informatics.entity.Receipt;
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.CashDeskOccupiedException;
import org.informatics.exception.DuplicateProductException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;

public class Store {

    private final StoreConfig cfg;
    private final Map<String, Product> inventory = new HashMap<>();
    private final List<Receipt> receipts = new ArrayList<>();
    private final List<Cashier> cashiers = new ArrayList<>();
    private final List<CashDesk> cashDesks = new ArrayList<>();
    private final Map<String, Integer> soldItems = new HashMap<>();
    private double costOfSoldGoods = 0.0;
    private double totalCostOfAllGoodsSupplied = 0.0;

    public Store(StoreConfig cfg) {
        this.cfg = cfg;
    }

    public void addCashier(Cashier c) {
        cashiers.add(c);
    }

    public void addCashDesk(CashDesk desk) {
        cashDesks.add(desk);
    }

    public List<CashDesk> listCashDesks() {
        return new ArrayList<>(cashDesks);
    }

    public Optional<CashDesk> findCashDeskById(String deskId) {
        return cashDesks.stream().filter(d -> d.getId().equals(deskId)).findFirst();
    }

    public Optional<Cashier> findCashierById(String cashierId) {
        return cashiers.stream().filter(c -> c.getId().equals(cashierId)).findFirst();
    }

    public void assignCashierToDesk(String cashierId, String deskId) throws Exception {
        Cashier cashier = findCashierById(cashierId)
                .orElseThrow(() -> new Exception("Cashier with ID " + cashierId + " not found."));
        CashDesk desk = findCashDeskById(deskId)
                .orElseThrow(() -> new Exception("CashDesk with ID " + deskId + " not found."));

        for (CashDesk d : cashDesks) {
            if (d.getCurrentCashier() != null && d.getCurrentCashier().getId().equals(cashierId) && !d.getId().equals(deskId)) {
                throw new Exception("Cashier " + cashierId + " is already assigned to desk " + d.getId());
            }
        }
        if (desk.isOccupied() && !desk.getCurrentCashier().getId().equals(cashierId)) {
            throw new Exception("Desk " + deskId + " is already occupied by cashier " + desk.getCurrentCashier().getId());
        }

        cashDesks.stream()
            .filter(d -> d.getCurrentCashier() != null && d.getCurrentCashier().getId().equals(cashierId))
            .findFirst()
            .ifPresent(CashDesk::releaseCashier);

        try {
            desk.assignCashier(cashier);
            System.out.println("Cashier " + cashier.getName() + " assigned to desk " + desk.getId());
        } catch (CashDeskOccupiedException e) {
            throw new Exception(e.getMessage());
        }
    }

    public void releaseCashierFromDesk(String deskId) throws Exception {
        CashDesk desk = findCashDeskById(deskId)
                .orElseThrow(() -> new Exception("CashDesk with ID " + deskId + " not found."));
        if (!desk.isOccupied()) {
            return;
        }
        desk.releaseCashier();
    }

    public Optional<CashDesk> getAssignedDeskForCashier(String cashierId) {
        return cashDesks.stream()
                .filter(desk -> desk.isOpen() && desk.getCurrentCashier() != null && desk.getCurrentCashier().getId().equals(cashierId))
                .findFirst();
    }

    public void addProduct(Product p) throws DuplicateProductException {
        if (inventory.containsKey(p.getId())) {
            throw new DuplicateProductException(p.getId());
        }
        inventory.put(p.getId(), p);
        totalCostOfAllGoodsSupplied += p.getPurchasePrice() * p.getQuantity();
    }

    public Product find(String id) {
        return inventory.get(id);
    }

    public List<Product> listProducts() {
        return new ArrayList<>(inventory.values());
    }

    public List<Receipt> listReceipts() {
        return new ArrayList<>(receipts);
    }

    public List<Cashier> listCashiers() {
        return new ArrayList<>(cashiers);
    }

    public Receipt sell(Cashier cashier, String productId, int qty, Customer cust)
            throws ProductNotFoundException, ProductExpiredException, InvalidQuantityException,
            InsufficientQuantityException, InsufficientBudgetException, IOException, CashDeskNotAssignedException {
        if (qty <= 0) {
            throw new InvalidQuantityException(qty);
        }

        getAssignedDeskForCashier(cashier.getId())
                .orElseThrow(() -> new CashDeskNotAssignedException("Cashier " + cashier.getName() + " is not assigned to an open cash desk."));
        
        Product p = inventory.get(productId);
        if (p == null) {
            throw new ProductNotFoundException(productId);
        }
        if (p.isExpired(LocalDate.now())) {
            throw new ProductExpiredException(productId);
        }
        if (p.getQuantity() < qty) {
            throw new InsufficientQuantityException(productId, qty, p.getQuantity());
        }
        double price = p.salePrice(cfg, LocalDate.now());
        double totalPrice = price * qty;
        cust.pay(totalPrice);
        p.addQuantity(-qty);
        Integer currentQty = soldItems.get(productId);
        if (currentQty == null) {
            soldItems.put(productId, qty);
        } else {
            soldItems.put(productId, currentQty + qty);
        }
        costOfSoldGoods += p.getPurchasePrice() * qty;
        Receipt r = new Receipt(cashier);
        r.add(p, qty, price);
        receipts.add(r);
        return r;
    }

    public Receipt addToReceipt(Receipt receipt, String productId, int qty, Customer cust)
            throws ProductNotFoundException, ProductExpiredException, InvalidQuantityException,
            InsufficientQuantityException, InsufficientBudgetException, IOException {
        if (qty <= 0) {
            throw new InvalidQuantityException(qty);
        }
        
        Product p = inventory.get(productId);
        if (p == null) {
            throw new ProductNotFoundException(productId);
        }
        if (p.isExpired(LocalDate.now())) {
            throw new ProductExpiredException(productId);
        }
        if (p.getQuantity() < qty) {
            throw new InsufficientQuantityException(productId, qty, p.getQuantity());
        }
        double price = p.salePrice(cfg, LocalDate.now());
        double totalPrice = price * qty;
        cust.pay(totalPrice);
        p.addQuantity(-qty);
        Integer currentQty = soldItems.get(productId);
        if (currentQty == null) {
            soldItems.put(productId, qty);
        } else {
            soldItems.put(productId, currentQty + qty);
        }
        costOfSoldGoods += p.getPurchasePrice() * qty;
        receipt.add(p, qty, price);
        return receipt;
    }

    public Receipt createReceipt(Cashier cashier) throws CashDeskNotAssignedException {
        getAssignedDeskForCashier(cashier.getId())
                .orElseThrow(() -> new CashDeskNotAssignedException("Cashier " + cashier.getName() + " is not assigned to an open cash desk."));
        
        Receipt r = new Receipt(cashier);
        receipts.add(r);
        return r;
    }

    public double turnover() {
        double total = 0.0;
        for (Receipt receipt : receipts) {
            total += receipt.total();
        }
        return total;
    }

    public Map<String, Integer> getSoldItems() {
        return new HashMap<>(soldItems);
    }

    public double salaryExpenses() {
        double total = 0.0;
        for (Cashier cashier : cashiers) {
            total += cashier.getMonthlySalary();
        }
        return total;
    }

    public double costOfSoldGoods() {
        return costOfSoldGoods;
    }

    public double getTotalCostOfAllGoodsSupplied() {
        return totalCostOfAllGoodsSupplied;
    }

    public double profit() {
        return turnover() - salaryExpenses() - costOfSoldGoods;
    }

    public int getReceiptCount() {
        return Receipt.getReceiptCount();
    }

    public StoreConfig getConfig() {
        return cfg;
    }
}
