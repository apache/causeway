package org.apache.isis.extensions.secman.shiro;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;


/**
 * Acts as the Principal for the {@link IsisModuleSecurityRealm}, meaning that it is returned from
 * {@link IsisModuleSecurityRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken) authentication}, and passed into
 * {@link IsisModuleSecurityRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection) authorization}.
 *
 * <p>
 *     To minimize database lookups, holds the user, corresponding roles and the full set of permissions
 *     (all as value objects).  The permissions are eagerly looked up during
 *     {@link IsisModuleSecurityRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken) authentication} and so the
 *     {@link IsisModuleSecurityRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection) authorization} merely involves
 *     creating an adapter object for the appropriate Shiro API.
 * </p>
 *
 * TODO: this should probably implement java.security.Principal so that it doesn't get wrapped in a
 * ShiroHttpServletRequest.ObjectPrincipal.  Such a change would need some testing to avoid regressions, though.
 */
class PrincipalForApplicationUser implements AuthorizationInfo {

    private static final long serialVersionUID = 1L;
    
    public static PrincipalForApplicationUser from(ApplicationUser applicationUser) {
        if(applicationUser == null) {
            return null;
        }
        final String username = applicationUser.getName();
        final String encryptedPassword = applicationUser.getEncryptedPassword();
        final AccountType accountType = applicationUser.getAccountType();
        final Set<String> roles = applicationUser.getRoles()
        		.stream()
        		.map(ApplicationRole::getName)
        		.collect(Collectors.toCollection(TreeSet::new));
        final ApplicationPermissionValueSet permissionSet = applicationUser.getPermissionSet();
        return new PrincipalForApplicationUser(username, encryptedPassword, accountType, 
        		applicationUser.getStatus(), roles, permissionSet);
    }

    private final String username;
    private final Set<String> roles;
    private final String encryptedPassword;
    private final ApplicationUserStatus status;
    private final AccountType accountType;
    private final ApplicationPermissionValueSet permissionSet;

    PrincipalForApplicationUser(
            final String username,
            final String encryptedPassword,
            final AccountType accountType,
            final ApplicationUserStatus status,
            final Set<String> roles,
            final ApplicationPermissionValueSet applicationPermissionValueSet) {
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.accountType = accountType;
        this.roles = roles;
        this.status = status;
        this.permissionSet = applicationPermissionValueSet;
    }

    public boolean isDisabled() {
        return getStatus() == ApplicationUserStatus.DISABLED;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public Collection<String> getStringPermissions() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        final Permission o = new Permission() {
            @Override
            public boolean implies(Permission p) {
                if (!(p instanceof PermissionForMember)) {
                    return false;
                }
                final PermissionForMember pfm = (PermissionForMember) p;
                return getPermissionSet().grants(pfm.getFeatureId(), pfm.getMode());
            }
        };
        return Collections.singleton(o);
    }

    ApplicationUserStatus getStatus() {
        return status;
    }

    String getUsername() {
        return username;
    }

    String getEncryptedPassword() {
        return encryptedPassword;
    }

    ApplicationPermissionValueSet getPermissionSet() {
        return permissionSet;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    /**
     * When wrapped by ShiroHttpServletRequest.ObjectPrincipal, the principal's name is derived by calling toString().
     *
     *  TODO: this should probably implement java.security.Principal so that it doesn't get wrapped in a
     *  ShiroHttpServletRequest.ObjectPrincipal.  Such a change would need some testing to avoid regressions, though.
     *
     */
    @Override
    public String toString() {
        return getUsername();
    }
}
