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


package org.apache.isis.example.expenses.employee;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Debug;
import org.apache.isis.applib.annotation.Executed;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.example.expenses.services.UserFinder;

import java.util.List;


public abstract class EmployeeRepositoryAbstract extends AbstractFactoryAndRepository implements EmployeeRepository, UserFinder {
    public String iconName() {
        return "Employee";
    }

    @Debug
    public List<Employee> allEmployees() {
        return allInstances(Employee.class);
    }

    private Employee findEmployeeForUserName(final String userName) {
        final Employee pattern = newTransientInstance(Employee.class);
        pattern.setUserName(userName);
        return firstMatch(Employee.class, pattern);
    }

    @Hidden
    public List<Employee> findEmployeeByName(final String name) {
        return allMatches(Employee.class, name);
    }

    // {{ User Finder
    // private Employee currentUser;

    @Executed(Executed.Where.LOCALLY)
    public Object currentUserAsObject() {
        // if (currentUser == null) {
        // String userName = getUser().getName();
        // currentUser = findEmployeeForUserName(userName);
        // }
	UserMemento user = getUser();
	String userName = user.getName();
        return findEmployeeForUserName(userName);
    }

    // }}

    @Executed(Executed.Where.LOCALLY)
    @Hidden
    public Employee me() {
        return (Employee) currentUserAsObject();
    }

}
