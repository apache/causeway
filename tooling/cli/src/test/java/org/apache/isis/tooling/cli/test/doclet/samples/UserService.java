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
package org.apache.isis.tooling.cli.test.doclet.samples;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;

/**
 * The {@link UserService} allows the domain object to obtain the identity of the user 
 * interacting with said object.
 * <p>
 * If {@link SudoService} has been used to temporarily override the user and/or roles, 
 * then this service will report the overridden values instead.
 *
 * @since 2.0 {@index}
 */
public interface UserService {

    // -- INTERFACE

    /**
     * Optionally gets the details about the current user, 
     * based on whether an {@link ExecutionContext} can be found with the current thread's context.
     */
    Optional<UserMemento> currentUser();

    /**
     * Testimonial with arguments.
     * @param arg1 - first argument (non-null)
     * @param arg2 - second argument (non-null)
     */
    Optional<UserMemento> currentUser(@NonNull String arg1, @NonNull String arg2);
    
    /**
     * Gets the details about the current user.
     * @apiNote for backward compatibility
     */
    @Nullable
    default UserMemento getUser() {
        return currentUser().orElse(null);
    }

    // -- UTILITIES

    /**
     * Gets the details about the current user.
     * @throws IllegalStateException if no {@link ExecutionContext} can be found with the current thread's context.
     */
    default UserMemento currentUserElseFail() {
        return currentUser()
                .orElseThrow(()->_Exceptions.illegalState("Current thread has no ExecutionContext."));
    }

    /**
     * Optionally gets the current user's name, 
     * based on whether an {@link ExecutionContext} can be found with the current thread's context.
     */
    default Optional<String> currentUserName() {
        return currentUser()
                .map(UserMemento::getName);
    }

    /**
     * Gets the current user's name if available, otherwise returns {@literal Nobody}.
     * @hidden
     */
    default String currentUserNameElseNobody() {
        return currentUserName()
                .orElse("Nobody");
    }

}
