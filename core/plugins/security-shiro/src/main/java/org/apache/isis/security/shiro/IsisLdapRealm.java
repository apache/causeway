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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.StringUtils;

import org.apache.isis.security.shiro.permrolemapper.PermissionToRoleMapper;
import org.apache.isis.security.shiro.permrolemapper.PermissionToRoleMapperFromIni;
import org.apache.isis.security.shiro.permrolemapper.PermissionToRoleMapperFromString;

/**
 * Implementation of {@link org.apache.shiro.realm.ldap.JndiLdapRealm} that also
 * returns each user's groups.
 * <p/>
 * <p>
 * Sample config for <tt>shiro.ini</tt>:
 * <p/>
 * <pre>
 * contextFactory = org.apache.isis.security.shiro.IsisLdapContextFactory
 * contextFactory.url = ldap://localhost:10389
 * contextFactory.authenticationMechanism = CRAM-MD5
 * contextFactory.systemAuthenticationMechanism = simple
 * contextFactory.systemUsername = uid=admin,ou=system
 * contextFactory.systemPassword = secret
 *
 * ldapRealm = org.apache.isis.security.shiro.IsisLdapRealm
 * ldapRealm.contextFactory = $contextFactory
 *
 * ldapRealm.searchBase = ou=groups,o=mojo
 * ldapRealm.groupObjectClass = groupOfUniqueNames
 * ldapRealm.uniqueMemberAttribute = uniqueMember
 * ldapRealm.uniqueMemberAttributeValueTemplate = uid={0}
 *
 * ldapRealm.searchUserBase = ou=users,o=mojo
 * ldapRealm.userObjectClass=inetOrgPerson
 * ldapRealm.groupExtractedAttribute=street,country
 * ldapRealm.userExtractedAttribute=street,country
 * ldapRealm.permissionByGroupAttribute=attribute:Folder.{street}:Read,attribute:Portfolio.{country}
 * ldapRealm.permissionByUserAttribute=attribute:Folder.{street}:Read,attribute:Portfolio.{country}
 *
 * # optional mapping from physical groups to logical application roles
 * ldapRealm.rolesByGroup = \
 *    LDN_USERS: user_role,\
 *    NYK_USERS: user_role,\
 *    HKG_USERS: user_role,\
 *    GLOBAL_ADMIN: admin_role,\
 *    DEMOS: self-install_role
 *
 * securityManager.realms = $ldapRealm
 * </pre>
 * <p/>
 * <p>
 * The permissions for each role can be specified using the
 * {@link #setResourcePath(String)} to an 'ini' file with a [roles] section, eg:
 * <p/>
 * <pre>
 * ldapRealm.resourcePath=classpath:webapp/myroles.ini
 * </pre>
 * <p/>
 * <p>
 * where <tt>myroles.ini</tt> is in <tt>src/main/resources/webapp</tt>, and takes the form:
 * <p/>
 * <pre>
 * [roles]
 * user_role = *:ToDoItemsJdo:*:*,\
 *             *:ToDoItem:*:*
 * self-install_role = *:ToDoItemsFixturesService:install:*
 * admin_role = *
 * </pre>
 * <p/>
 * <p>
 * This 'ini' file can then be referenced by other realms (if multiple realm are configured
 * with the Shiro security manager).
 * <p/>
 * <p>
 * Alternatively, permissions can be set directly using {@link #setPermissionsByRole(String)},
 * where the string is the same information, formatted thus:
 * <p/>
 * <pre>
 * ldapRealm.permissionsByRole=\
 *    user_role = *:ToDoItemsJdo:*:*,\
 *                *:ToDoItem:*:*; \
 *    self-install_role = *:ToDoItemsFixturesService:install:* ; \
 *    admin_role = *
 * </pre>
 * <p/>
 * <p>
 * Alternatively, permissions can be extracted from the base itself with the parameter searchUserBase,
 * the attribute list as userExtractedAttribute and the permission url as permissionByUserAttribute.
 * The idea is to extract attribute from the user or the group of the user and map directly to permission rule in
 * replacing the string {attribute} by the extracted attribute (can me multiple).
 * See the sample for group and user attribute and mapping.
 * <p/>
 * </p>
 */
public class IsisLdapRealm extends JndiLdapRealm {

    private static final String UNIQUEMEMBER_SUBSTITUTION_TOKEN = "{0}";
    private final static SearchControls SUBTREE_SCOPE = new SearchControls();

