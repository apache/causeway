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

package org.apache.isis.security.ldap.authentication;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.authentication.standard.AuthenticatorAbstract;

public class LdapAuthenticator extends AuthenticatorAbstract {

    private static final Logger LOG = Logger.getLogger(LdapAuthenticator.class);

    private final String ldapProvider;
    private final String ldapDn;

    public LdapAuthenticator(final IsisConfiguration configuration) {
        super(configuration);
        ldapProvider = getConfiguration().getString(LdapAuthenticationConstants.SERVER_KEY);
        ldapDn = getConfiguration().getString(LdapAuthenticationConstants.LDAPDN_KEY);
    }

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return AuthenticationRequestPassword.class.isAssignableFrom(authenticationRequestClass);
    }

    private void setRoles(final DirContext authContext, final AuthenticationRequest request, final String username) throws NamingException {
        final List<String> roles = new ArrayList<String>();
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(new String[] { "cn" });
        final String name = "uid=" + username + ", " + ldapDn;
        final NamingEnumeration<SearchResult> answer = authContext.search(name, LdapAuthenticationConstants.FILTER, controls);
        while (answer.hasMore()) {
            final SearchResult result = answer.nextElement();
            final String roleName = (String) result.getAttributes().get("cn").get(0);
            roles.add(roleName);
            LOG.debug("Adding role: " + roleName);
        }
        request.setRoles(roles);
    }

    @Override
    public boolean isValid(final AuthenticationRequest request) {
        final AuthenticationRequestPassword passwordRequest = (AuthenticationRequestPassword) request;
        final String username = passwordRequest.getName();
        Assert.assertNotNull(username);
        if (username.equals("")) {
            LOG.debug("empty username");
            return false; // failed authentication
        }
        final String password = passwordRequest.getPassword();
        Assert.assertNotNull(password);

        final Hashtable<String, String> env = new Hashtable<String, String>(4);
        env.put(Context.INITIAL_CONTEXT_FACTORY, LdapAuthenticationConstants.SERVER_DEFAULT);
        env.put(Context.PROVIDER_URL, ldapProvider);
        env.put(Context.SECURITY_PRINCIPAL, "uid=" + username + ", " + ldapDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext authContext = null;
        try {
            authContext = new InitialDirContext(env);
            setRoles(authContext, request, username);
            return true;
        } catch (final AuthenticationException e) {
            return false;
        } catch (final NamingException e) {
            throw new IsisException("Failed to authenticate using LDAP", e);
        } finally {
            try {
                if (authContext != null) {
                    authContext.close();
                }
            } catch (final NamingException e) {
                throw new IsisException("Failed to authenticate using LDAP", e);
            }
        }
    }

}
