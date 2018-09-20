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
package org.apache.isis.security.shiro;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.isis.commons.internal.collections._Lists;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.authorization.standard.Authorizor;
import org.apache.isis.security.shiro.authorization.IsisPermission;

/**
 * If Shiro is configured for both {@link AuthenticationManagerInstaller authentication} and
 * {@link AuthorizationManagerInstaller authorization} (as recommended), then this class is
 * instantiated twice, once in the role of {@link Authenticator} and once in the role of the {@link Authorizor}.
 *
 * <p>
 * However, although there are two objects, they are set up to share the same {@link SecurityManager Shiro SecurityManager}
 * (bound to a thread-local).
 */
public class ShiroAuthenticatorOrAuthorizor implements Authenticator, Authorizor {

    private static final Logger LOG = LoggerFactory.getLogger(ShiroAuthenticatorOrAuthorizor.class);

    private static final String ISIS_AUTHENTICATION_SHIRO_AUTO_LOGOUT_KEY = "isis.authentication.shiro.autoLogoutIfAlreadyAuthenticated";
    private static final boolean ISIS_AUTHENTICATION_SHIRO_AUTO_LOGOUT_DEFAULT = false;

    // -- constructor and fields
    private final IsisConfiguration configuration;
    private final boolean autoLogout;

    private DeploymentCategory deploymentCategory;

    public ShiroAuthenticatorOrAuthorizor(final IsisConfiguration configuration) {
        this.configuration = configuration;
        autoLogout = configuration.getBoolean(
                ISIS_AUTHENTICATION_SHIRO_AUTO_LOGOUT_KEY,
                ISIS_AUTHENTICATION_SHIRO_AUTO_LOGOUT_DEFAULT);
    }

    public IsisConfiguration getConfiguration() {
        return configuration;
    }



    // -- init, shutdown

    @Override
    public void init(final DeploymentCategory deploymentCategory) {
        this.deploymentCategory = deploymentCategory;
    }


    @Override
    public void shutdown() {
    }

    @Override
    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }



    // -- Authenticator API

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        if(getSecurityManager() == null) {
            return false;
        }
        return AuthenticationRequestPassword.class.isAssignableFrom(authenticationRequestClass);
    }

    @Override
    public AuthenticationSession authenticate(final AuthenticationRequest request, final String code) {
        RealmSecurityManager securityManager = getSecurityManager();
        if(securityManager == null) {
            return null;
        }
        final AuthenticationToken token = asAuthenticationToken(request);

        final Subject currentSubject = SecurityUtils.getSubject();
        if(currentSubject.isAuthenticated()) {

            if(autoLogout) {
                // this is preserving behaviour pre 1.13.0.  However, there is a suspicion that this might
                // produce a race condition.  In 1.13.0 the default is to simply reuse the session.
                //
                // See this thread for further info: http://markmail.org/message/hsjljwgkhhrzxbrm
                currentSubject.logout();
            } else {

                // TODO: should we verify the code passed in that this session is still alive?
                // TODO: perhaps we should cache Isis' AuthenticationSession inside the Shiro Session, and just retrieve it?

                return authenticationSessionFor(request, code, token, currentSubject);
            }
        }
        try {
            currentSubject.login(token);
        } catch ( UnknownAccountException uae ) {
            LOG.info("Unknown account: {}", request.getName());
            return null;
        } catch ( IncorrectCredentialsException ice ) {
            LOG.info("Incorrect credentials for user: {}", request.getName());
            return null;
        } catch ( CredentialsException ice ) {
            // it seems that this is the exception that is actually thrown for invalid user/password.
            LOG.info("Unable to authenticate", ice);
            return null;
        } catch ( LockedAccountException lae ) {
            LOG.info("Locked account for user: {}", request.getName());
            return null;
        } catch ( ExcessiveAttemptsException eae ) {
            LOG.info("Excessive attempts for user: {}", request.getName());
            return null;
        } catch ( AuthenticationException ae ) {
            LOG.error("Unable to authenticate", ae);
            return null;
        }

        return authenticationSessionFor(request, code, token, currentSubject);
    }

    @Override
    public void logout(final AuthenticationSession session) {
        Subject currentSubject = SecurityUtils.getSubject();
        if(currentSubject.isAuthenticated()) {
            currentSubject.logout();
        }
    }

    AuthenticationSession authenticationSessionFor(AuthenticationRequest request, String code, AuthenticationToken token, Subject currentSubject) {
        List<String> roles = getRoles(currentSubject, token);
        // copy over any roles passed in
        // (this is used by the Wicket viewer, for example).
        roles.addAll(request.getRoles());

        return new SimpleSession(request.getName(), roles, code);
    }

    /**
     * This method has protected visibility to allow for custom implementations
     * in the future that might obtain the list of roles for a principal from
     * somewhere other than Shiro's {@link RealmSecurityManager}.
     */
    protected List<String> getRoles(final Subject subject, final AuthenticationToken token) {
        final List<String> roles = _Lists.newArrayList();

        RealmSecurityManager securityManager = getSecurityManager();
        if(securityManager == null) {
            return roles;
        }

        final Set<String> realmNames = realmNamesOf(subject);
        final Collection<Realm> realms = securityManager.getRealms();
        for (final Realm realm : realms) {
            // only obtain roles from those realm(s) that authenticated this subject
            if(!realmNames.contains(realm.getName())) {
                continue;
            }
            final AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo(token);
            if(authenticationInfo instanceof AuthorizationInfo) {
                final AuthorizationInfo authorizationInfo = (AuthorizationInfo) authenticationInfo;
                final Collection<String> realmRoles = authorizationInfo.getRoles();
                for (final String role : realmRoles) {
                    roles.add(realm.getName() + ":" + role);
                }
            }
        }
        return roles;
    }

    private static Set<String> realmNamesOf(final Subject subject) {
        final PrincipalCollection principals = subject.getPrincipals();
        return principals != null? principals.getRealmNames(): Collections.<String>emptySet();
    }

    private static AuthenticationToken asAuthenticationToken(final AuthenticationRequest request) {
        final AuthenticationRequestPassword passwordRequest = (AuthenticationRequestPassword) request;
        final String username = passwordRequest.getName();
        final String password = passwordRequest.getPassword();

        return new UsernamePasswordToken(username, password);
    }


    // -- Authorizor API

    @Override
    public boolean isVisibleInAnyRole(Identifier identifier) {
        return isPermitted(identifier, "r");
    }

    @Override
    public boolean isUsableInAnyRole(Identifier identifier) {
        
        if((""+identifier).startsWith("domainapp.modules.simple.dom.impl.SimpleObject")) {
            return false;
        }
        
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



    // -- Injected (via Shiro service locator)

    /**
     * The {@link SecurityManager} is shared between both the {@link Authenticator} and the {@link Authorizor}
     * (if shiro is configured for both components).
     */
    protected RealmSecurityManager getSecurityManager() {
        SecurityManager securityManager;
        try {
            securityManager = SecurityUtils.getSecurityManager();
        } catch(UnavailableSecurityManagerException ex) {
            return null;
        }
        if(!(securityManager instanceof RealmSecurityManager)) {
            return null;
        }
        return (RealmSecurityManager) securityManager;
    }



}