    static {
        SUBTREE_SCOPE.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    private String searchBase;
    private String groupObjectClass;
    private String uniqueMemberAttribute = "uniqueMember";
    private String uniqueMemberAttributeValuePrefix;
    private String uniqueMemberAttributeValueSuffix;

    /**
     * For Group Extracted attribute name with mapping name in parenthesis. Ex: street,country
     */
    protected Set<String> groupExtractedAttribute = Sets.newConcurrentHashSet();

    /**
     * For User Extracted attribute name with mapping name in parenthesis. Ex: street,country
     */
    protected Set<String> userExtractedAttribute = Sets.newConcurrentHashSet();

    /**
     * For Group Mapping of attributes. Ex:
     * attribute:Folder.{street}:Read,attribute:Portfolio.{country}:*
     */
    protected Set<String> permissionByGroupAttribute = Sets.newConcurrentHashSet();

    /**
     * For User Mapping of attributes. Ex:
     * attribute:Folder.{street}:Read,attribute:Portfolio.{country}:*
     */
    protected Set<String> permissionByUserAttribute = Sets.newConcurrentHashSet();

    /**
     * For search ldap on user
     */
    private String searchUserBase = "";

    /**
     * The object className as person
     */
    private String userObjectClass;

    private final Map<String, String> rolesByGroup = Maps.newLinkedHashMap();

    private PermissionToRoleMapper permissionToRoleMapper;

    /**
     * cn attribute
     */
    private String cnAttribute = "cn";

    public IsisLdapRealm() {
        setGroupObjectClass("groupOfUniqueNames");
        setUniqueMemberAttribute("uniqueMember");
        setUniqueMemberAttributeValueTemplate("uid={0}");
    }

    /**
     * Get groups from LDAP.
     *
     * @param principals         the principals of the Subject whose AuthenticationInfo should
     *                           be queried from the LDAP server.
     * @param ldapContextFactory factory used to retrieve LDAP connections.
     * @return an {@link AuthorizationInfo} instance containing information
     * retrieved from the LDAP server.
     * @throws NamingException if any LDAP errors occur during the search.
     */
    @Override
    protected AuthorizationInfo queryForAuthorizationInfo(final PrincipalCollection principals, final LdapContextFactory ldapContextFactory) throws NamingException {
        final Set<String> roleNames = getRoles(principals, ldapContextFactory);
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo(roleNames);
        Set<String> stringPermissions = permsFor(roleNames);
        final String username = (String) getAvailablePrincipal(principals);
        final LdapContext finalLdapContext = ldapContextFactory.getSystemLdapContext();
        stringPermissions.addAll(getPermissionForUser(username, finalLdapContext));
        stringPermissions.addAll(getPermissionForRole(username, finalLdapContext));
        simpleAuthorizationInfo.setStringPermissions(stringPermissions);

        return simpleAuthorizationInfo;
    }

    private Set<String>
    getPermissionForRole(String username, LdapContext ldapContext)
            throws NamingException {
        final Set<String> permissions = Sets.newLinkedHashSet();

        Set<String> groups = groupFor(username, ldapContext);
        final NamingEnumeration<SearchResult> searchResultEnum = ldapContext.search(searchBase,
                "objectClass=" + groupObjectClass, SUBTREE_SCOPE);
        while (searchResultEnum.hasMore()) {
            final SearchResult group = searchResultEnum.next();
            if (memberOf(group, groups)) {
                addPermIfFound(group, permissions, groupExtractedAttribute, permissionByGroupAttribute);
            }
        }
        return permissions;
    }

    protected Set<String> groupFor(final String userName, final LdapContext ldapCtx)
            throws NamingException {
        final Set<String> roleNames = Sets.newLinkedHashSet();
        final NamingEnumeration<SearchResult> searchResultEnum = ldapCtx.search(searchBase,
                "objectClass=" + groupObjectClass, SUBTREE_SCOPE);
        while (searchResultEnum.hasMore()) {
            final SearchResult group = searchResultEnum.next();
            addRoleIfMember(userName, group, roleNames);
        }
        return roleNames;
    }

    protected boolean memberOf(SearchResult group, Set<String> groups) throws NamingException {
        Attribute attribute = group.getAttributes().get(cnAttribute);
        String groupName = attribute.get().toString();
        return groups.contains(groupName);
    }

    private Collection<String> getPermissionForUser(
            String username,
            LdapContext ldapContextFactory) throws NamingException {

        try {
            return permUser(username, ldapContextFactory);
        } catch (org.apache.shiro.authc.AuthenticationException ex) {
            return Collections.emptySet();
        }
    }

    private Collection<String> permUser(String username, LdapContext systemLdapCtx)
            throws NamingException {
        final Set<String> permissions = Sets.newLinkedHashSet();
        final NamingEnumeration<SearchResult> searchResultEnum = systemLdapCtx.search(
                searchUserBase, "objectClass=" + userObjectClass, SUBTREE_SCOPE);
        while (searchResultEnum.hasMore()) {
            final SearchResult group = searchResultEnum.next();
            addPermIfFound(group, permissions, userExtractedAttribute, permissionByUserAttribute);
        }
        return permissions;
    }

    private void addPermIfFound(
            SearchResult group, Set<String> permissions,
            Set<String> extractedAttributeP, Set<String> permissionByAttributeP)
                    throws NamingException {
        final NamingEnumeration<? extends Attribute> attributeEnum = group.getAttributes().getAll();
        Map<String, Set<String>> keyValues = Maps.newHashMap();
        while (attributeEnum.hasMore()) {
            final Attribute attr = attributeEnum.next();
            if (extractedAttributeP.contains(attr.getID())) {
                final NamingEnumeration<?> e = attr.getAll();
                keyValues.put(attr.getID(), new HashSet<String>());
                while (e.hasMore()) {
                    String attrValue = e.next().toString();
                    keyValues.get(attr.getID()).add(attrValue);
                }
            }
        }
        for (String permTempl : permissionByAttributeP) {
            for (String key : keyValues.keySet()) {
                if (permTempl.contains("{" + key + "}")) {
                    for (String value : keyValues.get(key)) {
                        permissions.add(permTempl.replaceAll("\\{" + key + "\\}", value));
                    }
                }
            }
        }
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

    private Set<String> rolesFor(final String userName, final LdapContext ldapCtx) throws NamingException {
        final Set<String> roleNames = Sets.newLinkedHashSet();
        final NamingEnumeration<SearchResult> searchResultEnum = ldapCtx.search(searchBase, "objectClass=" + groupObjectClass, SUBTREE_SCOPE);
        while (searchResultEnum.hasMore()) {
            final SearchResult group = searchResultEnum.next();
            addRoleIfMember(userName, group, roleNames);
        }
        return roleNames;
    }

    private void addRoleIfMember(final String userName, final SearchResult group, final Set<String> roleNames) throws NamingException {
        final NamingEnumeration<? extends Attribute> attributeEnum = group.getAttributes().getAll();
        while (attributeEnum.hasMore()) {
            final Attribute attr = attributeEnum.next();
            if (!uniqueMemberAttribute.equalsIgnoreCase(attr.getID())) {
                continue;
            }
            final NamingEnumeration<?> e = attr.getAll();
            while (e.hasMore()) {
                String attrValue = e.next().toString();
                if ((uniqueMemberAttributeValuePrefix + userName + uniqueMemberAttributeValueSuffix).equals(attrValue)) {
                    Attribute attribute = group.getAttributes().get("cn");
                    String groupName = attribute.get().toString();
                    String roleName = roleNameFor(groupName);
                    if (roleName != null) {
                        roleNames.add(roleName);
                    }
                    break;
                }
            }
        }
    }

    private String roleNameFor(String groupName) {
        return !rolesByGroup.isEmpty() ? rolesByGroup.get(groupName) : groupName;
    }

    private Set<String> permsFor(Set<String> roleNames) {
        Set<String> perms = Sets.newLinkedHashSet(); // preserve order
        for (String role : roleNames) {
            List<String> permsForRole = getPermissionsByRole().get(role);
            if (permsForRole != null) {
                perms.addAll(permsForRole);
            }
        }
        return perms;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public void setGroupObjectClass(String groupObjectClassAttribute) {
        this.groupObjectClass = groupObjectClassAttribute;
    }

    public void setUniqueMemberAttribute(String uniqueMemberAttribute) {
        this.uniqueMemberAttribute = uniqueMemberAttribute;
    }

    public void setUniqueMemberAttributeValueTemplate(String template) {
        if (!StringUtils.hasText(template)) {
            String msg = "User DN template cannot be null or empty.";
            throw new IllegalArgumentException(msg);
        }
        int index = template.indexOf(UNIQUEMEMBER_SUBSTITUTION_TOKEN);
        if (index < 0) {
            String msg = "UniqueMember attribute value template must contain the '" +
                    UNIQUEMEMBER_SUBSTITUTION_TOKEN + "' replacement token to understand how to " +
                    "parse the group members.";
            throw new IllegalArgumentException(msg);
        }
        String prefix = template.substring(0, index);
        String suffix = template.substring(prefix.length() + UNIQUEMEMBER_SUBSTITUTION_TOKEN.length());
        this.uniqueMemberAttributeValuePrefix = prefix;
        this.uniqueMemberAttributeValueSuffix = suffix;
    }

    public void setRolesByGroup(Map<String, String> rolesByGroup) {
        this.rolesByGroup.putAll(rolesByGroup);
    }

    /**
     * Retrieves permissions by role set using either
     * {@link #setPermissionsByRole(String)} or {@link #setResourcePath(String)}.
     */
    private Map<String, List<String>> getPermissionsByRole() {
        if (permissionToRoleMapper == null) {
            throw new IllegalStateException("Permissions by role not yet set.");
        }
        return permissionToRoleMapper.getPermissionsByRole();
    }

    /**
     * <pre>
     * ldapRealm.resourcePath=classpath:webapp/myroles.ini
     * </pre>
     * <p/>
     * <p/>
     * where <tt>myroles.ini</tt> is in <tt>src/main/resources/webapp</tt>, and takes the form:
     * <p/>
     * <pre>
     * [roles]
     * user_role = *:ToDoItemsJdo:*:*,\
     *             *:ToDoItem:*:*
     * self-install_role = *:ToDoItemsFixturesService:install:*
     * admin_role = *
     * </pre>
     * <p/>
     * <p/>
     * This 'ini' file can then be referenced by other realms (if multiple realm are configured
     * with the Shiro security manager).
     *
     * @see #setResourcePath(String)
     */
    public void setResourcePath(String resourcePath) {
        if (permissionToRoleMapper != null) {
            throw new IllegalStateException("Permissions already set, " + permissionToRoleMapper.getClass().getName());
        }
        final Ini ini = Ini.fromResourcePath(resourcePath);
        this.permissionToRoleMapper = new PermissionToRoleMapperFromIni(ini);
    }

    /**
     * Specify permissions for each role using a formatted string.
     * <p/>
     * <pre>
     * ldapRealm.permissionsByRole=\
     *    user_role = *:ToDoItemsJdo:*:*,\
     *                *:ToDoItem:*:*; \
     *    self-install_role = *:ToDoItemsFixturesService:install:* ; \
     *    admin_role = *
     * </pre>
     *
     * @see #setResourcePath(String)
     */
    @Deprecated
    public void setPermissionsByRole(String permissionsByRoleStr) {
        if (permissionToRoleMapper != null) {
            throw new IllegalStateException("Permissions already set, " + permissionToRoleMapper.getClass().getName());
        }
        this.permissionToRoleMapper = new PermissionToRoleMapperFromString(permissionsByRoleStr);
    }

    public void setPermissionByUserAttribute(String permissionByUserAttr) {
        String[] list = permissionByUserAttr.split(",");
        this.permissionByUserAttribute.addAll(Lists.newArrayList(list));
    }

    public void setPermissionByGroupAttribute(String permissionByGroupAttribute) {
        String[] list = permissionByGroupAttribute.split(",");
        this.permissionByGroupAttribute.addAll(Lists.newArrayList(list));
    }

    public void setUserExtractedAttribute(String userExtractedAttribute) {
        String[] list = userExtractedAttribute.split(",");
        this.userExtractedAttribute.addAll(Lists.newArrayList(list));
    }

    public void setGroupExtractedAttribute(String groupExtractedAttribute) {
        String[] list = groupExtractedAttribute.split(",");
        this.groupExtractedAttribute.addAll(Lists.newArrayList(list));
    }

    public void setSearchUserBase(String searchUserBase) {
        this.searchUserBase = searchUserBase;
    }

    public void setUserObjectClass(String userObjectClass) {
        this.userObjectClass = userObjectClass;
    }

    public void setCnAttribute(String cnAttribute) {
        this.cnAttribute = cnAttribute;
    }

}
