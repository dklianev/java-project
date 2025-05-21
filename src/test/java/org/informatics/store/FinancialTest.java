package org.informatics.store;

import java.io.IOException;
import java.math.BigDecimal;
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
        config = new StoreConfig(
            BigDecimal.valueOf(0.2), 
            BigDecimal.valueOf(0.25), 
            3, 
            BigDecimal.valueOf(0.3)
        );
        store = new Store(config);

        // Setup cashier
        cashier = new Cashier("C1", "Test Cashier", new BigDecimal("1000"));
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
        customer = new Customer("CU1", "Test Customer", new BigDecimal("1000"));
    }

    @Test
    void testNonFoodProductProfitCalculation() {
        try {
            // Test variables
            String productId = "NF1";
            BigDecimal purchasePrice = new BigDecimal("2.0");
            int quantity = 2;

            // Add non-food product to store
            Product product = new NonFoodProduct(productId, "Soap", purchasePrice, LocalDate.MAX, 10);
            store.addProduct(product);

            // Sell the product
            store.sell(cashier, productId, quantity, customer);

            // Calculate expected values
            BigDecimal expectedSalePrice = product.getPurchasePrice().multiply(
                    BigDecimal.ONE.add(config.nonFoodsMarkup()));
            BigDecimal expectedTurnover = expectedSalePrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal expectedCost = product.getPurchasePrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal expectedSalary = cashier.getMonthlySalary();
            BigDecimal expectedProfit = expectedTurnover.subtract(expectedSalary).subtract(expectedCost);

            // Verify turnover and profit calculations
            assertEquals(0, expectedTurnover.compareTo(store.turnover()), "Turnover should match expected value");
            assertEquals(0, expectedProfit.compareTo(store.profit()), "Profit should match expected value");
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
            BigDecimal purchasePrice = new BigDecimal("3.0");
            int quantity = 3;

            // Add food product to store
            Product product = new FoodProduct(productId, "Milk", purchasePrice, LocalDate.MAX, 10);
            store.addProduct(product);

            // Sell the product
            store.sell(cashier, productId, quantity, customer);

            // Calculate expected values
            BigDecimal expectedSalePrice = product.getPurchasePrice().multiply(
                    BigDecimal.ONE.add(config.groceriesMarkup()));
            BigDecimal expectedTurnover = expectedSalePrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal expectedCost = product.getPurchasePrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal expectedSalary = cashier.getMonthlySalary();
            BigDecimal expectedProfit = expectedTurnover.subtract(expectedSalary).subtract(expectedCost);

            // Verify turnover and profit calculations
            assertEquals(0, expectedTurnover.compareTo(store.turnover()), "Turnover should match expected value");
            assertEquals(0, expectedProfit.compareTo(store.profit()), "Profit should match expected value");
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
            BigDecimal purchasePrice = new BigDecimal("4.0");
            int quantity = 1;

            // Add near-expiry food product (expiry is config.daysForNearExpiryDiscount() - 1 days from now)
            LocalDate expiryDate = LocalDate.now().plusDays(config.daysForNearExpiryDiscount() - 1);
            Product product = new FoodProduct(productId, "Fresh Yogurt", purchasePrice, expiryDate, 10);
            store.addProduct(product);

            // Sell the product
            store.sell(cashier, productId, quantity, customer);

            // Calculate expected values with discount
            BigDecimal basePrice = product.getPurchasePrice().multiply(
                    BigDecimal.ONE.add(config.groceriesMarkup()));
            BigDecimal discountedPrice = basePrice.multiply(
                    BigDecimal.ONE.subtract(config.discountPercentage()));
            BigDecimal expectedTurnover = discountedPrice.multiply(BigDecimal.valueOf(quantity));

            // Verify discounted price is applied
            assertEquals(0, expectedTurnover.compareTo(store.turnover()), "Turnover should reflect discounted price");
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
            Product food = new FoodProduct("F1", "Bread", new BigDecimal("2.0"), LocalDate.MAX, 10);
            Product nonFood = new NonFoodProduct("NF1", "Soap", new BigDecimal("3.0"), LocalDate.MAX, 10);
            store.addProduct(food);
            store.addProduct(nonFood);

            // Sell multiple products
            store.sell(cashier, "F1", 2, customer);
            store.sell(cashier, "NF1", 3, customer);

            // Calculate expected turnover
            BigDecimal foodBasePrice = food.getPurchasePrice().multiply(
                    BigDecimal.ONE.add(config.groceriesMarkup()));
            BigDecimal nonFoodBasePrice = nonFood.getPurchasePrice().multiply(
                    BigDecimal.ONE.add(config.nonFoodsMarkup()));
            
            BigDecimal foodTotalPrice = foodBasePrice.multiply(BigDecimal.valueOf(2));
            BigDecimal nonFoodTotalPrice = nonFoodBasePrice.multiply(BigDecimal.valueOf(3));
            BigDecimal expectedTurnover = foodTotalPrice.add(nonFoodTotalPrice);

            // Calculate expected costs
            BigDecimal foodCost = food.getPurchasePrice().multiply(BigDecimal.valueOf(2));
            BigDecimal nonFoodCost = nonFood.getPurchasePrice().multiply(BigDecimal.valueOf(3));
            BigDecimal expectedCostOfGoods = foodCost.add(nonFoodCost);
            BigDecimal expectedSalary = cashier.getMonthlySalary();

            // Expected profit
            BigDecimal expectedProfit = expectedTurnover.subtract(expectedSalary).subtract(expectedCostOfGoods);

            // Verify financial calculations for multiple sales
            assertEquals(0, expectedTurnover.compareTo(store.turnover()), "Turnover should include all sales");
            assertEquals(0, expectedCostOfGoods.compareTo(store.costOfSoldGoods()), "Cost of goods should be calculated correctly");
            assertEquals(0, expectedProfit.compareTo(store.profit()), "Profit should account for all sales and costs");
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
        store.addCashier(new Cashier("C2", "Second Cashier", new BigDecimal("1200")));
        store.addCashier(new Cashier("C3", "Third Cashier", new BigDecimal("1500")));

        // Calculate expected salary expenses
        BigDecimal expectedSalaryExpenses = new BigDecimal("1000")
                .add(new BigDecimal("1200"))
                .add(new BigDecimal("1500"));

        // Verify salary expense calculation
        assertEquals(0, expectedSalaryExpenses.compareTo(store.salaryExpenses()), "Salary expenses should be calculated correctly");
    }
}
