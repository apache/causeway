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

import org.apache.causeway.applib.services.sudo.SudoService;

import lombok.val;

/**
 * Use to execute integration tests with permission checking disabled, with a user that has
 * the {@link SudoService#ACCESS_ALL_ROLE ACCESS_ALL_ROLE} role.
 *
 * <p>
 * This can be useful for example if working with code that depends on the secman extension,
 * where normally this activates the secman authorizor necessitating users and roles to be seeded and then
 * to run interact using an appropriate user.  Instead of all that, this extension effectively just
 * disables permission checking.
 * </p>
 *
 * <p>
 *     To use, annotate integration test class using <code>@ExtendWith(NoPermissionChecks.class)</code>
 * </p>
 */
public class NoPermissionChecks implements BeforeEachCallback {

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        _Helper.getInteractionFactory(extensionContext)
                .ifPresent(interactionService ->
                        interactionService.currentInteractionContext().ifPresent(
                                currentInteractionContext -> {
                                    val sudoUser = currentInteractionContext.getUser().withRoleAdded(SudoService.ACCESS_ALL_ROLE.getName());
                                    interactionService.openInteraction(currentInteractionContext.withUser(sudoUser));
                                }
                        )
                );
    }

}
