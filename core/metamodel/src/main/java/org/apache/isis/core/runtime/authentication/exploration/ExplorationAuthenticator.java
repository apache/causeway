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

package org.apache.isis.core.runtime.authentication.exploration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.standard.AuthenticatorAbstract;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;

/**
 * Creates a session suitable for {@link org.apache.isis.core.metamodel.deployment.DeploymentCategory#PROTOTYPING}
 * mode.
 *
 * <p>
 * The format of the {@value ExplorationAuthenticatorConstants#USERS} key should
 * be:
 *
 * <pre>
 * &lt;:userName&gt; [:&lt;role&gt;[|&lt;role&gt;]...], &lt;userName&gt;...
 * </pre>
 * @deprecated no longer supported
 */
@Deprecated
public class ExplorationAuthenticator extends AuthenticatorAbstract {

    // -- Constructor, fields
    private final Set<SimpleSession> registeredSessions = new LinkedHashSet<SimpleSession>();;
    private final String users;


    public ExplorationAuthenticator(final IsisConfiguration configuration) {
        super(configuration);
        users = getConfiguration().getString(ExplorationAuthenticatorConstants.USERS);
        if (users != null) {
            registeredSessions.addAll(parseUsers(users));
        }
    }



    private List<SimpleSession> parseUsers(final String users) {
        final List<SimpleSession> registeredUsers = new ArrayList<SimpleSession>();

        final StringTokenizer st = new StringTokenizer(users, ",");
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            final int end = token.indexOf(':');
            final List<String> roles = new ArrayList<String>();
            final String userName;
            if (end == -1) {
                userName = token.trim();
            } else {
                userName = token.substring(0, end).trim();
                final String roleList = token.substring(end + 1);
                final StringTokenizer st2 = new StringTokenizer(roleList, "|");
                while (st2.hasMoreTokens()) {
                    final String role = st2.nextToken().trim();
                    roles.add(role);
                }
            }
            registeredUsers.add(createSimpleSession(userName, roles));
        }
        return registeredUsers;
    }

    private SimpleSession createSimpleSession(final String userName, final List<String> roles) {
        return new SimpleSession(userName, roles.toArray(new String[roles.size()]));
    }

    // //////////////////////////////////////////////////////////////////
    // API
    // //////////////////////////////////////////////////////////////////

    /**
     * Can authenticate if a {@link AuthenticationRequestExploration}.
     */
    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return AuthenticationRequestExploration.class.isAssignableFrom(authenticationRequestClass);
    }

    @Override
    protected final boolean isValid(final AuthenticationRequest request) {
        return false; //was true only for deprecated exploring mode
    }

    @Override
    public AuthenticationSession authenticate(final AuthenticationRequest request, final String code) {
        final AuthenticationRequestExploration authenticationRequestExploration = (AuthenticationRequestExploration) request;
        if (!authenticationRequestExploration.isDefaultUser()) {
            registeredSessions.add(createSimpleSession(authenticationRequestExploration.getName(), authenticationRequestExploration.getRoles()));
        }
        if (registeredSessions.size() >= 1) {
            return registeredSessions.iterator().next();
        } else {
            return new ExplorationSession(code);
        }
    }

}
