package org.apache.isis.examples.webshop.dom.customers;


import java.util.List;

import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.QueryOnly;

@Named("Customers")
public interface Customers {

    public Customer newCustomer(
        @Named("First name")
        String firstName,
        @Named("Last name")
        String lastName, 
        @Named("Email")
        String email);

    @QueryOnly
    public Customer findByEmail(
        @Named("Email")
        String email);

    @QueryOnly
    List<Customer> recent();

    @Exploration
    @QueryOnly
    List<Customer> all();

}
