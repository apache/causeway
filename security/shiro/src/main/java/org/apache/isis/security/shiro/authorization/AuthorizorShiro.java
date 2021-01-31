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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.standard.Authenticator;
import org.apache.isis.core.security.authorization.standard.Authorizor;
import org.apache.isis.security.shiro.context.ShiroSecurityContext;

import lombok.val;

/**
 * If Shiro is configured for both authentication and authorization (as recommended), then this class is
 * in the role of {@link Authorizor}.
 *
 * <p>
 * However, although there are two objects, they are set up to share the same {@link SecurityManager Shiro SecurityManager}
 * (bound to a thread-local).
 * </p>
 *
 * @since 1.x {@index}
 */
@Service
@Named("isis.security.AuthorizorShiro")
@Order(OrderPrecedence.EARLY)
@Qualifier("Shiro")
public class AuthorizorShiro implements Authorizor {

    @Inject private SpecificationLoader specificationLoader;

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

    private String asPermissionsString(Identifier identifier) {
        String fullyQualifiedLogicalTypeName = asFeatureFqns(identifier);
        int lastDot = fullyQualifiedLogicalTypeName.lastIndexOf('.');
        String packageName;
        String className;
        if(lastDot > 0) {
            packageName =fullyQualifiedLogicalTypeName.substring(0, lastDot);
            className = fullyQualifiedLogicalTypeName.substring(lastDot+1);
        } else {
            packageName = "";
            className = fullyQualifiedLogicalTypeName;
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

    // -- DEPS

    /**
     * The {@link SecurityManager} is shared between both the {@link Authenticator} and the {@link Authorizor}
     * (if shiro is configured for both components).
     */
    protected RealmSecurityManager getSecurityManager() {
        return ShiroSecurityContext.getSecurityManager();
    }

    // -- HELPER

    /**
     * @deprecated while this is technically correct, we should not need to call the SpecificationLoader
     * on every permission check
     */
    private String asFeatureFqns(Identifier identifier) {
        val className = identifier.getClassName();
        return Result.of(()->_Context.loadClass(className))
                .<String>mapSuccess(this::asFeatureFqns)
                .orElse(className);
    }

    private String asFeatureFqns(Class<?> cls) {
        return Optional.ofNullable(specificationLoader.loadSpecification(cls))
                .map(ObjectSpecification::getSpecId)
                .map(ObjectSpecId::asString)
                .orElseGet(()->cls.getName());
    }


}
