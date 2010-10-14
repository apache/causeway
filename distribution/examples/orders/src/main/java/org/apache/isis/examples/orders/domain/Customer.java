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


package org.apache.isis.examples.orders.domain;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MaxLength;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.examples.orders.services.CustomerRepository;


public class Customer extends AbstractDomainObject {

    // use ctrl+space to bring up the NO templates.
    // if you do not wish to subclass AbstractDomainObject,
    // then use the "injc - Inject Container" template.
    
    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    
    // {{ Identification Methods
    /**
     * Defines the title that will be displayed on the user
     * interface in order to identity this object.
     */
    public String title() {
        TitleBuffer t = new TitleBuffer();
        if (getFirstName() != null){
           t.append(getFirstName()).append(getLastName());
        }
        return t.toString();
    }
    // }}
    
    
    // {{ FirstName
    private String firstName;
    
    @DescribedAs("Given or christian name")
    @TypicalLength(20)
    @MaxLength(100)
    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    // }}

    // {{ LastName
    private String lastName;
    @DescribedAs("Family name or surname")
    @MaxLength(100)
    @TypicalLength(30)
    @Named("Surname")
    public String getLastName() {
        return this.lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    // }}
    
    // {{ CustomerNumber
    private Integer customerNumber;
    @Disabled(When.ONCE_PERSISTED)
    public Integer getCustomerNumber() {
        return this.customerNumber;
    }
    public void setCustomerNumber(Integer customerNumber) {
        this.customerNumber = customerNumber;
    }
    public String validateCustomerNumber(Integer customerNumber) {
        return null;
    }
    // }}
    
    // {{ Orders
    private List<Order> orders = new ArrayList<Order>();
    public List<Order> getOrders() {
        return this.orders;
    }
    @SuppressWarnings("unused")
    private void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    public void addToOrders(Order order) {
        getOrders().add(order);
    }
    public void removeFromOrders(Order order) {
        getOrders().remove(order);
    }
    // }}
    
    // {{ LastOrder
    private Order lastOrder;
    @Disabled
    public Order getLastOrder() {
        return this.lastOrder;
    }
    public void setLastOrder(Order lastOrder) {
        this.lastOrder = lastOrder;
    }
    public void modifyLastOrder(Order lastOrder) {
        setLastOrder(lastOrder);
    }
    public void clearLastOrder() {
        setLastOrder(null);
    }
    // }}

    // {{ PlaceOrder
    public void placeOrder(
            Product p, 
            @Named("Quantity")
            Integer quantity ) {
        Order order = (Order)getContainer().newTransientInstance(Order.class);
        order.modifyCustomer(this);
        order.modifyProduct(p);
        order.setOrderDate(new Date(Clock.getTime()));
        order.setQuantity(quantity);
        addToOrders(order);
        modifyLastOrder(order);
        getContainer().persist(order);
    }
    public String validatePlaceOrder( Product p, Integer quantity) {
        if (quantity < 1 || quantity > 100) {
            return "Quantity must be between 1 and 100";
        }
        return null;
    }
    public Object[] defaultPlaceOrder() {
        Product lastProductOrdered = null;
        if (getLastOrder() != null) {
            lastProductOrdered = getLastOrder().getProduct();
        }
        return new Object[] {
                lastProductOrdered, new Integer(1)
        };
    }
    public String disablePlaceOrder() {
        return !isPersistent(this)?"Save object first":null;
    }
        
    // }}
    
    // {{ Injected Services
    /*
     * This region contains references to the services (Repositories, 
     * Factories or other Services) used by this domain object.  The 
     * references are injected by the application container.
     */

    // {{ Injected: CustomerRepository
    private CustomerRepository customerRepository;
    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected CustomerRepository getCustomerRepository() {
        return this.customerRepository;
    }
    /**
     * Injected by the application container.
     */
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    // }}

    // }}
    
}
