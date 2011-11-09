package org.apache.isis.examples.webshop.dom.orders;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.value.Date;
import org.apache.isis.examples.webshop.dom.customers.Customer;

public class Order extends AbstractDomainObject {

    // {{ Id (property, title)
    private int orderId;

    @Title(sequence="1", append=":")
    @Disabled
    @MemberOrder(sequence = "1")
    public int getId() {
        return orderId;
    }

    public void setId(final int orderId) {
        this.orderId = orderId;
    }
    // }}

    // {{ PlacedBy (property, title)
    private Customer placedBy;

    @Title(sequence="2")
    @Disabled
    @MemberOrder(sequence = "2")
    public Customer getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(final Customer customer) {
        this.placedBy = customer;
    }
    // }}

    // {{ PlacedOn (property)
    private Date date;

    @Disabled
    @MemberOrder(sequence = "3")
    public Date getPlacedOn() {
        return date;
    }

    public void setPlacedOn(final Date date) {
        this.date = date;
    }
    // }}

    // {{ LineItems (collection)
    private Set<OrderLineItem> lineItems = new LinkedHashSet<OrderLineItem>();

    @Disabled
    @MemberOrder(sequence = "4")
    public Set<OrderLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(final Set<OrderLineItem> lineItems) {
        this.lineItems = lineItems;
    }
    // }}
    
    // {{ helpers
    @Ignore // programmatic
    public boolean isRecent() {
        return true;
    }
    // }}

}
