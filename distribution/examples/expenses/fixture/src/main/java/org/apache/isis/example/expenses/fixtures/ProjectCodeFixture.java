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


package org.apache.isis.example.expenses.fixtures;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.example.expenses.claims.ProjectCode;
import org.apache.isis.example.expenses.employee.EmployeeRepository;


public class ProjectCodeFixture extends AbstractFixture {

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

    public static ProjectCode CODE1;
    public static ProjectCode CODE2;
    public static ProjectCode CODE3;

    @Override
    public void install() {
        CODE1 = createProjectCode("001", "Marketing");
        CODE2 = createProjectCode("002", "Sales");
        CODE3 = createProjectCode("003", "Training");
        createProjectCode("004", "Consulting");
        createProjectCode("005", "Product Development");
        createProjectCode("006", "Recruitment");
        createProjectCode("007", "Overhead");
    }

    private ProjectCode createProjectCode(final String code, final String description) {
        final ProjectCode pCode = newTransientInstance(ProjectCode.class);
        pCode.setCode(code);
        pCode.setDescription(description);
        persist(pCode);
        return pCode;
    }

}
