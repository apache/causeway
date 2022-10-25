/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.testing.integtestsupport.applib;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.security.authentication.manager.UserMementoRefiner;

/**
 * Use to execute integration tests where any {@link UserMementoRefiner} services are honoured.
 * These can be used to tweak the current user/role.  (Normally {@link UserMementoRefiner}s are only
 * consulted using the authentication process, but in integration tests the authentication phase is skipped).
 *
 * <p>
 * This can be useful for various use cases, though one use case is as an alternative to using the
 * {@link NoPermissionChecks} extension.
 * </p>
 *
 * <p>
 *     To use, annotate integration test class using <code>@ExtendWith(UserMementoRefiners.class)</code>
 * </p>
 */
public class UserMementoRefiners implements BeforeEachCallback {

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        _Helper.getInteractionFactory(extensionContext)
            .ifPresent(interactionService ->
                interactionService.currentInteractionContext().ifPresent(
                    currentInteractionContext -> _Helper.getServiceRegistry(extensionContext).ifPresent(
                        serviceRegistry -> {
                            UserMemento user = currentInteractionContext.getUser();
                            for (UserMementoRefiner userMementoRefiner : serviceRegistry.select(UserMementoRefiner.class)) {
                                user = userMementoRefiner.refine(user);
                            }
                            interactionService.openInteraction(currentInteractionContext.withUser(user));
                        }
                    )
                )
            );
    }

}
