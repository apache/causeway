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

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.security.authorization.Authorizor;
import org.apache.isis.extensions.secman.applib.SecmanConfiguration;
import org.apache.isis.extensions.secman.applib.user.dom.AccountType;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.shiro.util.ShiroUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
public class IsisModuleExtSecmanShiroRealm extends AuthorizingRealm {

    private static final String SECMAN_UNLOCK_DELEGATED_USERS = "isis.ext.secman.unlockDelegatedUsers";
	@Inject protected ServiceInjector serviceInjector;
    @Inject protected InteractionService interactionService;
    @Inject protected PlatformTransactionManager txMan;
    @Inject private SecmanConfiguration configBean;
	@Inject protected IsisConfiguration isisConfiguration;

    @Getter @Setter private AuthenticatingRealm delegateAuthenticationRealm;
    @Getter @Setter private boolean autoCreateUser = true;

    /**
     * Configures a {@link org.apache.shiro.authz.permission.PermissionResolver} that knows how to process the
     * permission strings that are provided by Isis'
     * {@link Authorizor} for Shiro.
     */
    public IsisModuleExtSecmanShiroRealm() {
        setPermissionResolver(new PermissionResolverForIsisShiroAuthorizor());
    }

    /**
     * In order to not provide an attacker with additional information, the exceptions thrown here deliberately have
     * few (or no) details in their exception message. Similarly, the generic
     * {@link org.apache.shiro.authc.CredentialsException} is thrown for both a non-existent user and also an
     * invalid password.
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
        if (!(token instanceof UsernamePasswordToken)) {
            throw new AuthenticationException();
        }

        val usernamePasswordToken = (UsernamePasswordToken) token;
        val username = usernamePasswordToken.getUsername();
        val password = usernamePasswordToken.getPassword();


        // this code block is just an optimization, entirely optional
        {
            val alreadyAuthenticatedPrincipal =
                    getPrincipal_fromAlreadyAuthenticatedSubjectIfApplicable(token);
            if(alreadyAuthenticatedPrincipal!=null) {
                val credentials = token.getCredentials();
                val realmName = getName();
                return AuthInfoForApplicationUser.of(alreadyAuthenticatedPrincipal, realmName, credentials);
            }
        }

        // lookup from database, for roles/perms
        PrincipalForApplicationUser principal = lookupPrincipal_inApplicationUserRepository(username);

        val autoCreateUserWhenDelegatedAuthentication = hasDelegateAuthenticationRealm() && isAutoCreateUser();
        if (principal == null && autoCreateUserWhenDelegatedAuthentication) {
            // When using delegated authentication, desired behavior is to auto-create user accounts in the
            // DB only if these do successfully authenticate with the delegated authentication mechanism
            // while the newly created user will be disabled by default
            authenticateElseThrow_usingDelegatedMechanism(token);
            val newPrincipal = createPrincipal_inApplicationUserRepository(username);

            _Assert.assertNotNull(newPrincipal);

            if(configBean.isAutoUnlockIfDelegatedAndAuthenticated()) {
                principal = newPrincipal;
            } else {
                _Assert.assertTrue(newPrincipal.isLocked(), "As configured in " + SECMAN_UNLOCK_DELEGATED_USERS + ", auto-created user accounts are initially locked!");
                throw disabledAccountException(username); // default behavior after user auto-creation
            }
        }

        if (principal == null) {
            throw credentialsException();
        }

        if (principal.isLocked()) {
            throw disabledAccountException(principal.getUsername());
        }

        if(principal.getAccountType() == AccountType.DELEGATED) {
            authenticateElseThrow_usingDelegatedMechanism(token);
        } else {
            val checkPasswordResult = checkPassword(password, principal.getEncryptedPassword());
            switch (checkPasswordResult) {
            case OK:
                break;
            case BAD_PASSWORD:
                throw credentialsException();
            case NO_PASSWORD_ENCRYPTION_SERVICE_CONFIGURED:
                throw new AuthenticationException("No password encryption service is installed");
            default:
                throw new AuthenticationException();
            }
        }

        val credentials = token.getCredentials();
        val realmName = getName();
        return AuthInfoForApplicationUser.of(principal, realmName, credentials);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        return principals.oneByType(PrincipalForApplicationUser.class);
    }


    // -- HELPER

    /**
     * @implNote
     * This is just an optimization, entirely optional.
     * <p>
     * We reuse principal information on subjects that are already authenticated,
     * provided we are in a single realm authentication scenario.
     * @param token
     * @return {@code null} if not applicable
     */
    private PrincipalForApplicationUser getPrincipal_fromAlreadyAuthenticatedSubjectIfApplicable(
            final AuthenticationToken token) {

        // this optimization is only implemented for the simple case of a single realm setup
        if(!ShiroUtils.isSingleRealm()) {
            return null;
        }

        val currentSubject = SecurityUtils.getSubject();
        if(currentSubject!=null && currentSubject.isAuthenticated()) {
            val authenticatedPrincipalObject = currentSubject.getPrincipal();
            if(authenticatedPrincipalObject instanceof PrincipalForApplicationUser) {
                val authenticatedPrincipal = (PrincipalForApplicationUser) authenticatedPrincipalObject;
                val authenticatedUsername = authenticatedPrincipal.getUsername();
                val usernamePasswordToken = (UsernamePasswordToken) token;
                val username = usernamePasswordToken.getUsername();
                val isAuthenticatedWithThisRealm = username.equals(authenticatedUsername);
                if(isAuthenticatedWithThisRealm) {
                    return authenticatedPrincipal;
                }
            }
        }
        return null;
    }

