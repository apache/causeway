package org.apache.isis.examples.webshop.dom.baskets;

import org.apache.isis.applib.annotation.Idempotent;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.examples.webshop.dom.customers.Customer;


@Named("Baskets")
public interface Baskets {

    @Idempotent
    Basket basketFor(Customer customer);
}
