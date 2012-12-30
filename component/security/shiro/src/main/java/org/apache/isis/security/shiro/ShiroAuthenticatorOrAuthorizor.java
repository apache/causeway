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

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.authorization.standard.Authorizor;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;

import com.google.common.collect.Lists;

/**
 * If Shiro is configured for both {@link AuthenticationManagerInstaller authentication} and
 * {@link AuthorizationManagerInstaller authorization} (as recommended), then this class is
 * instantiated twice, once in the role of {@link Authenticator} and once in the role of the {@link Authorizor}.
 * 
 * <p>
 * However, although there are two objects, they are set up to share the same {@link SecurityManager Shiro SecurityManager}.
 */
public class ShiroAuthenticatorOrAuthorizor implements Authenticator, Authorizor {

    private static final Logger LOG = Logger.getLogger(ShiroAuthenticatorOrAuthorizor.class);

    private final IsisConfiguration configuration;

    private SecurityManager shiroSecurityManager;
    /**
     * Downcast of {@link #shiroSecurityManager} (if of this type).
     */
    private RealmSecurityManager realmSecurityManager;

    // //////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////

    public ShiroAuthenticatorOrAuthorizor(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    // //////////////////////////////////////////////////////
    // init, shutdown
    // //////////////////////////////////////////////////////

    @Override
    public void init() {
        this.shiroSecurityManager = getSecurityManager(configuration);
        if(shiroSecurityManager instanceof RealmSecurityManager) {
            this.realmSecurityManager = (RealmSecurityManager) shiroSecurityManager;
        }
    }

    /**
     * The {@link SecurityManager} is shared between both the {@link Authenticator} and the {@link Authorizor}
     * (if shiro is configured for both components).
     */
    private static synchronized SecurityManager getSecurityManager(final IsisConfiguration configuration) {
        try {
            return (DefaultSecurityManager) SecurityUtils.getSecurityManager();
        } catch(UnavailableSecurityManagerException ex) {
            Factory<SecurityManager> factory = new IniSecurityManagerFactory(configuration.getString("isis.security.shiro.iniLocation", "classpath:shiro.ini"));
            SecurityManager securityManager = factory.getInstance();
            SecurityUtils.setSecurityManager(securityManager);
            return securityManager;
        }
    }

    @Override
    public void shutdown() {
        //
    }

    // //////////////////////////////////////////////////////
    // Authenticator API
    // //////////////////////////////////////////////////////

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return AuthenticationRequestPassword.class.isAssignableFrom(authenticationRequestClass);
    }

    @Override
    public AuthenticationSession authenticate(final AuthenticationRequest request, final String code) {
        final AuthenticationToken token = asAuthenticationToken(request);
        
        Subject currentUser = SecurityUtils.getSubject();
        if(currentUser.isAuthenticated()) {
            // TODO: verify the code passed in that this session is still alive?
            
            // TODO: perhaps we should cache Isis' AuthenticationSession inside the Shiro Session, and
            // just retrieve it?
            
            // for now, just log them out.
            currentUser.logout();
        }
        try {
            currentUser.login(token);
        } catch ( UnknownAccountException uae ) { 
            LOG.debug("Unable to authenticate", uae);
            return null;
        } catch ( IncorrectCredentialsException ice ) {
            LOG.debug("Unable to authenticate", ice);
            return null;
        } catch ( LockedAccountException lae ) {
            LOG.info("Unable to authenticate", lae);
            return null;
        } catch ( ExcessiveAttemptsException eae ) { 
            LOG.info("Unable to authenticate", eae);
            return null;
        } catch ( AuthenticationException ae ) {
            LOG.error("Unable to authenticate", ae);
            return null;
        }
        
        List<String> roles = getRoles(token);
        
        return new SimpleSession(request.getName(), roles, code);
    }

    private List<String> getRoles(final AuthenticationToken token) {
        if(realmSecurityManager == null) {
            return Collections.emptyList();
        }
        final List<String> roles = Lists.newArrayList();
        final Collection<Realm> realms = realmSecurityManager.getRealms();
        for (final Realm realm : realms) {
            if(realm.supports(token)) {
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

    private static AuthenticationToken asAuthenticationToken(final AuthenticationRequest request) {
        final AuthenticationRequestPassword passwordRequest = (AuthenticationRequestPassword) request;
        final String username = passwordRequest.getName();
        final String password = passwordRequest.getPassword();
        
        return new UsernamePasswordToken(username, password);
    }


    // //////////////////////////////////////////////////////
    // Authorizor API
    // //////////////////////////////////////////////////////

    @Override
    public boolean isVisibleInAnyRole(Identifier identifier) {
        return false;
    }

    @Override
    public boolean isUsableInAnyRole(Identifier identifier) {
        return false;
    }

    @Override
    public boolean isVisibleInRole(String role, Identifier identifier) {
        if(shiroSecurityManager instanceof RealmSecurityManager) {
            
        }
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUsableInRole(String role, Identifier identifier) {
        // TODO Auto-generated method stub
        return false;
    }

    
    // //////////////////////////////////////////////////////
    // Injected (via constructor)
    // //////////////////////////////////////////////////////

    public IsisConfiguration getConfiguration() {
        return configuration;
    }

}
