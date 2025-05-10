package org.informatics.store;
import java.time.LocalDate;

import org.informatics.config.StoreConfig;
import org.informatics.entity.Cashier;
import org.informatics.entity.NonFoodProduct;
import org.informatics.entity.Product;
import org.informatics.service.impl.CashdeskServiceImpl;
import org.informatics.service.impl.GoodsServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
public class ServiceLayerTest{
 @Test void goodsServiceAddList() throws Exception{
   Store s=new Store(new StoreConfig(0.2,0.25,3,0.3));
   GoodsServiceImpl gs=new GoodsServiceImpl(s);
   Product p=new NonFoodProduct("N","Notebook",1,LocalDate.MAX,5);
   gs.addProduct(p);
   assertEquals(1,gs.listProducts().size());
 }
 @Test void cashdeskService() {
   Store s=new Store(new StoreConfig(0.2,0.25,3,0.3));
   CashdeskServiceImpl cs=new CashdeskServiceImpl(s);
   cs.addCashier(new Cashier("C","Bob",1000));
   assertEquals(1,cs.listCashiers().size());
 }
}
