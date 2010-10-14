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

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.util.TitleBuffer;

public class Order extends AbstractDomainObject {

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
        if (getProduct() != null) {
            t.append(getProduct().getCode()).append("x", getQuantity());
        }
        return t.toString();
    }
    // }}

    // {{ OrderDate
    private Date orderDate;
    @Disabled
    public Date getOrderDate() {
        return this.orderDate;
    }
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    // }}
    
    // {{ Quantity
    private Integer quantity;
    public Integer getQuantity() {
        return this.quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public String validateQuantity(Integer quantity) {
        return quantity.intValue() <= 0 ? "Quantity must be a positive value" : null;
    }
    public String disableQuantity() {
        return isPersistent(this) ? "Already saved" : null;
    }
    public Integer defaultQuantity() {
        return new Integer(1);
    }
    // }}
    
    // {{ Customer
    private Customer customer;
    @Disabled
    public Customer getCustomer() {
        return this.customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public void modifyCustomer(Customer customer) {
        setCustomer(customer);
    }
    public void clearCustomer() {
        setCustomer(null);
    }
    // }}

    // {{ Product
    private Product product;
    @Disabled
    public Product getProduct() {
        return this.product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    /**
     * Capture price from product at time the
     * order is taken.
     */
    public void modifyProduct(Product product) {
        setProduct(product);
        setPrice(product.getPrice());
    }
    /**
     * Never called.
     */
    public void clearProduct() {
        setProduct(null);
    }
    // }}

    // {{ Price
    private Double price;
    @Disabled
    public Double getPrice() {
        return this.price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    // }}
    
}
