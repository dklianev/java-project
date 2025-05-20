package org.informatics.entity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int COUNTER = 0;
    private final int number = ++COUNTER;
    private final Cashier cashier;
    private final LocalDateTime time = LocalDateTime.now();
    private final List<Line> lines = new ArrayList<>();

    public Receipt(Cashier cashier) {
        this.cashier = cashier;
    }

    public int getNumber() {
        return number;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public List<Line> getLines() {
        List<Line> result = new ArrayList<>();
        for (Line line : lines) {
            result.add(line);
        }
        return result;
    }

    public void add(Product product, int quantity, double price) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        
        lines.add(new Line(product, quantity, price));
    }

    public double total() {
        double sum = 0.0;
        for (Line line : lines) {
            sum += line.getPrice() * line.getQuantity();
        }
        return sum;
    }

    public static int getReceiptCount() {
        return COUNTER;
    }


     //Saves this receipt as both a .txt and a .ser file under the given directory.
    public void save(File dir) throws IOException {
        if (!dir.exists()) dir.mkdirs();

        try ( // 1) write human-readable text file
                PrintWriter pw = new PrintWriter(new File(dir, "receipt-" + number + ".txt"))) {
            pw.print(this.toString());
        }

        try ( // 2) write serialized object
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(dir, "receipt-" + number + ".ser")))) {
            oos.writeObject(this);
        }
    }

    public void save(Path dir) throws IOException {
        save(dir.toFile());
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT #").append(number).append("\n");
        sb.append("Date: ").append(time.format(formatter)).append("\n");
        sb.append("Cashier: ").append(cashier.getName()).append(" (ID: ").append(cashier.getId()).append(")\n");
        sb.append("----------------------------------------\n");
        sb.append("ITEMS:\n");
        
        for (Line line : lines) {
            sb.append(String.format("%-20s %3d x %7.2f = %8.2f\n", 
                    line.getProduct().getName(), 
                    line.getQuantity(), 
                    line.getPrice(), 
                    line.getQuantity() * line.getPrice()));
        }
        
        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL: %33.2f\n", total()));
        return sb.toString();
    }

    public static class Line implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Product product;
        private final int quantity;
        private final double price;

        public Line(Product product, int quantity, double price) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }
}
