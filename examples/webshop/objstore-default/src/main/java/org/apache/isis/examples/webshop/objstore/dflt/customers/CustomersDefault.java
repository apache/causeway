package org.apache.isis.examples.webshop.objstore.dflt.customers;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Idempotent;
import org.apache.isis.applib.annotation.QueryOnly;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.examples.webshop.dom.customers.Customer;
import org.apache.isis.examples.webshop.dom.customers.Customers;

import com.google.common.collect.Lists;


public class CustomersDefault extends AbstractFactoryAndRepository implements Customers {


    // {{ Id, iconName
    @Override
    public String getId() {
        return "customers";
    }

	public String iconName() {
		return "Customer";
	}
	// }}


    private int id = 0;
    
    @Override
    public Customer newCustomer(String firstName, String lastName, String email) {
        final Customer customer = newTransientInstance(Customer.class);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setRef(nextRef());
        persistIfNotAlready(customer);
        return customer;
    }

    @Idempotent
    @Override
    public Customer findByEmail(final String email) {
        return firstMatch(Customer.class, new Filter<Customer>(){
            @Override
            public boolean accept(Customer t) {
                return t.hasEmail(email);
            }});
    }
    public String default0FindByEmail() {
        return "joe@bloggs.com";
    }

    @QueryOnly
    @Override
    public List<Customer> all() {
        return allInstances(Customer.class);
    }

    private String nextRef() {
        return "ID-" + (++id);
    }

    @Override
    public List<Customer> recent() {
        final List<Customer> recent = Lists.newArrayList(all());
        Collections.sort(recent, Customer.REF_COMPARATOR);
        return recent;
    }



}
