package org.informatics.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.informatics.entity.Receipt;
import org.informatics.service.contract.FileService;

public class FileServiceImpl implements FileService {

    @Override
    public void save(Receipt r, File dir) throws IOException {
        r.save(dir);
    }

    @Override
    public List<Receipt> loadAll(File dir) throws IOException, ClassNotFoundException {
        List<Receipt> list = new ArrayList<>();

        if (!dir.exists()) {
            return list;
        }

        File[] files = dir.listFiles((File dir1, String name) -> name.endsWith(".ser"));
        
        if (files == null) {
            return list;
        }
        
        for (File file : files) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Receipt receipt = (Receipt) ois.readObject();
                if (receipt != null) {
                    list.add(receipt);
                }
            } catch (InvalidClassException | ClassNotFoundException e) {
                System.err.println("Could not deserialize file: " + file.getName() + " - " + e.getMessage());
            }
        }

        return list;
    }

    @Override
    public Receipt load(File dir, int receiptNumber) throws IOException, ClassNotFoundException {
        File receiptFile = new File(dir, "receipt-" + receiptNumber + ".ser");
        if (!receiptFile.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(receiptFile))) {
            return (Receipt) ois.readObject();
        }
    }
}
