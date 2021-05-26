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
package org.apache.isis.extensions.secman.shiro;

import org.apache.shiro.authz.Permission;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionMode;

/**
 * As created by {@link org.apache.isis.extensions.secman.shiro.PermissionResolverForIsisShiroAuthorizor}, interprets the
 * permission strings formatted by <code>IsisShiroAuthorizor</code>.
 */
class PermissionForMember implements org.apache.shiro.authz.Permission {

    private final ApplicationFeatureId featureId;
    private final ApplicationPermissionMode mode;

    /**
     * Expects in format <code>package:className:methodName:r|w</code>
     */
    public PermissionForMember(String permissionString) {
        final String[] split = permissionString.split("\\:");
        if(split.length == 4) {
            String packageName = split[0];
            String className = split[1];
            String memberName = split[2];
            this.featureId = ApplicationFeatureId.newMember(packageName + "." + className, memberName);

            ApplicationPermissionMode mode = modeFrom(split[3]);
            if(mode != null) {
                this.mode = mode;
                return;
            }
        }
        throw new IllegalArgumentException("Invalid format for permission: " + permissionString + "; expected 'packageName:className:methodName:r|w");
    }

    private static ApplicationPermissionMode modeFrom(String s) {
        if("r".equals(s)) {
            return ApplicationPermissionMode.VIEWING;
        }
        if("w".equals(s)) {
            return ApplicationPermissionMode.CHANGING;
        }
        return null;
    }

    /**
     */
    @Override
    public boolean implies(Permission p) {
        return false;
    }

    ApplicationFeatureId getFeatureId() {
        return featureId;
    }

    ApplicationPermissionMode getMode() {
        return mode;
    }
}
