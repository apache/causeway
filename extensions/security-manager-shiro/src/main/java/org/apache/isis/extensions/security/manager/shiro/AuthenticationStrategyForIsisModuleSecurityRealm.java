package org.apache.isis.extensions.security.manager.shiro;

import java.util.Collection;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.pam.AllSuccessfulStrategy;
import org.apache.shiro.realm.Realm;

public class AuthenticationStrategyForIsisModuleSecurityRealm extends AllSuccessfulStrategy {

    /**
     * Reconfigures the SimpleAuthenticationInfo to use a implementation for storing its PrincipalCollections.
     *
     * <p>
     *    The default implementation uses a {@link org.apache.shiro.subject.SimplePrincipalCollection}, however this
     *    doesn't play well with the Isis Addons' security module which ends up chaining together multiple instances of
     *    {@link org.apache.isis.extensions.security.manager.shiro.PrincipalForApplicationUser} for each login.  This is probably
     *    because of it doing double duty with holding authorization information.  There may be a better design here,
     *    but for now the solution I've chosen is to use a different implementation of
     *    {@link org.apache.shiro.subject.PrincipalCollection} that will only ever store one instance of
     *    {@link org.apache.isis.extensions.security.manager.shiro.PrincipalForApplicationUser} as a principal.
     * </p>
     */
    @Override
    public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token) throws AuthenticationException {
        final SimpleAuthenticationInfo authenticationInfo = (SimpleAuthenticationInfo) super.beforeAllAttempts(realms, token);

        authenticationInfo.setPrincipals(new PrincipalCollectionWithSinglePrincipalForApplicationUserInAnyRealm());
        return authenticationInfo;
    }

}
