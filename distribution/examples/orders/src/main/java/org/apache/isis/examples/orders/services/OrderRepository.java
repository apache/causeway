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


package org.apache.isis.examples.orders.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.examples.orders.domain.Customer;
import org.apache.isis.examples.orders.domain.Order;


@Named("Orders")
public class OrderRepository extends AbstractFactoryAndRepository {

    // use ctrl+space to bring up the NO templates.
    
    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    
    // {{ findRecentOrders
    public List<Order> findRecentOrders(Customer customer, @Named("Number of Orders") Integer numberOfOrders) {
        List<Order> orders = customer.getOrders();
        Collections.sort(orders, new Comparator<Order>() {
            public int compare(Order o1, Order o2) {
                long time1 = o1.getOrderDate().getTime();
                long time2 = o2.getOrderDate().getTime();
                return (int) (time2 - time1);
            }
        });
        if (orders.size() < numberOfOrders) {
            return orders;
        } else {
            return orders.subList(0, numberOfOrders);
        }
    }
  public Object[] defaultFindRecentOrders(Customer customer, Integer numberOfOrders) {
        return new Object[] { null, new Integer(3) };
    }

    // }}

    // {{ identification
    /**
     * Use <tt>Order.gif</tt> for icon.
     */
    public String iconName() {
        return "Order";
    }
    // }}
    
    

}
