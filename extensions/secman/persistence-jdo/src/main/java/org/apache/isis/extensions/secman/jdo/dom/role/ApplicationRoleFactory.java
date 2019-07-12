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
package org.apache.isis.extensions.secman.jdo.dom.role;

import javax.inject.Inject;

import org.apache.isis.applib.services.factory.FactoryService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Optional hook so that alternative implementations of {@link org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole}.
 *
 * <p>
 *     To use, simply implement the interface and annotate that implementation with {@link org.apache.isis.applib.annotation.DomainService},
 *     for example:
 * </p>
 * <pre>
 *     @DomainService
 *     public class MyApplicationRoleFactory implements ApplicationRoleFactory {
 *         public ApplicationRole newApplicationRole() {
 *             return container.newTransientInstance(MyApplicationRole.class);
 *         }
 *
 *         @Inject
 *         RepositoryService repository;
 *     }
 * </pre>
 * <p>
 *     where:
 * </p>
 * <pre>
 *     public class MyApplicationRole extends ApplicationRole { ... }
 * </pre>
 */
public interface ApplicationRoleFactory {

    public ApplicationRole newApplicationRole();

    @Service @Order(Ordered.LOWEST_PRECEDENCE)
    public static class Default implements ApplicationRoleFactory {

        public ApplicationRole newApplicationRole() {
            return factory.instantiate(ApplicationRole.class);
        }

        @Inject FactoryService factory;

    }

}
