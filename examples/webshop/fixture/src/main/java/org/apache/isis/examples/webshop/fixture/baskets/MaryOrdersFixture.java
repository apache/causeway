package org.apache.isis.examples.webshop.fixture.baskets;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.webshop.dom.baskets.Basket;
import org.apache.isis.examples.webshop.dom.baskets.Baskets;
import org.apache.isis.examples.webshop.dom.catalog.products.Products;
import org.apache.isis.examples.webshop.dom.customers.Customer;
import org.apache.isis.examples.webshop.dom.customers.Customers;
import org.apache.isis.examples.webshop.dom.orders.Order;


public class MaryOrdersFixture extends AbstractFixture {

    @Override
    public void install() {

        // mary: 1 order, 1 empty basket
        final Customer mary = customers.findByEmail("mary@smith.com");
        Basket basket = baskets.basketFor(mary);
        
        addItem(basket, 5, 2);
        addItem(basket, 6, 1);
        addItem(basket, 11, 1);
        
        @SuppressWarnings("unused")
        final Order order = basket.checkout();
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
