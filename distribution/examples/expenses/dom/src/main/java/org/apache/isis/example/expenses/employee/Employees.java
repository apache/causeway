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

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Executed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;

import java.util.List;


/**
 * Defines the user actions available from the 'Employees' desktop icon or tab.
 * 
 * @author Richard
 * 
 */
@Named("Employees")
public class Employees extends AbstractService {

    private static final int MAX_NUM_EMPLOYEES = 10;

    // {{ Title & ID
    @Override
    public String getId() {
        return "Employees";
    }

    // }}
    public String iconName() {
        return Employee.class.getSimpleName();
    }

    // {{ Injected Services
    /*
     * This region contains references to the services (Repositories, Factories or other Services) used by
     * this domain object. The references are injected by the application container.
     */

    // {{ Injected: EmployeeRepository
    private EmployeeRepository employeeRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected EmployeeRepository getEmployeeRepository() {
        return this.employeeRepository;
    }

    /**
     * Injected by the application container.
     */
    public void setEmployeeRepository(final EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // }}

    // }}

    @MemberOrder(sequence = "2")
    public List<Employee> findEmployeeByName(@Named("Name or start of Name")
    final String name) {
        final List<Employee> results = employeeRepository.findEmployeeByName(name);
        if (results.isEmpty()) {
            warnUser("No employees found matching name: " + name);
            return null;
        } else if (results.size() > MAX_NUM_EMPLOYEES) {
            warnUser("Too many employees found matching name: " + name + "\n Please refine search.");
            return null;
        }
        return results;
    }

    @Executed(Executed.Where.LOCALLY)
    public Employee me() {
        final Employee me = employeeRepository.me();
        if (me == null) {
            warnUser("No Employee representing current user");
        }
        return me;
    }

}
