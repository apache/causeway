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

package org.apache.isis.security.ldap.authorization;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.runtime.authorization.standard.AuthorizorAbstract;

public class LdapAuthorizor extends AuthorizorAbstract {

    private static final Logger LOG = Logger.getLogger(LdapAuthorizor.class);

    private static final String FILTER = "(&(uniquemember={0}) (|(cn={1}) (cn={2}) (cn={3})))";
    private static final String ACCESS_QUALIFIER_RW = "RW";

    private final String ldapProvider;
    @SuppressWarnings("unused")
    private final String ldapDn;
    private final String appDn;
    private final boolean learn;

    public LdapAuthorizor(final IsisConfiguration configuration) {
        super(configuration);

        ldapProvider = getConfiguration().getString(LdapAuthorizationConstants.SERVER_KEY);
        ldapDn = getConfiguration().getString(LdapAuthorizationConstants.LDAPDN_KEY);
        appDn = getConfiguration().getString(LdapAuthorizationConstants.APP_DN_KEY);
        learn = getConfiguration().getBoolean(LdapAuthorizationConstants.LEARN_KEY, LdapAuthorizationConstants.LEARN_DEFAULT);
    }

    // ////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
        // do nothing
    }

    // ////////////////////////////////////////////////////////
    // API
    // ////////////////////////////////////////////////////////

    @Override
    public boolean isUsableInRole(final String role, final Identifier member) {
        if(role == null) {
            return false;
        }
        return isAuthorised(role, member, ACCESS_QUALIFIER_RW);
    }

    @Override
    public boolean isVisibleInRole(final String role, final Identifier member) {
        if(role == null) {
            return false;
        }
        return isAuthorised(role, member, null);
    }

    private boolean isAuthorised(final String role, final Identifier member, final String flag) {

        final Hashtable<String, String> env = new Hashtable<String, String>(4);
        env.put(Context.INITIAL_CONTEXT_FACTORY, LdapAuthorizationConstants.SERVER_DEFAULT);
        env.put(Context.PROVIDER_URL, ldapProvider);

        if (learn) {
            env.put(Context.SECURITY_PRINCIPAL, "uid=admin, ou=system");
            env.put(Context.SECURITY_CREDENTIALS, "secret");
        }

        DirContext authContext = null;
        try {
            authContext = new InitialDirContext(env);
            if (learn) {
                return bindNames(authContext, role, member);
            }
            return isPermitted(authContext, role, member, flag);
        } catch (final AuthenticationException e) {
            throw new IsisException("Failed to authorise using LDAP", e);
        } catch (final NameNotFoundException e) {
            // missing class in ldap server - treat as authorisation failure
            LOG.error(e);
            return false;
        } catch (final NamingException e) {
            throw new IsisException("Failed to authorise using LDAP", e);
        } finally {
            try {
                if (authContext != null) {
                    authContext.close();
                }
            } catch (final NamingException e) {
                throw new IsisException("Failed to authorise using LDAP", e);
            }
        }
    }

    private boolean isPermitted(final DirContext authContext, final String role, final Identifier member, final String flag) throws NamingException {
        final String cls = member.toIdentityString(Identifier.CLASS);
        final String name = member.toIdentityString(Identifier.MEMBERNAME_ONLY);
        final String parms = member.toIdentityString(Identifier.PARAMETERS_ONLY);

        final Object[] args = new Object[] { role, cls, name, parms };
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        final String searchName = buildSearchName(cls, appDn);
        final NamingEnumeration<SearchResult> answer = authContext.search(searchName, FILTER, args, controls);
        while (answer.hasMore()) {
            // if we have a class match must be OK
            // if we have a name match must be OK (parent must be class by
            // definition)
            // but parm matches need to check that parent = name
            final SearchResult result = answer.nextElement();
            final String cn = (String) result.getAttributes().get("cn").get(0);
            // result.getname gives relative path from class - so if contains
            // 'name' it is parent of parms
            // entry
            if (cn.equals(cls) || cn.equals(name) || ((cn.equals(parms) && result.getName().contains(name)))) {
                // last check if there is a flag attribute
                if (flag != null) {
                    final Attribute flagAttribute = result.getAttributes().get("flag");
                    if (flagAttribute != null) {
                        // since there is a flag need to check is match
                        return flag.equalsIgnoreCase((String) flagAttribute.get(0));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private String buildSearchName(final String cls, final String appDn) {
        final StringBuffer search = new StringBuffer();
        search.append("cn=").append(cls).append(", ").append(appDn);
        final String searchName = search.toString();
        return searchName;
    }

    private Attributes createCommonAttributes(final String cnName, final String role, final boolean isClass) {
        final Attributes attrs = new BasicAttributes(true); // case-ignore
        final Attribute objclass = new BasicAttribute("objectclass");
        objclass.add("top");
        objclass.add("javaContainer");
        objclass.add("groupOfUniqueNames");
        if (isClass) {
            objclass.add("javaObject");
        }
        final Attribute cn = new BasicAttribute("cn");
        cn.add(cnName);
        final Attribute uniqueMember = new BasicAttribute("uniquemember");
        uniqueMember.add(role);
        if (isClass) {
            final Attribute javaClass = new BasicAttribute("javaclassname");
            javaClass.add(cnName);
            attrs.put(javaClass);
        }
        attrs.put(objclass);
        attrs.put(cn);
        attrs.put(uniqueMember);
        return attrs;
    }

    private String createClassBindname(final String cls) {
        final StringBuffer bindName = new StringBuffer();
        bindName.append("cn=").append(cls).append(", ").append(appDn);
        return bindName.toString();
    }

    private void bindClass(final DirContext authContext, final String role, final Identifier member) throws NamingException {
        final String cls = member.toIdentityString(Identifier.CLASS);
        final Attributes attrs = createCommonAttributes(cls, role, true);
        try {
            authContext.createSubcontext(createClassBindname(cls), attrs);
        } catch (final NameAlreadyBoundException e) {
            // ignore as this is just debug code
            // and we don't check if this is already bound first
            LOG.debug(e);
        }
    }

    private String createNameBindname(final String cls, final String name) {
        final StringBuffer bindName = new StringBuffer();
        bindName.append("cn=").append(name).append(", ");
        bindName.append(createClassBindname(cls));
        return bindName.toString();
    }

    private void bindName(final DirContext authContext, final String role, final Identifier member) throws NamingException {
        final String cls = member.toIdentityString(Identifier.CLASS);
        final String name = member.toIdentityString(Identifier.MEMBERNAME_ONLY);
        final Attributes attrs = createCommonAttributes(name, role, false);
        try {
            authContext.createSubcontext(createNameBindname(cls, name), attrs);
        } catch (final NameAlreadyBoundException e) {
            // ignore as this is just debug code
            // and we don't check if this is already bound first
            LOG.debug(e);
        }
    }

    private String createParmsBindname(final String cls, final String name, final String parms) {
        final StringBuffer bindName = new StringBuffer();
        bindName.append("cn=").append(parms).append(", ");
        bindName.append(createNameBindname(cls, name));
        return bindName.toString();
    }

    private void bindParms(final DirContext authContext, final String role, final Identifier member) throws NamingException {
        final String cls = member.toIdentityString(Identifier.CLASS);
        final String name = member.toIdentityString(Identifier.MEMBERNAME_ONLY);
        // have to escape any commas in parms string or ldap parser is not happy
        final String parms = member.toIdentityString(Identifier.PARAMETERS_ONLY).replace(",", "\\,");
        if (parms.length() == 0) {
            return;
        }
        final Attributes attrs = createCommonAttributes(parms, role, false);
        try {
            authContext.createSubcontext(createParmsBindname(cls, name, parms), attrs);
        } catch (final NameAlreadyBoundException e) {
            // ignore as this is just debug code
            // and we don't check if this is already bound first
            LOG.debug(e);
        }
    }

    private boolean bindNames(final DirContext authContext, final String role, final Identifier member) throws NamingException {
        bindClass(authContext, role, member);
        bindName(authContext, role, member);
        bindParms(authContext, role, member);
        return true;
    }

}
