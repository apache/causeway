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
import java.util.function.UnaryOperator;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.functional.ThrowingRunnable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Allows a block of code to be executed within an arbitrary
 * {@link InteractionContext}, allowing the who, when and where to be temporarily
 * switched.
 *
 * <p>
 * Most typically this service is used to temporarily change the
 * &quot;who&quot;, that is the user reported by the {@link UserService}'s
 * {@link UserService#currentUser() getUser()} - hence the name SudoService.
 * But the user's locale and timezome can also be changed, as well as the time
 * reported by {@link org.apache.isis.applib.services.clock.ClockService}.
 * </p>
 *
 * <p>
 * The primary use case for this service is for fixture scripts and
 * integration tests.
 * </p>
 *
 * @see InteractionService
 * @see InteractionLayerTracker
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Service
@Named("isis.applib.SudoService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SudoService {

    /**
     * If included in the list of roles, then will disable security checks (can view and use all object members).
     */
    public static RoleMemento ACCESS_ALL_ROLE =
            new RoleMemento(
                    SudoService.class.getName() + "#accessAll",
                    "Sudo, can view and use all object members.");


    private final InteractionService interactionService;
    private final InteractionLayerTracker interactionLayerTracker;
    private final List<SudoServiceListener> sudoListeners;

    /**
     * Executes the supplied {@link Callable} block, within the provided
     * {@link InteractionContext}.
     *
     * @param sudoMapper - maps the current {@link InteractionContext} to the sudo one
     * @since 2.0
     */
    public <T> T call(
            final @NonNull UnaryOperator<InteractionContext> sudoMapper,
            final @NonNull Callable<T> callable) {

        val currentInteractionLayer = interactionLayerTracker.currentInteractionLayerElseFail();
        val currentInteractionContext = currentInteractionLayer.getInteractionContext();
        val sudoInteractionContext = sudoMapper.apply(currentInteractionContext);

        try {
            for (val sudoListener : sudoListeners) {
                sudoListener.beforeCall(currentInteractionContext, sudoInteractionContext);
            }

            return interactionService.call(sudoInteractionContext, callable);

        } finally {
            for (val sudoListener : sudoListeners) {
                sudoListener.afterCall(sudoInteractionContext, currentInteractionContext);
            }
        }
    }

    /**
     * Executes the supplied {@link Callable} block, within the provided
     * {@link InteractionContext}.
     *
     * @param sudoMapper - maps the current {@link InteractionContext} to the sudo one
     * @since 2.0
     */
    public void run(
            final @NonNull UnaryOperator<InteractionContext> sudoMapper,
            final @NonNull ThrowingRunnable runnable) {
        call(sudoMapper, ThrowingRunnable.toCallable(runnable));
    }

}
