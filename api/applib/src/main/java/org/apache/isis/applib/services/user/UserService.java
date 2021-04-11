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
package org.apache.isis.applib.services.user;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.iactn.ExecutionContext;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * Allows the domain object to obtain the identity of the user interacting with
 * said object.
 *
 * <p>
 * If {@link SudoService} has been used to temporarily override the user and/or
 * roles, then this service will report the overridden values instead.
 * </p>
 *
 * @since 1.x revised in 2.0 {@index}
 */
public interface UserService {

    // -- INTERFACE

    /**
     * Optionally gets the details about the current user,
     * based on whether an {@link ExecutionContext} can be found with the current thread's context.
     */
    Optional<UserMemento> currentUser();

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
     * Optionally gets the the current user's name,
     * based on whether an {@link ExecutionContext} can be found with the current thread's context.
     */
    default Optional<String> currentUserName() {
        return currentUser()
                .map(UserMemento::getName);
    }

    /**
     * Returns either the current user's name or else {@literal Nobody}.
     */
    default String currentUserNameElseNobody() {
        return currentUserName()
                .orElse("Nobody");
    }

    /**
     * Allows implementations to override the current user with another user.
     *
     * <p>
     *     This is intended for non-production environments only, where it can
     *     be invaluable (from a support perspective) to be able to quickly
     *     use the application &quot;as if&quot; logged in as another user.
     * </p>
     *
     * @see #supportsImpersonation()
     * @see #getImpersonatedUser()
     * @see #isImpersonating()
     * @see #stopImpersonating()
     *
     * @param userName
     * @param roles
     */
    default void impersonateUser(final String userName, final List<String> roles) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * For implementations that support impersonation, this is to
     * programmatically stop impersonating a user
     *
     * <p>
     *     Intended to be called at some point after
     *     {@link #impersonateUser(String, List)} would have been called.
     * </p>
     *
     * @see #supportsImpersonation()
     * @see #impersonateUser(String, List)
     * @see #getImpersonatedUser()
     * @see #isImpersonating()
     */
    default void stopImpersonating() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Whether this implementation supports impersonation.
     *
     * @see #impersonateUser(String, List)
     * @see #getImpersonatedUser()
     * @see #isImpersonating()
     * @see #stopImpersonating()
     */
    default boolean supportsImpersonation() {
        return false;
    }

    /**
     * The impersonated user, if it has previously been set.
     *
     * @see #supportsImpersonation()
     * @see #impersonateUser(String, List)
     * @see #isImpersonating()
     * @see #stopImpersonating()
     */
    default Optional<UserMemento> getImpersonatedUser() {
        return Optional.empty();
    }

    /**
     * Whether or not the user currently reported (in {@link #currentUser()}
     * and similar) is actually an impersonated user.
     *
     * @see #currentUser()
     * @see #supportsImpersonation()
     * @see #impersonateUser(String, List)
     * @see #getImpersonatedUser()
     * @see #stopImpersonating()
     */
    default boolean isImpersonating() {
        return getImpersonatedUser().isPresent();
    }

}
