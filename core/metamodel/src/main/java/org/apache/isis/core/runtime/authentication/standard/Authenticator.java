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

package org.apache.isis.core.runtime.authentication.standard;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;

public interface Authenticator extends ApplicationScopedComponent {

    /**
     * Whether the provided {@link AuthenticationRequest} is recognized by this
     * {@link Authenticator}.
     */
    boolean canAuthenticate(Class<? extends AuthenticationRequest> authenticationRequestClass);

    /**
     * UNUSED ... IMPLEMENTATIONS SHOULD PROVIDE A STUB METHOD ONLY.
     * 
     * <p>
     * This method is only ever called from {@link AuthenticatorAbstract}, and as such should
     * not be defined as part of the API.
     * 
     * <p>
     * TODO: remove in 2.0.0 [ISIS-292]
     */
    @Deprecated
    boolean isValid(AuthenticationRequest request);

    /**
     * @param code
     *            - a hint; is guaranteed to be unique, but the authenticator
     *            decides whether to use it or not.
     */
    AuthenticationSession authenticate(AuthenticationRequest request, String code);

}
