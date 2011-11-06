package org.apache.isis.examples.webshop.fixture.baskets;

import java.util.List;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.webshop.dom.baskets.Basket;
import org.apache.isis.examples.webshop.dom.baskets.BasketLineItem;
import org.apache.isis.examples.webshop.dom.catalog.products.Product;
import org.apache.isis.examples.webshop.dom.catalog.products.Products;


public class BasketsAndOrdersFixture extends AbstractFixture {

    public BasketsAndOrdersFixture() {
        addFixture(new JoeOrdersAndBasketFixture());
        addFixture(new MaryOrdersFixture());
        addFixture(new BillBasketFixture());
    }

    public static void addItemsTo(final Basket basket, final Products products, int index, int count) {
        final List<Product> productList = products.all();
        BasketLineItem lineItem = basket.add(productList.get(index));
    
        for(int i=0; i<count-1; i++) {
            lineItem.increment();
        }
    }
    
}
