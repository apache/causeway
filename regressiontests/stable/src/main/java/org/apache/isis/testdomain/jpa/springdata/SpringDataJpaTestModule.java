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
package org.apache.isis.testdomain.jpa.springdata;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.apache.isis.applib.services.factory.FactoryService;

@Configuration
@Import({
    
})
@EnableJpaRepositories(basePackageClasses = EmployeeRepository.class)
@EntityScan(basePackageClasses = Employee.class)
public class SpringDataJpaTestModule {

    public static void setupEmployeeFixture(final EmployeeRepository repository) {
        repository.save(new Employee("Bill", "Gates"));
        repository.save(new Employee("Mark", "Zuckerberg"));
        repository.save(new Employee("Sundar", "Pichai"));
        repository.save(new Employee("Jeff", "Bezos"));
    }
    
    public static EmployeeManager createEmployeeManager(final FactoryService factoryService){
        return factoryService.viewModel(EmployeeManager.class);
    }
    
}

