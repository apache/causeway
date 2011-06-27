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


package org.apache.isis.app.cart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.isis.app.cart.services.CartRepository;
import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Money;


public class Cart extends AbstractDomainObject {
    private List<OrderItem> items = new ArrayList<OrderItem>();
    private CartRepository repository;
    private Address address;
    private PaymentMethod paymentMethod;
    private DateTime dateCreated;
    private DateTime dateModified;
    private String id;
    private Customer customer;
    
    public void created() {
        dateCreated = new DateTime();
        //dateModified = new DateTime();
    }
    
    private void updateDataModified() {
        dateModified = new DateTime();
    }
    
    public Address getAddress() {
        return address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getId() {
        resolve(id);
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
        objectChanged();
    }

    public DateTime getDateCreated() {
        return dateCreated;
    }
    
    public DateTime getDateModified() {
        return dateModified;
    }
    
    public void setDateCreated(DateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public void setDateModified(DateTime dateCreated) {
        this.dateModified = dateCreated;
        objectChanged();
    }
    
    public int getNoItems() {
        int total = 0;
        Iterator<OrderItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            total += item.getQuantity();
        }
        return total;
    }
    
    private void addToItems(OrderItem item) {
        items.add(item);
    }
    
    public List<OrderItem> getItems() {
        return items;
    }

    public Money getTotal() {
         Money total = new Money(0, "USD");
         Iterator<OrderItem> iterator = items.iterator();
         while (iterator.hasNext()) {
             OrderItem item = iterator.next();
             total = total.add(item.getTotal());
         }
         return total;
    }
    
    public boolean getCartEmpty() {
        return items.size() == 0;
    }
    
    // FIXME this will not work as addToCart!
    public void addProductToCart(Product product) {
        Iterator<OrderItem> iterator = items.iterator();
        OrderItem orderItem = null;
        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            if (item.getProduct() == product) {
                orderItem  = item;
                orderItem.setQuantity(orderItem.getQuantity() + 1);
                break;
            }
        }
        if (orderItem == null) {
            orderItem = getContainer().newInstance(OrderItem.class, this);
            orderItem.setQuantity(1);
            addToItems(orderItem);
        }
        orderItem.setProduct(product);
        orderItem.setPrice(product.getPrice());
        updateDataModified();
        objectChanged();
    }
    
    public String title() {
        return dateModified + " " + id;
    }
    
    public void emptyCart() {
        items.clear();
        informUser("Cart has been emptied");
        updateDataModified();
        objectChanged();
    }

    public String disableEmptyCart() {
        return items.size() == 0 ? "Cart is already empty" : null;
    }
    
    @Hidden
    public void resetCart() {
        address = null;
        paymentMethod = null;
        items.clear();
        objectChanged();
    }

    public Order checkOut() {
        updateDataModified();
        return repository.placeOrder(this);
    }
    
    public String disableCheckOut() {
        return items.size() == 0 ? "No items in cart" : null;
    }
    
    public void setRepository(CartRepository repository) {
        this.repository = repository;
    }
    
    public Address createAddress() {
        if (address == null) {
            address = getContainer().newTransientInstance(Address.class);
        }
        return address;
    }

    public Customer getCustomer() {
        resolve(customer);
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
        objectChanged();
    }
   
}

