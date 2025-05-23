package org.informatics.entity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Receipt implements Serializable {

    @Serial
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
        return new ArrayList<>(lines);
    }

    public void add(Product product, int quantity, BigDecimal price) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        lines.add(new Line(product, quantity, price));
    }

    public BigDecimal total() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Line line : lines) {
            BigDecimal lineTotal = line.price().multiply(BigDecimal.valueOf(line.quantity()));
            sum = sum.add(lineTotal);
        }
        return sum;
    }

    public static int getReceiptCount() {
        return COUNTER;
    }

    public static void resetCounter() {
        COUNTER = 0;
    }

    // Saves receipt as both .txt and .ser files, creates directory if needed
    public void save(File dir) throws IOException {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Unable to create directory for receipts: " + dir.getAbsolutePath());
            }
        }

        // Write human-readable text file
        File txtFile = new File(dir, "receipt-" + number + ".txt");
        try (PrintWriter pw = new PrintWriter(txtFile)) {
            pw.print(this);
        }

        // Write serialized object
        File serFile = new File(dir, "receipt-" + number + ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFile))) {
            oos.writeObject(this);
        }
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
            BigDecimal lineTotal = line.price().multiply(BigDecimal.valueOf(line.quantity()));
            sb.append(String.format("%-20s %3d x %7.2f = %8.2f\n",
                    line.product().getName(),
                    line.quantity(),
                    line.price(),
                    lineTotal));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL: %33.2f\n", total()));
        return sb.toString();
    }

    public record Line(Product product, int quantity, BigDecimal price) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
    }
}
