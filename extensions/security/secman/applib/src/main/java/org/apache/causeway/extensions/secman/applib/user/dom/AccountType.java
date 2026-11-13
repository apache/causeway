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
package org.apache.causeway.extensions.secman.applib.user.dom;

import org.apache.causeway.commons.internal.base._Strings;

/**
 * Whether the user's account is defined by locally (Secman itself is the authenticator) or instead if there's a
 * different security module that has responsibility for authentication (for example Spring Security with OAuth2).
 * This is the delegate model.
 *
 * @since 2.0 {@index}
 */
public enum AccountType {
    /**
     * Secman itself is the authenticator.
     */
    LOCAL,
    /**
     * Another security module performs the primary authentication, for example Spring Security with OAuth2.
     */
    DELEGATED;

    public boolean isLocal() {
        return this==LOCAL;
    }

    public boolean isDelegated() {
        return this==DELEGATED;
    }

    @Override
    public String toString() {
        return _Strings.capitalize(name());
    }

}
