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

import java.util.concurrent.Callable;
import java.util.function.UnaryOperator;

import org.apache.isis.applib.services.iactn.ExecutionContext;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.functional.ThrowingRunnable;

import lombok.NonNull;

/**
 * Intended only for use by fixture scripts and integration tests, allows a block of code to execute
 * while the {@link UserService}'s {@link UserService#getUser() getUser()} method returns the specified user/role
 * as the effective user.
 */
// tag::refguide[]
public interface SudoService {

    // end::refguide[]
    /**
     * If included in the list of roles, then will disable security checks (can view and use all object members).
     */
    // tag::refguide[]
    RoleMemento ACCESS_ALL_ROLE =                                // <.>
            new RoleMemento(
                    SudoService.class.getName() + "#accessAll",
                    "Sudo, can view and use all object members.");
            

    // end::refguide[]
    /**
     * Executes the supplied block, with the {@link UserService} returning the specified user.
     * @param sudoMapper - maps the current {@link ExecutionContext} to the sudo one
     * @since 2.0
     */
    // tag::refguide[]
    <T> T call(                                             // <.>
            @NonNull UnaryOperator<ExecutionContext> sudoMapper,
            @NonNull Callable<T> supplier);

    // end::refguide[]
    /**
     * Executes the supplied block, with the {@link UserService} returning the specified user.
     * @param sudoMapper - maps the current {@link ExecutionContext} to the sudo one
     * @since 2.0
     */
    // tag::refguide[]
    default void run(                                        // <.>
            final @NonNull UnaryOperator<ExecutionContext> sudoMapper,
            final @NonNull ThrowingRunnable runnable) {
        call(sudoMapper, ThrowingRunnable.toCallable(runnable));
    }

    // end::refguide[]
    
    
    /**
     * Allows the {@link SudoService} to notify other services/components that the effective user has been changed.
     * @since 2.0
     */
    // tag::refguide-1[]
    interface Listener {

        void beforeCall(@NonNull ExecutionContext before, @NonNull ExecutionContext after);          // <.>

        void afterCall(@NonNull ExecutionContext before, @NonNull ExecutionContext after);          // <.>
    }
    // end::refguide-1[]

    // tag::refguide[]

}
// end::refguide[]
