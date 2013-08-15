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

package org.apache.isis.core.integtestsupport.legacy.sample.domain;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.util.TitleBuffer;

public class Order extends AbstractDomainObject {

    // use ctrl+space to bring up the NO templates.
    // if you do not wish to subclass AbstractDomainObject,
    // then use the "injc - Inject Container" template.

    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    // {{ Logger
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(Order.class);

    // }}

    // {{ Identification Methods
    /**
     * Defines the title that will be displayed on the user interface in order
     * to identity this object.
     */
    public String title() {
        final TitleBuffer t = new TitleBuffer();
        // null guard because NOF may call title while still setting
        // up the object
        final Product product = getProduct();
        if (product != null) {
            t.append(product.getCode());
        } else {
            t.append("???");
        }
        t.append("x", getQuantity());
        return t.toString();
    }

    // }}

    // {{ OrderDate
    private Date orderDate;

    @Disabled
    public Date getOrderDate() {
        return this.orderDate;
    }

    public void setOrderDate(final Date orderDate) {
        this.orderDate = orderDate;
    }

    // }}

    // {{ Quantity
    private Integer quantity;

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public String validateQuantity(final Integer quantity) {
        return quantity.intValue() <= 0 ? "Quantity must be a positive value" : null;
    }

    public String disableQuantity() {
        return isPersistent() ? "Already saved" : null;
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

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    public void modifyCustomer(final Customer customer) {
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

    public void setProduct(final Product product) {
        this.product = product;
    }

    /**
     * Capture price from product at time the order is taken.
     * 
     * @param product
     */
    public void modifyProduct(final Product product) {
        setProduct(product);
        setPrice(product.getPrice());
    }

    /**
     * Never called.
     * 
     * @param product
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

    public void setPrice(final Double price) {
        this.price = price;
    }

    // }}

    // {{ makePersistent
    /**
     * Raise visibility so can be invoked by other classes.
     */
    @Override
    public void makePersistent() {
        persist(this);
    }
    // }}

}
