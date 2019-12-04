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
package org.apache.isis.security.shiro.authorization;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;

import org.apache.isis.applib.Identifier;
import org.apache.isis.security.api.authentication.standard.Authenticator;
import org.apache.isis.security.api.authorization.standard.Authorizor;
import org.apache.isis.security.shiro.ShiroSecurityContext;

/**
 * If Shiro is configured for both {@link AuthenticationManagerInstaller authentication} and
 * {@link AuthorizationManagerInstaller authorization} (as recommended), then this class is
 * instantiated twice in the role of the {@link Authorizor}.
 *
 * <p>
 * However, although there are two objects, they are set up to share the same {@link SecurityManager Shiro SecurityManager}
 * (bound to a thread-local).
 */
public class ShiroAuthorizor implements Authorizor {

    @Override
    public void init() {
    }


    @Override
    public void shutdown() {
    }


    @Override
    public boolean isVisibleInAnyRole(Identifier identifier) {
        return isPermitted(identifier, "r");
    }

    @Override
    public boolean isUsableInAnyRole(Identifier identifier) {
        return isPermitted(identifier, "w");
    }

    private boolean isPermitted(Identifier identifier, String qualifier) {

        RealmSecurityManager securityManager = getSecurityManager();
        if(securityManager == null) {
            // since a security manager will always be present for regular web requests, presumably the user
            // is running in fixtures during bootstrapping.  We therefore permit the interaction.
            return true;
        }

        String permission = asPermissionsString(identifier) + ":" + qualifier;

        Subject subject = SecurityUtils.getSubject();

        try {
            return subject.isPermitted(permission);
        } finally {
            IsisPermission.resetVetoedPermissions();
        }
    }

    private static String asPermissionsString(Identifier identifier) {
        String fullyQualifiedClassName = identifier.getClassName();
        int lastDot = fullyQualifiedClassName.lastIndexOf('.');
        String packageName;
        String className;
        if(lastDot > 0) {
            packageName =fullyQualifiedClassName.substring(0, lastDot);
            className = fullyQualifiedClassName.substring(lastDot+1);
        } else {
            packageName = "";
            className = fullyQualifiedClassName;
        }
        return packageName + ":" + className + ":" + identifier.getMemberName();
    }

    /**
     * Returns <tt>false</tt> because the checking across all roles is done in
     * {@link #isVisibleInAnyRole(Identifier)}, which is always called prior to this.
     */
    @Override
    public boolean isVisibleInRole(String role, Identifier identifier) {
        return false;
    }

    /**
     * Returns <tt>false</tt> because the checking across all roles is done in
     * {@link #isUsableInAnyRole(Identifier)}, which is always called prior to this.
     */
    @Override
    public boolean isUsableInRole(String role, Identifier identifier) {
        return false;
    }

    /**
     * The {@link SecurityManager} is shared between both the {@link Authenticator} and the {@link Authorizor}
     * (if shiro is configured for both components).
     */
    protected RealmSecurityManager getSecurityManager() {
        return ShiroSecurityContext.getSecurityManager();
    }

}
