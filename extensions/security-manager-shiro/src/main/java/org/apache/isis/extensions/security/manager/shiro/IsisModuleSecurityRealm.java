package org.apache.isis.extensions.security.manager.shiro;

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.isis.extensions.security.manager.api.SecurityRealm;
import org.apache.isis.extensions.security.manager.api.SecurityRealmCharacteristic;
import org.apache.isis.extensions.security.manager.api.password.PasswordEncryptionService;
import org.apache.isis.extensions.security.manager.api.user.AccountType;
import org.apache.isis.extensions.security.manager.api.user.ApplicationUser;
import org.apache.isis.extensions.security.manager.api.user.ApplicationUserRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

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
     * few (or no) details in their exception message.  Similarly, the generic
     * {@link org.apache.shiro.authc.CredentialsException} is thrown for both a non-existent user and also an
     * invalid password.
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        if (!(token instanceof UsernamePasswordToken)) {
            throw new AuthenticationException();
        }

        final UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String username = usernamePasswordToken.getUsername();
        char[] password = usernamePasswordToken.getPassword();

        // lookup from database, for roles/perms, but also
        // determine how to authenticate (delegate or local), whether disabled
        final PrincipalForApplicationUser principal = lookupPrincipal(username,
                (hasDelegateAuthenticationRealm() && getAutoCreateUser()));
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

        final Object credentials = token.getCredentials();
        final String realmName = getName();
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

    /**
     * @param username
     * @param autoCreateUser
     */
    private PrincipalForApplicationUser lookupPrincipal(final String username, final boolean autoCreateUser) {
        return execute(new Supplier<PrincipalForApplicationUser>() {
            @Override
            public PrincipalForApplicationUser get() {
                final ApplicationUser applicationUser = lookupUser();
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

            @Inject
            private PasswordEncryptionService passwordEncryptionService;
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
                        PersistenceSession persistenceSession = getPersistenceSession();
                        persistenceSession.getServiceInjector().injectServicesInto(closure);
                        return doExecute(closure);
                    }
                }
        );
    }

    <V> V doExecute(final Supplier<V> closure) {
        //final PersistenceSession persistenceSession = getPersistenceSession();
        
        TransactionTemplate txTemplate = IsisContext.createTransactionTemplate();

        return txTemplate.execute(status->{
        	return closure.get();
        });
        
//        
//        final IsisTransactionManager transactionManager = getTransactionManager(persistenceSession);
//        return transactionManager.executeWithinTransaction(closure);
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession().orElse(null);
    }

//    protected IsisTransactionManager getTransactionManager(PersistenceSession persistenceSession) {
//        return persistenceSession.getTransactionManager();
//    }

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
