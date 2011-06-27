/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.app.cart.services;

import java.util.Iterator;
import java.util.List;

import org.apache.isis.app.cart.Cart;
import org.apache.isis.app.cart.Order;
import org.apache.isis.app.cart.OrderItem;
import org.apache.isis.applib.AbstractFactoryAndRepository;


public class CartRepository extends AbstractFactoryAndRepository {
    @Override
    public String getId() {
        return "carts";
    }

    public List<Cart> allCarts() {
        return allInstances(Cart.class);
    }

    public List<Order> allOrders() {
        return allInstances(Order.class);
    }

    public Cart newCart() {
        return newPersistentInstance(Cart.class);
    }

    public Order placeOrder(Cart cart) {
        Order order = newTransientInstance(Order.class);
        order.setDeliveryAddress(cart.getAddress());
        order.setPaymentMethod(cart.getPaymentMethod());
        Iterator<OrderItem> iterator = cart.getItems().iterator();
        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            order.addToItems(item);
        }
        persist(order);
        cart.resetCart();
        return order;
    }
    
    public void clearEmptyCarts() {
    }

    /*
    public Cart findCart(final String id) {
        Cart cart;
        Filter filter = new Filter() {
            public boolean accept(Object pojo) {
                String id2 = ((Cart) pojo).getId();
                return id2 != null && id2.equals(id);
            }
        };
        cart = firstMatch(Cart.class, filter, false);
        if (cart == null) {
            cart = newPersistentInstance(Cart.class);
            cart.setId(id);
        } else {
            cart.updateLastUsed();
        }
        return cart;
    }
    */
}
