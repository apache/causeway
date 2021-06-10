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

package org.apache.isis.core.security.authentication;

import java.util.Optional;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * Provides the current thread's {@link Authentication}.
 * @since 2.0
 * @deprecated
 */
@Deprecated
public interface AuthenticationProvider {

    /**
     * Optionally provides the current thread's {@link Authentication}.
     * <p>
     * That is the {@link Authentication} that sits at the top of
     * the current thread's {@link org.apache.isis.core.interaction.session.InteractionSession}'s
     * authentication layer stack.
     */
    Optional<InteractionContext> currentAuthentication();

    default InteractionContext currentAuthenticationElseFail() {
        return currentAuthentication()
                .orElseThrow(()->
                    _Exceptions.illegalState(
                            "no InteractionLayer available on current %s",
                            _Probe.currentThreadId()));
    }


}
