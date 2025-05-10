package org.informatics.store;

import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.Cashier;
import org.informatics.entity.Customer;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Product;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class FinancialTest {
    @Test
    void testProfitCalculation() throws Exception {
        // Store configuration with markup 25% for non-food products
        StoreConfig cfg = new StoreConfig(0.2, 0.25, 3, 0.3);
        Store s = new Store(cfg);
        
        // Add a cashier with 1000 salary
        Cashier c = new Cashier("C", "Bob", 1000);
        s.addCashier(c);
        
        // Product variables
        String productId = "S";
        double purchasePrice = 2.0; // Purchase price for the product
        int quantity = 2; // Quantity to sell
        
        // Add product to store inventory
        Product soap = new NonFoodProduct(productId, "Soap", purchasePrice, LocalDate.MAX, 10);
        s.addProduct(soap);
        
        // Customer with sufficient funds
        Customer cust = new Customer("CU", "Ann", 100);
        
        // Sell the product
        s.sell(c, productId, quantity, cust);

        // Calculate expected values
        double expectedSalePrice = purchasePrice * (1 + cfg.nonFoodsMarkup()); // Sale price with markup
        double expectedTurnover = expectedSalePrice * quantity; // Total turnover
        double expectedCost = purchasePrice * quantity; // Cost of goods sold
        double expectedSalary = c.getMonthlySalary(); // Salary expense
        double expectedProfit = expectedTurnover - expectedSalary - expectedCost; // Expected profit
        
        // Verify turnover, profit and sold items calculations
        assertEquals(expectedTurnover, s.turnover(), 0.001);
        assertEquals(expectedProfit, s.profit(), 0.001);
        assertEquals(quantity, s.getSoldItems().get(productId));
    }
}
