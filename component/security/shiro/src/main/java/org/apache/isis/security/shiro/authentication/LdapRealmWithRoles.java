package org.apache.isis.security.shiro.authentication;

import java.util.Collections;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;

import com.google.common.collect.Sets;

/**
 * Implementation of {@link org.apache.shiro.realm.ldap.JndiLdapRealm} that also
 * returns each user's groups.
 * 
 * <p>
 * Sample config for <tt>shiro.ini</tt>:
 * 
 * <pre>
 *   ldapRealm = org.apache.isis.security.shiro.LdapRealmWithRoles
 *   ldapRealm.userDnTemplate = uid={0},ou=users,o=mojo
 *   ldapRealm.contextFactory.url = ldap://localhost:10389
 *   ldapRealm.contextFactory.authenticationMechanism = simple
 *   ldapRealm.contextFactory.systemUsername = admin
 *   ldapRealm.contextFactory.systemPassword = secret
 *   
 *   ldapRealm.searchBase = ou=groups,o=mojo
 *   ldapRealm.groupNameAttribute = cn
 *   ldapRealm.groupMembersAttribute = uniqueMember
 * </pre>
 */
public class LdapRealmWithRoles extends org.apache.shiro.realm.ldap.JndiLdapRealm {

    private String searchBase;
    private String groupNameAttribute;
    private String groupMembersAttribute;
//    private Map<String, String> groupRolesMap;

    private final static SearchControls SUBTREE_SCOPE = new SearchControls();
    static {
        SUBTREE_SCOPE.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    /**
     * Get groups from LDAP.
     * 
     * @param principals
     *            the principals of the Subject whose AuthenticationInfo should
     *            be queried from the LDAP server.
     * @param ldapContextFactory
     *            factory used to retrieve LDAP connections.
     * @return an {@link AuthorizationInfo} instance containing information
     *         retrieved from the LDAP server.
     * @throws NamingException
     *             if any LDAP errors occur during the search.
     */
    @Override
    protected AuthorizationInfo queryForAuthorizationInfo(final PrincipalCollection principals, final LdapContextFactory ldapContextFactory) throws NamingException {
        final Set<String> roleNames = getRoles(principals, ldapContextFactory);
        return new SimpleAuthorizationInfo(roleNames);
    }

    private Set<String> getRoles(final PrincipalCollection principals, final LdapContextFactory ldapContextFactory) throws NamingException {
        final String username = (String) getAvailablePrincipal(principals);

        LdapContext systemLdapCtx = null;
        try {
            systemLdapCtx = ldapContextFactory.getSystemLdapContext();
            return rolesFor(username, systemLdapCtx);
        } catch (AuthenticationException ex) {
            // principal was not authenticated on LDAP
            return Collections.emptySet();
        } finally {
            LdapUtils.closeContext(systemLdapCtx);
        }
    }

    private Set<String> rolesFor(final String groupName, final LdapContext ldapCtx) throws NamingException {
        final Set<String> roleNames = Sets.newLinkedHashSet();
        final NamingEnumeration<SearchResult> searchResultEnum = ldapCtx.search(searchBase, groupNameAttribute + "=" + groupName, SUBTREE_SCOPE);
        while (searchResultEnum.hasMore()) {
            final SearchResult sr = searchResultEnum.next();
            final NamingEnumeration<? extends Attribute> attributeEnum = sr.getAttributes().getAll();
            while (attributeEnum.hasMore()) {
                final Attribute attr = attributeEnum.next();
                if (!groupMembersAttribute.equalsIgnoreCase(attr.getID())) {
                    continue;
                } 
                final NamingEnumeration<?> e = attr.getAll();
                while (e.hasMore()) {
                    Object next = e.next();

                    roleNames.add(next.toString());

//                    String role = groupRolesMap.get(next);
//                    if (role != null) {
//                        roleNames.add(role);
//                    }
                }
            }
        }
        return roleNames;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public void setGroupNameAttribute(String groupNameAttribute) {
        this.groupNameAttribute = groupNameAttribute;
    }

    public void setGroupMembersAttribute(String groupMembersAttribute) {
        this.groupMembersAttribute = groupMembersAttribute;
    }

}
