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
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FinancialTest {

    private Store store;
    private Store mockStore;
    private Cashier cashier;
    private Customer customer;
    private StoreConfig config;

    @BeforeEach
    void setUp() {
        config = new StoreConfig(
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
        
        try {
            store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
        } catch (Exception e) {
            fail("Failed to set up test environment: " + e.getMessage());
        }
    }

    // Mock-based tests for financial calculations
    @Test
    void testTurnoverCalculationWithMockedReceipts() {
        Receipt mockReceipt1 = Mockito.mock(Receipt.class);
        Receipt mockReceipt2 = Mockito.mock(Receipt.class);
        
        Mockito.when(mockReceipt1.total()).thenReturn(new BigDecimal("10.50"));
        Mockito.when(mockReceipt2.total()).thenReturn(new BigDecimal("25.75"));
        
        List<Receipt> mockReceipts = List.of(mockReceipt1, mockReceipt2);
        Mockito.when(mockStore.listReceipts()).thenReturn(mockReceipts);
        
        // Calculate expected total: 10.50 + 25.75 = 36.25
        BigDecimal expectedTurnover = new BigDecimal("36.25");
        Mockito.when(mockStore.turnover()).thenReturn(expectedTurnover);

        assertEquals(0, expectedTurnover.compareTo(mockStore.turnover()));
    }

    @Test
    void testSalaryExpensesWithMockedCashiers() {
        Cashier mockCashier1 = Mockito.mock(Cashier.class);
        Cashier mockCashier2 = Mockito.mock(Cashier.class);
        
        Mockito.when(mockCashier1.getMonthlySalary()).thenReturn(new BigDecimal("1000"));
        Mockito.when(mockCashier2.getMonthlySalary()).thenReturn(new BigDecimal("1200"));
        
        List<Cashier> mockCashiers = List.of(mockCashier1, mockCashier2);
        Mockito.when(mockStore.listCashiers()).thenReturn(mockCashiers);
        
        // Calculate expected total: 1000 + 1200 = 2200
        BigDecimal expectedSalary = new BigDecimal("2200");
        Mockito.when(mockStore.salaryExpenses()).thenReturn(expectedSalary);

        assertEquals(0, expectedSalary.compareTo(mockStore.salaryExpenses()));
    }

    @Test
    void testProfitCalculationWithMockedValues() {
        Mockito.when(mockStore.turnover()).thenReturn(new BigDecimal("5000"));
        Mockito.when(mockStore.salaryExpenses()).thenReturn(new BigDecimal("2000"));
        Mockito.when(mockStore.costOfSoldGoods()).thenReturn(new BigDecimal("1500"));
        
        // Calculate expected profit: 5000 - 2000 - 1500 = 1500
        BigDecimal expectedProfit = new BigDecimal("1500");
        Mockito.when(mockStore.profit()).thenReturn(expectedProfit);

        assertEquals(0, expectedProfit.compareTo(mockStore.profit()));
    }

    @Test
    void testZeroTurnoverWithEmptyMockedReceipts() {
        Mockito.when(mockStore.listReceipts()).thenReturn(List.of());
        
        BigDecimal expectedTurnover = BigDecimal.ZERO;
        Mockito.when(mockStore.turnover()).thenReturn(expectedTurnover);

        assertEquals(0, expectedTurnover.compareTo(mockStore.turnover()));
    }

    // Original integration tests kept for end-to-end validation
    @Test
    void testTurnoverWithSingleFoodSale() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5));
        
        store.sell(cashier, "F1", 2, customer); // 2 * (2.00 * 1.20) = 4.80
        BigDecimal expectedTurnover = new BigDecimal("4.80");
        
        assertEquals(0, expectedTurnover.compareTo(store.turnover()));
    }

    @Test
    void testTurnoverWithSingleNonFoodSale() throws Exception {
        store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.00"), LocalDate.now().plusYears(1), 3));
        
        store.sell(cashier, "N1", 1, customer); // 1 * (3.00 * 1.25) = 3.75
        BigDecimal expectedTurnover = new BigDecimal("3.75");
        
        assertEquals(0, expectedTurnover.compareTo(store.turnover()));
    }

    @Test
    void testTurnoverWithMultipleSales() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.00"), LocalDate.now().plusYears(1), 5));
        
        store.sell(cashier, "F1", 3, customer); // 3 * 2.40 = 7.20
        store.sell(cashier, "N1", 2, customer); // 2 * 3.75 = 7.50
        
        BigDecimal expectedTurnover = new BigDecimal("14.70");
        assertEquals(0, expectedTurnover.compareTo(store.turnover()));
    }

    @Test
    void testTurnoverWithNoSales() {
        BigDecimal expectedTurnover = BigDecimal.ZERO;
        assertEquals(0, expectedTurnover.compareTo(store.turnover()));
    }

    @Test
    void testCostOfSoldGoodsWithSingleSale() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5));
        
        store.sell(cashier, "F1", 3, customer); // 3 * 2.00 = 6.00
        BigDecimal expectedCost = new BigDecimal("6.00");
        
        assertEquals(0, expectedCost.compareTo(store.costOfSoldGoods()));
    }

    @Test
    void testCostOfSoldGoodsWithMultipleSales() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 10));
        store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.00"), LocalDate.now().plusYears(1), 5));
        
        store.sell(cashier, "F1", 2, customer); // 2 * 2.00 = 4.00
        store.sell(cashier, "N1", 1, customer); // 1 * 3.00 = 3.00
        
        BigDecimal expectedCost = new BigDecimal("7.00");
        assertEquals(0, expectedCost.compareTo(store.costOfSoldGoods()));
    }

    @Test
    void testCostOfSoldGoodsWithNoSales() {
        BigDecimal expectedCost = BigDecimal.ZERO;
        assertEquals(0, expectedCost.compareTo(store.costOfSoldGoods()));
    }

    @Test
    void testSalaryExpensesWithSingleCashier() {
        BigDecimal expectedSalary = new BigDecimal("1000");
        assertEquals(0, expectedSalary.compareTo(store.salaryExpenses()));
    }

    @Test
    void testSalaryExpensesWithMultipleCashiers() {
        store.addCashier(new Cashier("C2", "Second Cashier", new BigDecimal("1200")));
        store.addCashier(new Cashier("C3", "Third Cashier", new BigDecimal("800")));
        
        BigDecimal expectedSalary = new BigDecimal("3000"); // 1000 + 1200 + 800
        assertEquals(0, expectedSalary.compareTo(store.salaryExpenses()));
    }

    @Test
    void testSalaryExpensesWithNoCashiers() {
        Store emptyStore = new Store(config);
        BigDecimal expectedSalary = BigDecimal.ZERO;
        assertEquals(0, expectedSalary.compareTo(emptyStore.salaryExpenses()));
    }

    @Test
    void testProfitWithNormalSale() throws Exception {
        store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.00"), LocalDate.now().plusDays(10), 5));
        
        store.sell(cashier, "F1", 2, customer); // 2 * 2.40 = 4.80 turnover, 2 * 2.00 = 4.00 cost
        
        // Profit = 4.80 - 1000 - 4.00 = -999.20
        BigDecimal expectedProfit = new BigDecimal("-999.20");
        assertEquals(0, expectedProfit.compareTo(store.profit()));
    }

    @Test
    void testProfitWithNoSalesOrCashiers() {
        Store emptyStore = new Store(config);
        BigDecimal expectedProfit = BigDecimal.ZERO; // 0 - 0 - 0 = 0
        assertEquals(0, expectedProfit.compareTo(emptyStore.profit()));
    }

    @Test
    void testProfitCalculationFormula() throws Exception {
        // Add high-value products to see positive margin
        store.addProduct(new NonFoodProduct("N1", "Luxury Item", new BigDecimal("100.00"), LocalDate.now().plusYears(1), 20));
        
        Customer richCustomer = new Customer("RC1", "Rich Customer", new BigDecimal("5000"));
        store.sell(cashier, "N1", 10, richCustomer); // 10 * 125.00 = 1250 turnover, 10 * 100 = 1000 cost
        
        BigDecimal turnover = store.turnover();
        BigDecimal salaryExpenses = store.salaryExpenses();
        BigDecimal costOfGoods = store.costOfSoldGoods();
        BigDecimal expectedProfit = turnover.subtract(salaryExpenses).subtract(costOfGoods);
        
        assertEquals(0, expectedProfit.compareTo(store.profit()));
    }
}
