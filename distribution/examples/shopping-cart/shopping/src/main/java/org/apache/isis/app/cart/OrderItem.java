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

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.value.Money;


public class OrderItem extends AbstractDomainObject {
    private Product product;
    private Money price;
    private int quantity;

    public Product getProduct() {
        resolve(product);
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        objectChanged();
    }

    public int getQuantity() {
        resolve();
        return quantity;
    }

    public void setQuantity(int qantity) {
        this.quantity = qantity;
        objectChanged();
    }

    public Money getPrice() {
        resolve(price);
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
        objectChanged();
    }

    public Money getTotal() {
        if (product != null) {
            Money price = product.getPrice();
            if (price != null) {
                double amount = price.doubleValue() * quantity;
                Money total = new Money(amount, price.getCurrency());
                return total;
            }
        }
        return new Money(0.0, "USD");
    }

    public String title() {
        return (product == null ? "" : product.getTitle()) + " x " + quantity;
    }
}

