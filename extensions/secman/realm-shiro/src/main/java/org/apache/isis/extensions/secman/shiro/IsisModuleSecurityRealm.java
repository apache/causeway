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

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.isis.extensions.secman.api.SecurityRealm;
import org.apache.isis.extensions.secman.api.SecurityRealmCharacteristic;
import org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import lombok.val;

public class IsisModuleSecurityRealm extends AuthorizingRealm implements SecurityRealm {

    /**
     * Configures a {@link org.apache.shiro.authz.permission.PermissionResolver} that knows how to process the
     * permission strings that are provided by Isis'
     * {@link org.apache.isis.security.authorization.standard.Authorizor} for Shiro.
     */
    public IsisModuleSecurityRealm() {
        setPermissionResolver(new PermissionResolverForIsisShiroAuthorizor());
    }


    /**
     * In order to provide an attacker with additional information, the exceptions thrown here deliberately have
     * few (or no) details in their exception message. Similarly, the generic
     * {@link org.apache.shiro.authc.CredentialsException} is thrown for both a non-existent user and also an
     * invalid password.
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        if (!(token instanceof UsernamePasswordToken)) {
            throw new AuthenticationException();
        }

        val usernamePasswordToken = (UsernamePasswordToken) token;
        val username = usernamePasswordToken.getUsername();
        val password = usernamePasswordToken.getPassword();

        // lookup from database, for roles/perms
        val principal = lookupPrincipal(username);
        if (principal == null) {
            // if no delegate authentication
            throw new CredentialsException("Unknown user/password combination");
        }

        if (principal.isDisabled()) {
            // this is the default if delegated account and automatically created
            throw new DisabledAccountException(String.format("username='%s'", principal.getUsername()));
        }

        if(principal.getAccountType() == AccountType.DELEGATED) {
            AuthenticationInfo delegateAccount = null;
            if (hasDelegateAuthenticationRealm()) {
                try {
                    delegateAccount = delegateAuthenticationRealm.getAuthenticationInfo(token);
                } catch (AuthenticationException ex) {
                    // fall through
                }
            }
            if(delegateAccount == null) {
                throw new CredentialsException("Unknown user/password combination");
            }
        } else {
            final CheckPasswordResult result = checkPassword(password, principal.getEncryptedPassword());
            switch (result) {
                case OK:
                    break;
                case BAD_PASSWORD:
                    throw new IncorrectCredentialsException("Unknown user/password combination");
                case NO_PASSWORD_ENCRYPTION_SERVICE_CONFIGURED:
                    throw new AuthenticationException("No password encryption service is installed");
                default:
                    throw new AuthenticationException();
            }
        }

        val credentials = token.getCredentials();
        val realmName = getName();
        return new AuthInfoForApplicationUser(principal, realmName, credentials);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        final PrincipalForApplicationUser urp = principals.oneByType(PrincipalForApplicationUser.class);
        if (urp == null) {
            return null;
        }
        return urp;
    }

    private PrincipalForApplicationUser lookupPrincipal(final String username) {
    	
    	//FIXME[2157] do not auto-create if user cannot authenticate
    	// determine how to authenticate (delegate or local), whether disabled
        val autoCreateUser = hasDelegateAuthenticationRealm() && getAutoCreateUser();
    	
        return execute(new Supplier<PrincipalForApplicationUser>() {
            @Override
            public PrincipalForApplicationUser get() {
                val applicationUser = lookupUser();
                return PrincipalForApplicationUser.from(applicationUser);
            }

            private ApplicationUser lookupUser() {
                if (autoCreateUser) {
                    return applicationUserRepository.findOrCreateUserByUsername(username);
                } else {
                    return applicationUserRepository.findByUsername(username);
                }
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
            @Override
            public CheckPasswordResult get() {
                if (passwordEncryptionService == null) {
                    return CheckPasswordResult.NO_PASSWORD_ENCRYPTION_SERVICE_CONFIGURED;
                }
                return passwordEncryptionService.matches(new String(candidate), actualEncryptedPassword)
                        ? CheckPasswordResult.OK
                        : CheckPasswordResult.BAD_PASSWORD;
            }

            @Inject private PasswordEncryptionService passwordEncryptionService;
        });
    }

    private AuthenticatingRealm delegateAuthenticationRealm;
    public AuthenticatingRealm getDelegateAuthenticationRealm() {
        return delegateAuthenticationRealm;
    }
    public void setDelegateAuthenticationRealm(AuthenticatingRealm delegateRealm) {
        this.delegateAuthenticationRealm = delegateRealm;
    }

    public boolean hasDelegateAuthenticationRealm() {
        return delegateAuthenticationRealm != null;
    }

    private boolean autoCreateUser = true;

    public boolean getAutoCreateUser() {
        return autoCreateUser;
    }

    public void setAutoCreateUser(boolean autoCreateUser) {
        this.autoCreateUser = autoCreateUser;
    }

    <V> V execute(final Supplier<V> closure) {
        return getSessionFactory().doInSession(
                new Callable<V>() {
                    @Override
                    public V call() {
                    	val serviceInjector = IsisContext.getServiceInjector();
                    	serviceInjector.injectServicesInto(closure);
                        return doExecute(closure);
                    }
                }
        );
    }

    <V> V doExecute(final Supplier<V> closure) {
        val txTemplate = IsisContext.createTransactionTemplate();
        return txTemplate.execute(status->closure.get());
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession().orElse(null);
    }


    protected IsisSessionFactory getSessionFactory() {
        return IsisContext.getSessionFactory();
    }


	@Override
	public EnumSet<SecurityRealmCharacteristic> getCharacteristics() {
		if(hasDelegateAuthenticationRealm()) {
			return EnumSet.of(SecurityRealmCharacteristic.DELEGATING);
		}
		return EnumSet.noneOf(SecurityRealmCharacteristic.class);
	}

}
