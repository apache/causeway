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

/**
 * Enables {@link ImpersonateMenu.impersonateWithRoles#act(String, List, String)},
 * to provides choices for user and roles.
 *
 * <p>
 *     This will result in the simpler {@link ImpersonateMenu.impersonate#act(String)}
 *     (which simply allows a username to be specified, with no roles) being hidden.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface ImpersonateMenuAdvisor {

    /**
     * Returns the names of all known users.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to provide a choices
     *     (drop-down) for the username (string) argument of
     *     {@link ImpersonateMenu.impersonateWithRoles#act(String, List, String)}.
     * </p>
     */
    List<String> allUserNames();

    /**
     * Returns the names of all known roles.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to provide a choices
     *     (drop-down) for the rolenames (list) argument of
     *     {@link ImpersonateMenu.impersonateWithRoles#act(String, List, String)}.
     * </p>
     */
    List<String> allRoleNames();

    /**
     * Returns the names of the roles of the specified username.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to select the defaults
     *     for the rolenames (list) argument of
     *     {@link ImpersonateMenu.impersonateWithRoles#act(String, List, String)}.
     * </p>
     */
    List<String> roleNamesFor(final String username);

    /**
     * Returns the multi-tenancy token of the specified username.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to select the defaults
     *     for the rolenames (list) argument of
     *     {@link ImpersonateMenu.impersonateWithRoles#act(String, List, String)}.
     * </p>
     */
    String multiTenancyTokenFor(final String username);

}
