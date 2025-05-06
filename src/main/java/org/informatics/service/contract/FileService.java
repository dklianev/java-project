package org.informatics.service.contract;

import java.io.IOException;
import java.util.List;

import org.informatics.entity.Receipt;

public interface FileService {

    void save(Receipt r, java.io.File dir) throws IOException;

    List<Receipt> loadAll(java.io.File dir) throws IOException, ClassNotFoundException;

    Receipt load(java.io.File dir, int receiptNumber) throws IOException, ClassNotFoundException;
}
