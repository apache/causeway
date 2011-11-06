package org.apache.isis.examples.webshop.objstore.dflt.baskets;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.examples.webshop.dom.baskets.Basket;
import org.apache.isis.examples.webshop.dom.baskets.Baskets;
import org.apache.isis.examples.webshop.dom.customers.Customer;
import org.apache.isis.examples.webshop.dom.customers.Customers;


public class BasketsDefault extends AbstractFactoryAndRepository implements Baskets {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "baskets";
    }

    public String iconName() {
        return "Basket";
    }
    // }}

    // {{ basketFor (action)
    @MemberOrder(sequence="1.0")
    @Override
    public Basket basketFor(final Customer customer) {
        Basket basket = firstMatch(Basket.class, new Filter<Basket>(){
            @Override
            public boolean accept(Basket basket) {
                return basket.getOwner() == customer;
            }
        });
        if(basket == null) {
            basket = newTransientInstance(Basket.class);
            basket.setOwner(customer);
            persistIfNotAlready(basket);
        }
        return basket;
    }
    public List<Customer> choices0BasketFor() {
        return customers.recent();
    }
    // }}
    
    // {{ injected: Customers
    private Customers customers;
    public void setCustomers(final Customers customers) {
        this.customers = customers;
    }
    // }}

}
