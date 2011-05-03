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
package org.apache.isis.viewer.restful.viewer.authentication;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.webapp.auth.AuthenticationSessionLookupStrategyDefault;

public class AuthenticationSessionLookupStrategyParams extends AuthenticationSessionLookupStrategyDefault {

    @Override
    public AuthenticationSession lookup(final ServletRequest servletRequest, final ServletResponse servletResponse) {
        final AuthenticationSession session = super.lookup(servletRequest, servletResponse);
        if (session != null) {
            return session;
        }

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final String user = httpServletRequest.getParameter("user");
        final String password = httpServletRequest.getParameter("password");

        if (user == null || password == null) {
            return null;
        }
        final AuthenticationRequestPassword request = new AuthenticationRequestPassword(user, password);
        return IsisContext.getAuthenticationManager().authenticate(request);
    }
}
