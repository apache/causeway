package org.apache.isis.examples.webshop.dom.orders;


import java.util.List;

import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.examples.webshop.dom.customers.Customer;

@Named("Orders")
public interface Orders {

    public List<Order> recentOrdersFor(Customer customer);

    @Ignore // programmatic
    public Order createOrder(Customer owner);

}
