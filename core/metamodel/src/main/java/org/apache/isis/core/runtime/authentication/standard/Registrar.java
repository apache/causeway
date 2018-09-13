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

import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.runtime.authentication.RegistrationDetails;

public interface Registrar extends Authenticator, ApplicationScopedComponent {

    static Predicate<Registrar> NON_NULL = _NullSafe::isPresent; 

    static Function<Authenticator, Registrar> AS_REGISTRAR_ELSE_NULL = (final Authenticator input) -> {
            if (input instanceof Registrar) {
                return (Registrar) input;
            }
            return null;
    };

    /**
     * Whether the provided {@link RegistrationDetails} is recognized by this
     * {@link Registrar}.
     */
    boolean canRegister(Class<? extends RegistrationDetails> registrationDetailsClass);

    boolean register(RegistrationDetails registrationDetails);

}
