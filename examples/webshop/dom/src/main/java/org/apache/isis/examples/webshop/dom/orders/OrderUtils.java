package org.apache.isis.examples.webshop.dom.orders;

import java.util.List;
import java.util.Set;

import org.apache.isis.examples.webshop.dom.catalog.products.Product;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class OrderUtils {
    
    private OrderUtils(){}

    public static List<Product> productsIn(List<Order> recentOrders) {
        Set<Product> products = Sets.newLinkedHashSet();
        for (Order order : recentOrders) {
            final Set<OrderLineItem> lineItems = order.getLineItems();
            for (OrderLineItem lineItem : lineItems) {
                final Product product = lineItem.getProduct();
                products.add(product);
            }
        }
        return Lists.newArrayList(products);
    }

}
