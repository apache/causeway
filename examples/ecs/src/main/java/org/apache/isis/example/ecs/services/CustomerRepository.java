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


package org.apache.isis.example.ecs.services;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.example.ecs.Customer;

import java.util.List;


public class CustomerRepository extends AbstractFactoryAndRepository {
    public static String[] namesFindByName() {
        return new String[] { "Full Name" };
    }

    public List<Customer> findByName(String name) {
        List<Customer> allMatches = allMatches(Customer.class, name);
        if (allMatches.size() == 0) {
            warnUser("No customer matching title '" + name + "'");
            return null;
        } else {
            return allMatches;
        }
    }

    public List<Customer> explorationAllCustomers() {
        return allInstances(Customer.class);
    }

    public String iconName() {
        return "Customer";
    }
    
}

