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
package org.apache.causeway.applib.services.user;

import java.util.List;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Allows the domain object to obtain the identity of the user interacting with
 * said object.
 *
 * <p>
 * If {@link SudoService} has been used to temporarily override the user and/or
 * roles, then this service will report the overridden values instead. This is
 * within the context of a thread.
 * </p>
 *
 * <p>
 * In addition, if impersonation has been invoked through the
 * {@link ImpersonateMenu}, then this service will report the impersonated user,
 * with the companion {@link ImpersonatedUserHolder} taking responsibilty for
 * remembering the impersonated user over multiple (http) requests, eg using an
 * http session. It's important to note that under these circumstances the user
 * reported by this service (the &quot;effective&quot; user) will <i>not</i> be
 * the same as the user held in the {@link InteractionContext}, as obtained by
 * {@link InteractionLayerTracker#currentInteractionContext()
 * InteractionLayerTracker} (the &quot;real&quot; user).
 * </p>
 *
 * @see org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker
 * @see org.apache.causeway.applib.services.iactnlayer.InteractionContext
 * @see SudoService
 * @see ImpersonateMenu
 * @see ImpersonatedUserHolder
 *
 * @since 1.x revised in 2.0 {@index}
 */
@Service
@Named(UserService.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class UserService {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".UserService";

    /**
     * Default returned from {@link #currentUserNameElseNobody()}.
     */
    public static final String NOBODY = "__causeway_nobody";

    private final Provider<InteractionLayerTracker> iInteractionLayerTrackerProvider;
    private final List<ImpersonatedUserHolder> impersonatedUserHolders;

    /**
     * Returns the details about the current user, either the &quot;effective&quot;
     * user (if being {@link #impersonateUser(String, List, String) impersonated})
     * otherwise the &quot;real&quot; user (as obtained from the
     * {@link InteractionContext} of the current thread).
     */
    public Optional<UserMemento> currentUser() {
        val impersonatedUserIfAny = impersonatedUserIfAny();
        return impersonatedUserIfAny.isPresent()
                ? impersonatedUserIfAny
                : iInteractionLayerTrackerProvider.get()
                    .currentInteractionContext()
                    .map(InteractionContext::getUser);
    }

    /**
     * Whether the current user is the <i>system user</i> (as obtained from the
     * {@link InteractionContext} of the current thread).
     */
    public boolean isCurrentUserWithSudoAccessAllRole() {
        return currentUser()
                .map(UserMemento::hasSudoAccessAllRole)
                .orElse(false);
    }

    /**
     * Gets the details about the {@link #currentUser()} current user, if any (and
     * returning <code>null</code> if there is none).
     */
    @Nullable
    public UserMemento getUser() {
        return currentUser().orElse(null);
    }

    /**
     * Gets the details about the {@link #currentUser()} current user, throwing an
     * exception if there is none.
     *
     * @throws IllegalStateException if no {@link InteractionContext} can be found
     *                               with the current thread's context.
     */
    public UserMemento currentUserElseFail() {
        return currentUser()
                .orElseThrow(() -> _Exceptions.illegalState("Current thread has no InteractionContext."));
    }

    /**
     * Optionally gets the {@link #currentUser() current user}'s name, obtained from
     * {@link UserMemento}.
     */
    public Optional<String> currentUserName() {
        return currentUser().map(UserMemento::getName);
    }

    /**
     * Returns either the current user's name or else {@link #NOBODY}.
     */
    public String currentUserNameElseNobody() {
        return currentUserName().orElse(NOBODY);
    }

    /**
     * Whether or not the user currently reported (in {@link #currentUser()} and
     * similar) is actually an impersonated user.
     *
     * @see #currentUser()
     * @see #supportsImpersonation()
     * @see #impersonateUser(String, List, String)
     * @see #stopImpersonating()
     */
    public boolean isImpersonating() {
        return impersonatedUserIfAny().isPresent();
    }

    /**
     * Whether impersonation is available for this request.
     *
     * <p>
     * The typical implementation uses an HTTP session, which is not guaranteed to
     * be available for all viewers. Specifically, the Wicket viewer <i>does</i> use
     * HTTP sessions and therefore supports impersonation, but the RestfulObjects
     * viewer does <i>not</i>. This means that the result of this call varies on a
     * request-by-request basis.
     * </p>
     *
     * @see #impersonateUser(String, List, String)
     * @see #isImpersonating()
     * @see #stopImpersonating()
     *
     * @return whether impersonation is supported in the context of this (http)
     *         request.
     */
    public boolean supportsImpersonation() {
        return impersonatingHolder().isPresent();
    }

    private Optional<ImpersonatedUserHolder> impersonatingHolder() {
        for (ImpersonatedUserHolder impersonatedUserHolder : impersonatedUserHolders) {
            try {
                if (impersonatedUserHolder.supportsImpersonation()) {
                    return Optional.of(impersonatedUserHolder);
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
        return Optional.empty();
    }

    /**
     * Allows implementations to override the current user with another user.
     *
     * <p>
     * If this service (for this request) does not {@link #supportsImpersonation()
     * support impersonation}, then the request is just ignored.
     * </p>
     *
     * <p>
     * IMPORTANT: this is intended for non-production environments only, where it
     * can be invaluable (from a support perspective) to be able to quickly use the
     * application &quot;as if&quot; logged in as another user.
     * </p>
     *
     * @see #supportsImpersonation()
     * @see #isImpersonating()
     * @see #stopImpersonating()
     * @param userName          - the name of the user to be impersonated
     * @param roles             - the collection of roles for the impersonated user
     *                          to have.
     * @param multiTenancyToken
     */
    public void impersonateUser(
            final String userName,
            final List<String> roles,
            final String multiTenancyToken) {
        impersonatingHolder()
        .ifPresent(x -> x.setUserMemento(UserMemento.ofNameAndRoleNames(userName, roles)
                .withImpersonating(true).withMultiTenancyToken(multiTenancyToken)));
    }

    /**
     * For implementations that support impersonation, this is to programmatically
     * stop impersonating a user
     *
     * <p>
     * If this service (for this request) does not {@link #supportsImpersonation()
     * support impersonation}, then the request is just ignored.
     * </p>
     *
     * <p>
     * Intended to be called at some point after
     * {@link #impersonateUser(String, List, String)} would have been called.
     * </p>
     *
     * @see #supportsImpersonation()
     * @see #impersonateUser(String, List, String)
     * @see #isImpersonating()
     */
    public void stopImpersonating() {
        impersonatingHolder().ifPresent(ImpersonatedUserHolder::clearUserMemento);
    }

    private Optional<UserMemento> impersonatedUserIfAny() {
        return impersonatingHolder().flatMap(ImpersonatedUserHolder::getUserMemento);
    }

}
