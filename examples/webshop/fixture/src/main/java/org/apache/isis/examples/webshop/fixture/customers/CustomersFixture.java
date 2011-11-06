package org.apache.isis.examples.webshop.fixture.customers;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.webshop.dom.customers.Customers;


public class CustomersFixture extends AbstractFixture {

    @Override
    public void install() {
        customers.newCustomer("Joe", "Bloggs", "joe@bloggs.com");
        customers.newCustomer("Mary", "Smith", "mary@smith.com");
        customers.newCustomer("Bill", "Owen", "bill@owen.com");
        customers.newCustomer("Louise", "Allen", "louise@allen.com");
    }
    
    // {{ injected: Customers
    private Customers customers;

    public void setCustomers(final Customers customers) {
        this.customers = customers;
    }
    // }}


    
}
