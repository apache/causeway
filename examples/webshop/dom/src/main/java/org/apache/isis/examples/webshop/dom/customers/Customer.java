package org.apache.isis.examples.webshop.dom.customers;

import java.util.Comparator;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.examples.webshop.dom.orders.Order;
import org.apache.isis.examples.webshop.dom.orders.Orders;

import com.google.common.base.Objects;


public class Customer extends AbstractDomainObject {

    public static final Comparator<Customer> REF_COMPARATOR = new Comparator<Customer>() {
        @Override
        public int compare(Customer o1, Customer o2) {
            return o1.getRef().compareTo(o2.getRef());
        }
    };
    
    // {{ Ref (property, key)
    private String ref;

    @Hidden
    public String getRef() {
        return ref;
    }

    public void setRef(final String ref) {
        this.ref = ref;
    }
    // }}
    
    // {{ FirstName (property, title)
    private String firstName;

    @Title(sequence="1.0")
    @Hidden
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }
    // }}

    // {{ LastName (property, title)
    private String lastName;

    @Title(sequence="2.0")
    @Hidden
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
    // }}

    // {{ Email (property)
    private String email;

    @MemberOrder(sequence = "4.0")
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Ignore
    public boolean hasEmail(String email) {
        return Objects.equal(email, this.email);
    }
    // }}
 
    // {{ RecentOrders (collection, derived)
    @MemberOrder(sequence = "5.0")
    public List<Order> getRecentOrders() {
        return orders.recentOrdersFor(this);
    }
    // }}

    // {{ changeName (action)
    @MemberOrder(sequence = "6.0")
    public void changeName(
        @Named("First name") final String firstName, 
        @Named("Last name") final String lastName) {
        
        setFirstName(firstName);
        setLastName(lastName);
    }
    public String validateChangeName(final String firstName, final String lastName) {
        if(!onlyAlpha(firstName)) return "First name can only contain alphabetic characters";
        if(!onlyAlpha(lastName)) return "Last name can only contain alphabetic characters";
        return null;
    }
    private static boolean onlyAlpha(String str) {
        return str.matches("^[a-zA-Z]+$");
    }
    // }}


    // {{ injected: Orders
    private Orders orders;

    public void setOrders(final Orders orders) {
        this.orders = orders;
    }
    // }}



}
