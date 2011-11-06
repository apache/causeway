package org.apache.isis.examples.webshop.dom.baskets;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.examples.webshop.dom.catalog.products.Product;
import org.apache.isis.examples.webshop.dom.customers.Customer;
import org.apache.isis.examples.webshop.dom.orders.Order;
import org.apache.isis.examples.webshop.dom.orders.OrderLineItem;
import org.apache.isis.examples.webshop.dom.orders.OrderUtils;
import org.apache.isis.examples.webshop.dom.orders.Orders;

public class Basket extends AbstractDomainObject {

    // {{ Owner (property, title)
    private Customer owner;

    @Title
    @MemberOrder(sequence = "1")
    public Customer getOwner() {
        return owner;
    }

    public void setOwner(final Customer owner) {
        this.owner = owner;
    }
    // }}

    // {{ LineItems (collection)
    private Set<BasketLineItem> lineItems = new LinkedHashSet<BasketLineItem>();

    @Disabled
    @MemberOrder(sequence = "2")
    public Set<BasketLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(final Set<BasketLineItem> lineItems) {
        this.lineItems = lineItems;
    }
    // }}

    // {{ add (action)
    @MemberOrder(sequence = "1")
    public BasketLineItem add(final Product product) {
        final BasketLineItem lineItem = newTransientInstance(BasketLineItem.class);
        lineItem.setBasket(this);
        getLineItems().add(lineItem);
        lineItem.setProduct(product);
        lineItem.setQuantity(1);
        persist(lineItem);
        return lineItem;
    }
    public List<Product> choices0Add() {
        return OrderUtils.productsIn(getOwner().getRecentOrders());
    }
    // }}

    // {{ checkout (action)
    @MemberOrder(sequence = "1")
    public Order checkout() {
        final Order order = orders.createOrder(getOwner());
        for (BasketLineItem bli : getLineItems()) {
            OrderLineItem oli = newTransientInstance(OrderLineItem.class);
            oli.setOrder(order);
            order.getLineItems().add(oli);
            oli.setProduct(bli.getProduct());
            oli.setQuantity(bli.getQuantity());
            oli.setPrice(bli.getProduct().getPrice()); // fixed
            persist(oli);
        }
        // remove line items
        for (BasketLineItem bli : getLineItems()) {
            bli.setBasket(null);
        }
        getLineItems().clear();
        return order;
    }
    // }}

    // {{ injected: Orders
    private Orders orders;

    public void setOrders(final Orders orders) {
        this.orders = orders;
    }
    // }}


    
}
