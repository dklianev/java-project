package org.informatics.store;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Receipt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FinancialTest {

    private Store store;
    private Store mockStore;
    private Cashier cashier;
    private Customer customer;

    @BeforeEach
    void setUp() throws Exception {
        StoreConfig config = new StoreConfig(
                new BigDecimal("0.20"), // 20% food markup
                new BigDecimal("0.25"), // 25% non-food markup
                3, // near expiry days
                new BigDecimal("0.30") // near expiry discount
        );
        store = new Store(config);
        mockStore = Mockito.mock(Store.class);
        cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
        customer = new Customer("CU1", "Test Customer", new BigDecimal("500"));
        
        store.addCashier(cashier);
        CashDesk cashDesk = new CashDesk();
        store.addCashDesk(cashDesk);
        store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
    }

    // === MOCK TESTS ===
    @Test
    void testTurnoverCalculationWithMockedReceipts() {
        // Arrange
        Receipt mockReceipt1 = Mockito.mock(Receipt.class);
        Receipt mockReceipt2 = Mockito.mock(Receipt.class);
        
        Mockito.when(mockReceipt1.total()).thenReturn(new BigDecimal("10.50"));
        Mockito.when(mockReceipt2.total()).thenReturn(new BigDecimal("25.75"));
        
        List<Receipt> mockReceipts = List.of(mockReceipt1, mockReceipt2);
        Mockito.when(mockStore.listReceipts()).thenReturn(mockReceipts);
        
        BigDecimal expectedTurnover = new BigDecimal("36.25");
        Mockito.when(mockStore.turnover()).thenReturn(expectedTurnover);

        // Act & Assert
        assertEquals(0, expectedTurnover.compareTo(mockStore.turnover()));
    }

    @Test
    void testSalaryExpensesWithMockedCashiers() {
        // Arrange
        Cashier mockCashier1 = Mockito.mock(Cashier.class);
        Cashier mockCashier2 = Mockito.mock(Cashier.class);
        
        Mockito.when(mockCashier1.getMonthlySalary()).thenReturn(new BigDecimal("1000"));
        Mockito.when(mockCashier2.getMonthlySalary()).thenReturn(new BigDecimal("1200"));
        
        List<Cashier> mockCashiers = List.of(mockCashier1, mockCashier2);
        Mockito.when(mockStore.listCashiers()).thenReturn(mockCashiers);
        
        BigDecimal expectedSalary = new BigDecimal("2200");
        Mockito.when(mockStore.salaryExpenses()).thenReturn(expectedSalary);

        // Act & Assert
        assertEquals(0, expectedSalary.compareTo(mockStore.salaryExpenses()));
    }

    @Test
    void testProfitCalculationWithMockedValues() {
        // Arrange
        Mockito.when(mockStore.turnover()).thenReturn(new BigDecimal("5000"));
        Mockito.when(mockStore.salaryExpenses()).thenReturn(new BigDecimal("2000"));
        Mockito.when(mockStore.costOfSoldGoods()).thenReturn(new BigDecimal("1500"));
        
        BigDecimal expectedProfit = new BigDecimal("1500");
        Mockito.when(mockStore.profit()).thenReturn(expectedProfit);

        // Act & Assert
        assertEquals(0, expectedProfit.compareTo(mockStore.profit()));
    }

    // === INTEGRATION TESTS ===
    @Test
    void testTurnoverWithSingleFoodSale() throws Exception {
        // Arrange
        store.addProduct(new FoodProduct("F1", "Organic Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5));
        
        // Act
        store.sell(cashier, "F1", 2, customer); // 2 * (2.00 * 1.20) = 4.80
        BigDecimal expectedTurnover = new BigDecimal("4.80");
        
        // Assert
        assertEquals(0, expectedTurnover.compareTo(store.turnover()));
    }

    @Test
    void testTurnoverWithSingleNonFoodSale() throws Exception {
        // Arrange
        store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.00"), LocalDate.now().plusYears(1), 3));
        
        // Act
        store.sell(cashier, "N1", 1, customer); // 1 * (3.00 * 1.25) = 3.75
        BigDecimal expectedTurnover = new BigDecimal("3.75");
        
        // Assert
        assertEquals(0, expectedTurnover.compareTo(store.turnover()));
    }

    @Test
    void testSalaryExpensesWithSingleCashier() {
        // Arrange
        BigDecimal expectedSalary = new BigDecimal("1000");
        
        // Act & Assert
        assertEquals(0, expectedSalary.compareTo(store.salaryExpenses()));
    }

    @Test
    void testCostOfSoldGoodsWithSingleSale() throws Exception {
        // Arrange
        store.addProduct(new FoodProduct("F1", "Organic Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5));
        
        // Act
        store.sell(cashier, "F1", 3, customer); // 3 * 2.00 = 6.00
        BigDecimal expectedCost = new BigDecimal("6.00");
        
        // Assert
        assertEquals(0, expectedCost.compareTo(store.costOfSoldGoods()));
    }

    @Test
    void testProfitCalculationFormula() throws Exception {
        // Arrange
        store.addProduct(new NonFoodProduct("N1", "Luxury Item", new BigDecimal("100.00"), LocalDate.now().plusYears(1), 20));
        Customer richCustomer = new Customer("RC1", "Rich Customer", new BigDecimal("5000"));
        
        // Act
        store.sell(cashier, "N1", 10, richCustomer); // 10 * 125.00 = 1250 turnover, 10 * 100 = 1000 cost
        
        BigDecimal turnover = store.turnover();
        BigDecimal salaryExpenses = store.salaryExpenses();
        BigDecimal costOfGoods = store.costOfSoldGoods();
        BigDecimal expectedProfit = turnover.subtract(salaryExpenses).subtract(costOfGoods);
        
        // Assert
        assertEquals(0, expectedProfit.compareTo(store.profit()));
    }
}
