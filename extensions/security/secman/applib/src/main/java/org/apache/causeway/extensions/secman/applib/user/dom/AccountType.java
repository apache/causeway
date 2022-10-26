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

import org.apache.causeway.core.metamodel.commons.StringExtensions;

/**
 * Whether the user's account is local enabled (user/password) or
 * delegated (eg LDAP), as per
 * {@code org.apache.causeway.extensions.secman.shiro.CausewayModuleExtSecmanShiroRealm#setDelegateAuthenticationRealm(org.apache.shiro.realm.AuthenticatingRealm)}.
 *
 * @since 2.0 {@index}
 */
public enum AccountType {
    LOCAL,
    DELEGATED;

    public boolean isLocal() {
        return this==LOCAL;
    }

    public boolean isDelegated() {
        return this==DELEGATED;
    }

    @Override
    public String toString() {
        return StringExtensions.capitalize(name());
    }

}
