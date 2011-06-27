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
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.value.Password;


public class Customer extends AbstractDomainObject {

    private String name;
    private String userName;
    private Password password;
    private String email;
    private Address defaultAddress;
    private List<Address> addresses = new ArrayList<Address>();
    private PaymentMethod defaultPaymentMethod;
    private List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();

    public String title() {
        return getName();
    }

    public String getName() {
        resolve(name);
        return name;
    }

    public void setName(String name) {
        this.name = name;
        objectChanged();
    }

    public String getUserName() {
        resolve(userName);
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        objectChanged();
    }

    public Password getPassword() {
        resolve(password);
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
        objectChanged();
    }

    @Disabled(When.ONCE_PERSISTED)
    @TypicalLength(50)
    public String getEmail() {
        resolve(email);
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        objectChanged();
    }

    public Address getDefaultAddress() {
        resolve(defaultAddress);
        return defaultAddress;
    }

    public void setDefaultAddress(Address defaultAddress) {
        this.defaultAddress = defaultAddress;
        objectChanged();
    }

    public List<Address> getAddresses() {
        resolve(addresses);
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        addresses = addresses;
        objectChanged();
    }

    public PaymentMethod getDefaultPaymentMethod() {
        resolve(defaultPaymentMethod);
        return defaultPaymentMethod;
    }

    public void setDefaultPaymentMethod(PaymentMethod defaultPaymentMethod) {
        this.defaultPaymentMethod = defaultPaymentMethod;
        objectChanged();
    }

    public List<PaymentMethod> getPaymentMethods() {
        resolve(paymentMethods);
        return paymentMethods;
    }

    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
        objectChanged();
    }
    

}

