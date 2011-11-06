package org.apache.isis.examples.webshop.fixture.baskets;


import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.webshop.dom.baskets.Basket;
import org.apache.isis.examples.webshop.dom.baskets.Baskets;
import org.apache.isis.examples.webshop.dom.catalog.products.Products;
import org.apache.isis.examples.webshop.dom.customers.Customer;
import org.apache.isis.examples.webshop.dom.customers.Customers;


public class BillBasketFixture extends AbstractFixture {

    @Override
    public void install() {

        // bill: 1 non-empty basket, no orders
        final Customer bill = customers.findByEmail("bill@owen.com");
        Basket basket = baskets.basketFor(bill);
        
        addItem(basket, 4, 1);
        addItem(basket, 7, 2);
        addItem(basket, 9, 2);
        addItem(basket, 10, 1);
        addItem(basket, 11, 1);

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
