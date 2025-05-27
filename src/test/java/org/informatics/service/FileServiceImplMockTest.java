package org.informatics.service;

import org.informatics.entity.Receipt;
import org.informatics.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceImplMockTest {

    FileServiceImpl fileService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        fileService = new FileServiceImpl();
    }

    @Test
    void testLoadAllFromNonExistentDirectoryReturnsEmptyList() throws IOException {
        // Arrange
        File nonExistentDir = new File(tempDir.toFile(), "nonexistent");
        
        // Act
        List<Receipt> result = fileService.loadAll(nonExistentDir);
        
        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void testLoadAllFromDirectoryWithValidReceiptFilesReturnsReceipts() throws IOException {
        // Arrange
        File dir = tempDir.toFile();
        Receipt mockReceipt1 = Mockito.mock(Receipt.class);
        Receipt mockReceipt2 = Mockito.mock(Receipt.class);
        createSerializedReceiptFile(dir, "receipt-1.ser", mockReceipt1);
        createSerializedReceiptFile(dir, "receipt-2.ser", mockReceipt2);
        
        // Act
        List<Receipt> result = fileService.loadAll(dir);
        
        // Assert
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(1));
    }

    @Test
    void testLoadSpecificExistingReceiptReturnsReceipt() throws IOException, ClassNotFoundException {
        // Arrange
        File dir = tempDir.toFile();
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        createSerializedReceiptFile(dir, "receipt-123.ser", mockReceipt);
        
        // Act
        Receipt result = fileService.load(dir, 123);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void testLoadSpecificNonExistentReceiptReturnsNull() throws IOException, ClassNotFoundException {
        // Arrange
        File dir = tempDir.toFile();
        
        // Act
        Receipt result = fileService.load(dir, 999);
        
        // Assert
        assertNull(result);
    }

    private void createSerializedReceiptFile(File dir, String filename, Receipt receipt) throws IOException {
        File file = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(receipt);
        }
    }
} 