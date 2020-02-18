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

package org.apache.isis.applib.services.sudo;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.user.UserService;

/**
 * Intended only for use by fixture scripts and integration tests, allows a block of code to execute
 * while the {@link org.apache.isis.applib.DomainObjectContainer container}'s
 * {@link UserService#getUser() getUser()} method returns the specified user/role as the effective user.
 */
// tag::refguide[]
public interface SudoService {

    /**
     * If included in the list of roles, then will disable security checks (can view and use all object members).
     */
    String ACCESS_ALL_ROLE = SudoService.class.getName() + "#accessAll";

    /**
     * Executes the supplied block, with the {@link DomainObjectContainer} returning the specified user.
     *
     * <p>
     *    The roles of this user will be the same as the currently logged-in user.
     * </p>
     */
    void sudo(String username, final Runnable runnable);

    /**
     * Executes the supplied block, with the {@link DomainObjectContainer} returning the specified user.
     *
     * <p>
     *    The roles of this user will be the same as the currently logged-in user.
     * </p>
     */
    <T> T sudo(String username, final Callable<T> callable);

    /**
     * Executes the supplied block, with the {@link DomainObjectContainer} returning the specified user with the specified roles.
     */
    void sudo(String username, List<String> roles, final Runnable runnable);

    /**
     * Executes the supplied block, with the {@link DomainObjectContainer} returning the specified user with the specified roles.
     */
    <T> T sudo(String username, List<String> roles, final Callable<T> callable);

// end::refguide[]
// tag::refguide-1[]
    /**
     * Allows the {@link SudoService} to notify other services/components that the effective user has been changed.
     */
    interface Spi {
        /**
         * Any implementation of the {@link SudoService} should call this method on all implementations of the
         * {@link Spi} service whenever {@link SudoService#sudo(String, List, Callable)} (or its overloads)
         * is called.
         *
         * <p>
         *     Modelled after Shiro security's <a href="https://shiro.apache.org/static/1.2.6/apidocs/org/apache/shiro/subject/Subject.html#runAs-org.apache.shiro.subject.PrincipalCollection-">runAs</a> support.
         * </p>
         */
        @Programmatic
        void runAs(String username, List<String> roles);

        /**
         *
         */
        void releaseRunAs();
    }
// end::refguide-1[]
// tag::refguide[]
}
// end::refguide[]
