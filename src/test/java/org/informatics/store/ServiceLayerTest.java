package org.informatics.store;

import java.util.List;
import java.util.Optional;

import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.Product;
import org.informatics.entity.Receipt;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.service.impl.GoodsServiceImpl;
import org.informatics.service.impl.StoreServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ServiceLayerTest {

    private Store mockStore;
    private GoodsServiceImpl goodsService;
    private CashdeskServiceImpl cashdeskService;
    private StoreServiceImpl storeService;

    @BeforeEach
    void setUp() {
        mockStore = Mockito.mock(Store.class);
        goodsService = new GoodsServiceImpl(mockStore);
        cashdeskService = new CashdeskServiceImpl(mockStore);
        storeService = new StoreServiceImpl(mockStore);
    }

    // GoodsService Tests
    @Test
    void testGoodsServiceAddProduct() {
        Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockStore.addProduct(mockProduct)).thenReturn(true);
        
        boolean result = goodsService.addProduct(mockProduct);
        
        assertTrue(result);
    }

    @Test
    void testGoodsServiceAddDuplicateProduct() {
        Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockStore.addProduct(mockProduct)).thenReturn(false);

        boolean result = goodsService.addProduct(mockProduct);

        assertFalse(result);
    }

    @Test
    void testGoodsServiceListProducts() {
        Product mockProduct1 = Mockito.mock(Product.class);
        Product mockProduct2 = Mockito.mock(Product.class);
        List<Product> mockProducts = List.of(mockProduct1, mockProduct2);
        Mockito.when(mockStore.listProducts()).thenReturn(mockProducts);

        List<Product> products = goodsService.listProducts();

        assertEquals(2, products.size());
    }

    @Test
    void testGoodsServiceListEmptyProducts() {
        Mockito.when(mockStore.listProducts()).thenReturn(List.of());
        
        List<Product> products = goodsService.listProducts();
        
        assertEquals(0, products.size());
    }

    @Test
    void testGoodsServiceFindExistingProduct() {
        Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockProduct.getName()).thenReturn("Milk");
        Mockito.when(mockProduct.getId()).thenReturn("F1");
        Mockito.when(mockStore.find("F1")).thenReturn(mockProduct);

        Product foundProduct = goodsService.find("F1");

        assertNotNull(foundProduct);
        assertEquals("Milk", foundProduct.getName());
        assertEquals("F1", foundProduct.getId());
    }

    @Test
    void testGoodsServiceFindNonExistentProduct() {
        Mockito.when(mockStore.find("NONEXISTENT")).thenReturn(null);
        
        Product foundProduct = goodsService.find("NONEXISTENT");
        
        assertNull(foundProduct);
    }

    // CashdeskService Tests
    @Test
    void testCashdeskServiceAddCashier() {
        Cashier mockCashier = Mockito.mock(Cashier.class);
        
        cashdeskService.addCashier(mockCashier);
        
        Mockito.verify(mockStore).addCashier(mockCashier);
    }

    @Test
    void testCashdeskServiceListCashiers() {
        Cashier mockCashier1 = Mockito.mock(Cashier.class);
        Cashier mockCashier2 = Mockito.mock(Cashier.class);
        Mockito.when(mockCashier1.getName()).thenReturn("John");
        Mockito.when(mockCashier2.getName()).thenReturn("Jane");
        List<Cashier> mockCashiers = List.of(mockCashier1, mockCashier2);
        Mockito.when(mockStore.listCashiers()).thenReturn(mockCashiers);

        List<Cashier> cashiers = cashdeskService.listCashiers();

        assertEquals(2, cashiers.size());
        assertEquals("John", cashiers.getFirst().getName());
    }

    @Test
    void testCashdeskServiceAddCashDesk() {
        CashDesk mockDesk = Mockito.mock(CashDesk.class);
        
        cashdeskService.addCashDesk(mockDesk);
        
        Mockito.verify(mockStore).addCashDesk(mockDesk);
    }

    @Test
    void testCashdeskServiceFindCashierById() {
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Mockito.when(mockCashier.getName()).thenReturn("John");
        Mockito.when(mockStore.findCashierById("C1")).thenReturn(Optional.of(mockCashier));

        Optional<Cashier> foundCashier = cashdeskService.findCashierById("C1");

        assertTrue(foundCashier.isPresent());
        assertEquals("John", foundCashier.get().getName());
    }

    @Test
    void testCashdeskServiceFindNonExistentCashier() {
        Mockito.when(mockStore.findCashierById("NONEXISTENT")).thenReturn(Optional.empty());
        
        Optional<Cashier> foundCashier = cashdeskService.findCashierById("NONEXISTENT");
        
        assertTrue(foundCashier.isEmpty());
    }

    @Test
    void testCashdeskServiceFindCashDeskById() {
        CashDesk mockDesk = Mockito.mock(CashDesk.class);
        Mockito.when(mockDesk.getId()).thenReturn("DESK1");
        Mockito.when(mockStore.findCashDeskById("DESK1")).thenReturn(Optional.of(mockDesk));

        Optional<CashDesk> foundDesk = cashdeskService.findCashDeskById("DESK1");

        assertTrue(foundDesk.isPresent());
        assertEquals("DESK1", foundDesk.get().getId());
    }

    @Test
    void testCashdeskServiceAssignCashierToDesk() throws Exception {
        cashdeskService.assignCashierToDesk("C1", "DESK1");

        Mockito.verify(mockStore).assignCashierToDesk("C1", "DESK1");
    }

    @Test
    void testCashdeskServiceReleaseCashierFromDesk() throws Exception {
        cashdeskService.releaseCashierFromDesk("DESK1");

        Mockito.verify(mockStore).releaseCashierFromDesk("DESK1");
    }

    @Test
    void testCashdeskServiceGetAssignedDeskForCashier() {
        CashDesk mockDesk = Mockito.mock(CashDesk.class);
        Mockito.when(mockDesk.getId()).thenReturn("DESK1");
        Mockito.when(mockStore.getAssignedDeskForCashier("C1")).thenReturn(Optional.of(mockDesk));

        Optional<CashDesk> assignedDesk = cashdeskService.getAssignedDeskForCashier("C1");

        assertTrue(assignedDesk.isPresent());
        assertEquals("DESK1", assignedDesk.get().getId());
    }

    @Test
    void testCashdeskServiceGetAssignedDeskForUnassignedCashier() {
        Mockito.when(mockStore.getAssignedDeskForCashier("C1")).thenReturn(Optional.empty());

        Optional<CashDesk> assignedDesk = cashdeskService.getAssignedDeskForCashier("C1");

        assertTrue(assignedDesk.isEmpty());
    }

    // StoreService Tests
    @Test
    void testStoreServiceCreateReceipt() {
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        Mockito.when(mockReceipt.getCashier()).thenReturn(mockCashier);
        Mockito.when(mockStore.createReceipt(mockCashier)).thenReturn(mockReceipt);

        Receipt receipt = storeService.createReceipt(mockCashier);

        assertNotNull(receipt);
        assertEquals(mockCashier, receipt.getCashier());
    }

    @Test
    void testStoreServiceCreateReceiptWithUnassignedCashierThrowsException() {
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Mockito.when(mockStore.createReceipt(mockCashier))
                .thenThrow(new IllegalStateException("Cashier is not assigned to an open cash desk"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> storeService.createReceipt(mockCashier));
        
        assertTrue(exception.getMessage().contains("not assigned to an open cash desk"));
    }

    @Test
    void testStoreServiceSellProduct() throws Exception {
        Cashier mockCashier = Mockito.mock(Cashier.class);
        Customer mockCustomer = Mockito.mock(Customer.class);
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        Mockito.when(mockReceipt.getLines()).thenReturn(List.of(Mockito.mock(Receipt.Line.class)));
        Mockito.when(mockStore.sell(mockCashier, "F1", 2, mockCustomer)).thenReturn(mockReceipt);

        Receipt receipt = storeService.sell(mockCashier, "F1", 2, mockCustomer);

        assertNotNull(receipt);
        assertEquals(1, receipt.getLines().size());
    }

    @Test
    void testStoreServiceAddToReceipt() throws Exception {
        Customer mockCustomer = Mockito.mock(Customer.class);
        Receipt mockReceipt = Mockito.mock(Receipt.class);
        Receipt.Line mockLine1 = Mockito.mock(Receipt.Line.class);
        Receipt.Line mockLine2 = Mockito.mock(Receipt.Line.class);
        
        Mockito.when(mockStore.addToReceipt(mockReceipt, "F1", 1, mockCustomer)).thenReturn(mockReceipt);
        Mockito.when(mockStore.addToReceipt(mockReceipt, "F2", 2, mockCustomer)).thenReturn(mockReceipt);
        Mockito.when(mockReceipt.getLines()).thenReturn(List.of(mockLine1, mockLine2));

        Receipt updatedReceipt = storeService.addToReceipt(mockReceipt, "F1", 1, mockCustomer);
        storeService.addToReceipt(updatedReceipt, "F2", 2, mockCustomer);

        assertEquals(2, updatedReceipt.getLines().size());
    }
}
