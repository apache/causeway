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
package org.apache.isis.extensions.secman.api.permission;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;

/**
 * Named after UNIX modes (<code>chmod</code> etc), determines that nature of access (of denial of access if vetoed)
 * to an {@link ApplicationFeature}.
 */
public enum ApplicationPermissionMode {
    /**
     * Whether the user/role can view (or is prevented from viewing) the application feature (class member).
     *
     * <p>
     * The {@link ApplicationPermissionRule rule} of the
     * {@link org.apache.isis.extensions.security.manager.jdo.dom.permission.ApplicationPermission} indicates whether access is being
     * granted or denied.
     * </p>
     */
    VIEWING,
    /**
     * Whether can user/role can change (or is prevented from changing) the state of the system using the application feature (class member).
     *
     * <p>
     * In other words, whether they can execute (if an action, modify/clear (if a property), addTo/removeFrom
     * (if a collection).
     * </p>
     *
     * <p>
     * The {@link ApplicationPermissionRule rule} of the
     * {@link org.apache.isis.extensions.security.manager.jdo.dom.permission.ApplicationPermission} indicates whether access is being
     * granted or denied.
     * </p>
     *
     * <p>
     * Note that the Wicket viewer does not at the time of writing (Isis 1.6.0) support the notion of
     * mutable collections.  The RO viewer does, however.
     * </p>
     */
    CHANGING;

    @Override
    public String toString() {
        return _Strings.capitalize(name());
    }

}