    private DisabledAccountException disabledAccountException(final String username) {
        return new DisabledAccountException(String.format("username='%s'", username));
    }

    private CredentialsException credentialsException() {
        return new CredentialsException("Unknown user/password combination") {
            private static final long serialVersionUID = 1L;
            @Override public StackTraceElement[] getStackTrace() {
                // truncate reported stacktraces down to just 1 line
                val fullStackTrace = super.getStackTrace();
                return _NullSafe.size(fullStackTrace)>1
                        ? _Arrays.subArray(super.getStackTrace(), 0, 1)
                        : fullStackTrace;
            }
        };
    }

    private void authenticateElseThrow_usingDelegatedMechanism(final AuthenticationToken token) {
        AuthenticationInfo delegateAccount = null;
        try {
            delegateAccount = delegateAuthenticationRealm.getAuthenticationInfo(token);
        } catch (AuthenticationException ex) {
            // fall through
        }
        if(delegateAccount == null) {
            throw credentialsException();
        }
    }

    private PrincipalForApplicationUser lookupPrincipal_inApplicationUserRepository(final String username) {

        return execute(new Supplier<PrincipalForApplicationUser>() {
            @Override
            public PrincipalForApplicationUser get() {
                val applicationUser = applicationUserRepository.findByUsername(username).orElse(null);
                return PrincipalForApplicationUser.from(applicationUser);
            }
            @Inject private ApplicationUserRepository applicationUserRepository;
        });
    }

    private PrincipalForApplicationUser createPrincipal_inApplicationUserRepository(final String username) {

        return execute(new Supplier<PrincipalForApplicationUser>() {
            @Override
            public PrincipalForApplicationUser get() {
                val applicationUser = applicationUserRepository.findOrCreateUserByUsername(username);
                return PrincipalForApplicationUser.from(applicationUser);
            }
            @Inject private ApplicationUserRepository applicationUserRepository;
        });
    }


    private static enum CheckPasswordResult {
        OK,
        BAD_PASSWORD,
        NO_PASSWORD_ENCRYPTION_SERVICE_CONFIGURED
    }

    private CheckPasswordResult checkPassword(final char[] candidate, final String actualEncryptedPassword) {
        return execute(new Supplier<CheckPasswordResult>() {

            @Autowired(required = false) private @Qualifier("secman") PasswordEncoder passwordEncoder;

            @Override
            public CheckPasswordResult get() {
                if (passwordEncoder == null) {
                    return CheckPasswordResult.NO_PASSWORD_ENCRYPTION_SERVICE_CONFIGURED;
                }
                return passwordEncoder.matches(new String(candidate), actualEncryptedPassword)
                        ? CheckPasswordResult.OK
                        : CheckPasswordResult.BAD_PASSWORD;
            }

        });
    }

    private boolean hasDelegateAuthenticationRealm() {
        return delegateAuthenticationRealm != null;
    }

    <V> V execute(final Supplier<V> closure) {
        return interactionService.callAnonymous(
                new Callable<V>() {
                    @Override
                    public V call() {
                        serviceInjector.injectServicesInto(closure);
                        return doExecute(closure);
                    }
                }
                );
    }

    <V> V doExecute(final Supplier<V> closure) {
        val txTemplate = new TransactionTemplate(txMan);
        return txTemplate.execute(status->closure.get());
    }


}
