package org.informatics.store;


import java.math.BigDecimal;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FinancialTest {

    private Store store;
    private Cashier cashier;
    private Customer customer;

    @BeforeEach
    public void setUp() {
        try {
            StoreConfig config = new StoreConfig(
                    new BigDecimal("0.20"),
                    new BigDecimal("0.25"),
                    3,
                    new BigDecimal("0.30")
            );
            store = new Store(config);
            cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
            customer = new Customer("CU1", "Test Customer", new BigDecimal("200"));
            store.addCashier(cashier);

            // Create cash desk and assign cashier
            CashDesk cashDesk = new CashDesk();
            store.addCashDesk(cashDesk);

            try {
                store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
            } catch (Exception e) {
                fail("Failed to set up test environment: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            fail("Failed to set up store configuration: " + e.getMessage());
        }
    }

    @Test
    void testTurnoverCalculation() {
        try {
            // Add products to store
            store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 10));
            store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.0"), LocalDate.now().plusYears(1), 5));

            // Make sales
            store.sell(cashier, "F1", 3, customer); // 3 * 2.40 = 7.20
            store.sell(cashier, "N1", 2, customer); // 2 * 3.75 = 7.50

            // Expected turnover = 7.20 + 7.50 = 14.70
            BigDecimal expectedTurnover = new BigDecimal("14.70");
            BigDecimal actualTurnover = store.turnover();

            assertEquals(0, expectedTurnover.compareTo(actualTurnover),
                    "Turnover should be calculated correctly based on sale prices");

        } catch (ProductNotFoundException | ProductExpiredException | InsufficientQuantityException
                | InsufficientBudgetException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testCostOfSoldGoodsCalculation() {
        try {
            // Add products to store
            store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 10));
            store.addProduct(new NonFoodProduct("N1", "Soap", new BigDecimal("3.0"), LocalDate.now().plusYears(1), 5));

            // Make sales
            store.sell(cashier, "F1", 3, customer); // 3 * 2.0 = 6.0 (purchase price)
            store.sell(cashier, "N1", 1, customer); // 1 * 3.0 = 3.0 (purchase price)

            // Expected cost of sold goods = 6.0 + 3.0 = 9.0
            BigDecimal expectedCost = new BigDecimal("9.0");
            BigDecimal actualCost = store.costOfSoldGoods();

            assertEquals(0, expectedCost.compareTo(actualCost),
                    "Cost of sold goods should be calculated based on purchase prices");

        } catch (ProductNotFoundException | ProductExpiredException | InsufficientQuantityException
                | InsufficientBudgetException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testSalaryExpensesCalculation() {
        // Add more cashiers
        store.addCashier(new Cashier("C2", "Second Cashier", new BigDecimal("1200")));
        store.addCashier(new Cashier("C3", "Third Cashier", new BigDecimal("800")));

        // Expected total salary = 1000 + 1200 + 800 = 3000
        BigDecimal expectedSalary = new BigDecimal("3000");
        BigDecimal actualSalary = store.salaryExpenses();

        assertEquals(0, expectedSalary.compareTo(actualSalary),
                "Salary expenses should be sum of all cashier salaries");
    }

    @Test
    void testProfitCalculation() {
        try {
            // Add products to store
            store.addProduct(new FoodProduct("F1", "Milk", new BigDecimal("2.0"), LocalDate.now().plusDays(10), 10));

            // Make a sale
            store.sell(cashier, "F1", 5, customer); // 5 * 2.40 = 12.00 turnover, 5 * 2.0 = 10.00 cost

            // Expected profit = turnover - salary - cost of goods = 12.00 - 1000 - 10.00 = -998.00
            BigDecimal expectedProfit = new BigDecimal("12.00").subtract(new BigDecimal("1000")).subtract(new BigDecimal("10.00"));
            BigDecimal actualProfit = store.profit();

            assertEquals(0, expectedProfit.compareTo(actualProfit),
                    "Profit should be turnover minus salary expenses minus cost of sold goods");

        } catch (ProductNotFoundException | ProductExpiredException | InsufficientQuantityException
                | InsufficientBudgetException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
