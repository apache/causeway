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

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeature;

/**
 * Whether the permission {@link #ALLOW grants} or {@link #VETO denies} access to an
 * {@link ApplicationFeature}.
 */
public enum ApplicationPermissionRule {
    /**
     * The permission grants the ability to view/use the {@link ApplicationFeature}.
     *
     * <p>
     * The {@link ApplicationPermissionMode mode} determines whether the
     * permission is to only view or also to use the {@link ApplicationFeature}.
     * </p>
     */
    ALLOW,
    /**
     * The permission prevents the ability to view/use the {@link ApplicationFeature}.
     *
     * <p>
     * The {@link ApplicationPermissionMode mode} determines whether the
     * permission is to only view or also to use the {@link ApplicationFeature}.
     * </p>
     */
    VETO;

    @Override
    public String toString() {
        return _Strings.capitalize(name());
    }
}
