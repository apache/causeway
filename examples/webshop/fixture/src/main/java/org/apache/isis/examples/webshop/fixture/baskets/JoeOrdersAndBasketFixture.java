package org.apache.isis.examples.webshop.fixture.baskets;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.webshop.dom.baskets.Basket;
import org.apache.isis.examples.webshop.dom.baskets.Baskets;
import org.apache.isis.examples.webshop.dom.catalog.products.Products;
import org.apache.isis.examples.webshop.dom.customers.Customer;
import org.apache.isis.examples.webshop.dom.customers.Customers;
import org.apache.isis.examples.webshop.dom.orders.Order;


public class JoeOrdersAndBasketFixture extends AbstractFixture {

    @Override
    public void install() {

        // 2 orders and a non-empty basket
        final Customer joe = customers.findByEmail("joe@bloggs.com");
        Basket basket = baskets.basketFor(joe);
        
        addItem(basket, 0, 2);
        addItem(basket, 1, 1);
        addItem(basket, 2, 1);
        
        @SuppressWarnings("unused")
        final Order order1 = basket.checkout();

        addItem(basket, 4, 1);
        addItem(basket, 7, 2);
        
        @SuppressWarnings("unused")
        final Order order2 = basket.checkout();

        addItem(basket, 3, 1);

    }

    private void addItem(final Basket basket, int productIndex, int num) {
        BasketsAndOrdersFixture.addItemsTo(basket, products, productIndex, num);
    }
    
    // {{ injected: Baskets
    private Baskets baskets;

    public void setBaskets(final Baskets baskets) {
        this.baskets = baskets;
    }
    // }}

    // {{ injected: Customers
    private Customers customers;

    public void setCustomers(final Customers customers) {
        this.customers = customers;
    }
    // }}

    // {{ injected: Products
    private Products products;

    public void setProducts(final Products producst) {
        this.products = producst;
    }
    // }}

    
}
