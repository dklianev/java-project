package org.informatics.store;

import java.io.IOException;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.CashDesk;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.FoodProduct;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Product;
import org.informatics.exception.CashDeskNotAssignedException;
import org.informatics.exception.DuplicateProductException;
import org.informatics.exception.InsufficientBudgetException;
import org.informatics.exception.InsufficientQuantityException;
import org.informatics.exception.InvalidQuantityException;
import org.informatics.exception.ProductExpiredException;
import org.informatics.exception.ProductNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FinancialTest {

    private Store store;
    private StoreConfig config;
    private Cashier cashier;
    private Customer customer;
    private CashDesk cashDesk;

    @BeforeEach
    public void setUp() {
        // Common configuration values
        config = new StoreConfig(0.2, 0.25, 3, 0.3);
        store = new Store(config);

        // Setup cashier
        cashier = new Cashier("C1", "Test Cashier", 1000);
        store.addCashier(cashier);

        // Setup cash desk and assign cashier
        cashDesk = new CashDesk();
        store.addCashDesk(cashDesk);
        try {
            store.assignCashierToDesk(cashier.getId(), cashDesk.getId());
        } catch (Exception e) {
            fail("Failed to set up test environment: " + e.getMessage());
        }

        // Setup customer
        customer = new Customer("CU1", "Test Customer", 1000);
    }

    @Test
    void testNonFoodProductProfitCalculation() {
        try {
            // Test variables
            String productId = "NF1";
            double purchasePrice = 2.0;
            int quantity = 2;

            // Add non-food product to store
            Product product = new NonFoodProduct(productId, "Soap", purchasePrice, LocalDate.MAX, 10);
            store.addProduct(product);

            // Sell the product
            store.sell(cashier, productId, quantity, customer);

            // Calculate expected values
            double expectedSalePrice = purchasePrice * (1 + config.nonFoodsMarkup());
            double expectedTurnover = expectedSalePrice * quantity;
            double expectedCost = purchasePrice * quantity;
            double expectedSalary = cashier.getMonthlySalary();
            double expectedProfit = expectedTurnover - expectedSalary - expectedCost;

            // Verify turnover and profit calculations
            assertEquals(expectedTurnover, store.turnover(), 0.001, "Turnover should match expected value");
            assertEquals(expectedProfit, store.profit(), 0.001, "Profit should match expected value");
            assertEquals(quantity, store.getSoldItems().get(productId), "Sold quantity should be tracked correctly");
        } catch (DuplicateProductException | ProductNotFoundException | ProductExpiredException
                | InvalidQuantityException | InsufficientQuantityException | InsufficientBudgetException
                | IOException | CashDeskNotAssignedException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testFoodProductProfitCalculation() {
        try {
            // Test variables
            String productId = "F1";
            double purchasePrice = 3.0;
            int quantity = 3;

            // Add food product to store
            Product product = new FoodProduct(productId, "Milk", purchasePrice, LocalDate.MAX, 10);
            store.addProduct(product);

            // Sell the product
            store.sell(cashier, productId, quantity, customer);

            // Calculate expected values
            double expectedSalePrice = purchasePrice * (1 + config.groceriesMarkup());
            double expectedTurnover = expectedSalePrice * quantity;
            double expectedCost = purchasePrice * quantity;
            double expectedSalary = cashier.getMonthlySalary();
            double expectedProfit = expectedTurnover - expectedSalary - expectedCost;

            // Verify turnover and profit calculations
            assertEquals(expectedTurnover, store.turnover(), 0.001, "Turnover should match expected value");
            assertEquals(expectedProfit, store.profit(), 0.001, "Profit should match expected value");
            assertEquals(quantity, store.getSoldItems().get(productId), "Sold quantity should be tracked correctly");
        } catch (DuplicateProductException | ProductNotFoundException | ProductExpiredException
                | InvalidQuantityException | InsufficientQuantityException | InsufficientBudgetException
                | IOException | CashDeskNotAssignedException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testNearExpiryProductDiscounting() {
        try {
            // Test variables
            String productId = "F2";
            double purchasePrice = 4.0;
            int quantity = 1;

            // Add near-expiry food product (expiry is config.daysForNearExpiryDiscount() - 1 days from now)
            LocalDate expiryDate = LocalDate.now().plusDays(config.daysForNearExpiryDiscount() - 1);
            Product product = new FoodProduct(productId, "Fresh Yogurt", purchasePrice, expiryDate, 10);
            store.addProduct(product);

            // Sell the product
            store.sell(cashier, productId, quantity, customer);

            // Calculate expected values with discount
            double basePrice = purchasePrice * (1 + config.groceriesMarkup());
            double discountedPrice = basePrice * (1 - config.discountPercentage());
            double expectedTurnover = discountedPrice * quantity;

            // Verify discounted price is applied
            assertEquals(expectedTurnover, store.turnover(), 0.001, "Turnover should reflect discounted price");
        } catch (DuplicateProductException | ProductNotFoundException | ProductExpiredException
                | InvalidQuantityException | InsufficientQuantityException | InsufficientBudgetException
                | IOException | CashDeskNotAssignedException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testMultipleProductSale() {
        try {
            // Add multiple products
            Product food = new FoodProduct("F1", "Bread", 2.0, LocalDate.MAX, 10);
            Product nonFood = new NonFoodProduct("NF1", "Soap", 3.0, LocalDate.MAX, 10);
            store.addProduct(food);
            store.addProduct(nonFood);

            // Sell multiple products
            store.sell(cashier, "F1", 2, customer);
            store.sell(cashier, "NF1", 3, customer);

            // Calculate expected turnover
            double foodPrice = 2.0 * (1 + config.groceriesMarkup()) * 2;
            double nonFoodPrice = 3.0 * (1 + config.nonFoodsMarkup()) * 3;
            double expectedTurnover = foodPrice + nonFoodPrice;

            // Calculate expected costs
            double expectedCostOfGoods = (2.0 * 2) + (3.0 * 3);
            double expectedSalary = cashier.getMonthlySalary();

            // Expected profit
            double expectedProfit = expectedTurnover - expectedSalary - expectedCostOfGoods;

            // Verify financial calculations for multiple sales
            assertEquals(expectedTurnover, store.turnover(), 0.001, "Turnover should include all sales");
            assertEquals(expectedCostOfGoods, store.costOfSoldGoods(), 0.001, "Cost of goods should be calculated correctly");
            assertEquals(expectedProfit, store.profit(), 0.001, "Profit should account for all sales and costs");
            assertEquals(2, store.getReceiptCount(), "Should have issued 2 receipts");
        } catch (DuplicateProductException | ProductNotFoundException | ProductExpiredException
                | InvalidQuantityException | InsufficientQuantityException | InsufficientBudgetException
                | IOException | CashDeskNotAssignedException e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testSalaryExpenses() {
        // Add multiple cashiers with different salaries
        store.addCashier(new Cashier("C2", "Second Cashier", 1200));
        store.addCashier(new Cashier("C3", "Third Cashier", 1500));

        // Calculate expected salary expenses
        double expectedSalaryExpenses = 1000 + 1200 + 1500;

        // Verify salary expense calculation
        assertEquals(expectedSalaryExpenses, store.salaryExpenses(), 0.001, "Salary expenses should be calculated correctly");
    }
}
