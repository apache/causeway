package org.apache.isis.examples.webshop.objstore.dflt.orders;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.value.Date;
import org.apache.isis.examples.webshop.dom.customers.Customer;
import org.apache.isis.examples.webshop.dom.orders.Order;
import org.apache.isis.examples.webshop.dom.orders.Orders;


public class OrdersDefault extends AbstractFactoryAndRepository implements Orders {


    // {{ Id, iconName
    @Override
    public String getId() {
        return "orders";
    }

	public String iconName() {
		return "Order";
	}
	// }}

    // {{ recentOrdersFor
    @MemberOrder(sequence = "2")
    @Override
    public List<Order> recentOrdersFor(final Customer customer) {
        return allMatches(Order.class, new Filter<Order>(){

            @Override
            public boolean accept(Order order) {
                return order.getPlacedBy() == customer && order.isRecent();
            }});
    }
    // }}

    private int orderId = 0;
    @Override
    public Order createOrder(Customer customer) {
        final Order order = newTransientInstance(Order.class);
        order.setPlacedBy(customer);
        order.setPlacedOn(new Date(clock.getTimeAsDateTime()));
        order.setId(++orderId);
        persist(order);
        return order;
    }
    
    // {{ injected: Clock
    private Clock clock;

    public void setClock(final Clock clock) {
        this.clock = clock;
    }
    // }}

}
