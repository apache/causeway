package org.apache.isis.extensions.security.manager.shiro;

import java.util.Collection;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

class AuthInfoForApplicationUser implements AuthenticationInfo, AuthorizationInfo {
    
    private static final long serialVersionUID = 1L;
    
    private final PrincipalForApplicationUser principal;
    private final String realmName;
    private final Object credentials;

    public AuthInfoForApplicationUser(PrincipalForApplicationUser principal, String realmName, Object credentials) {
        this.principal = principal;
        this.realmName = realmName;
        this.credentials = credentials;
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return new SimplePrincipalCollection(principal, realmName);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Collection<String> getRoles() {
        return principal.getRoles();
    }

    @Override
    public Collection<String> getStringPermissions() {
        return principal.getStringPermissions();
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        return principal.getObjectPermissions();
    }
}
