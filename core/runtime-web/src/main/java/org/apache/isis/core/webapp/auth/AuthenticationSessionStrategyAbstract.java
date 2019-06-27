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
package org.apache.isis.core.webapp.auth;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.manager.AuthenticationManager;

public abstract class AuthenticationSessionStrategyAbstract implements AuthenticationSessionStrategy {

    public static final int STATUS_UNAUTHORIZED = 401;

    protected HttpSession getHttpSession(final ServletRequest servletRequest) {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        return httpServletRequest.getSession();
    }

    protected ServletContext getServletContext(final ServletRequest servletRequest) {
        final HttpSession httpSession = getHttpSession(servletRequest);
        return httpSession.getServletContext();
    }

    @Override
    public void bind(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final AuthenticationSession authSession) {
        // no-op
    }

    @Override
    public void invalidate(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {
        bind(httpServletRequest, httpServletResponse, null);
        httpServletResponse.setStatus(STATUS_UNAUTHORIZED);
    }


    // -- Dependencies (from request)
    protected AuthenticationManager authenticationManagerFrom(final HttpServletRequest httpServletRequest) {
        return isisSessionFactoryFrom(httpServletRequest).getAuthenticationManager();
    }

    // TODO
    protected IsisSessionFactory isisSessionFactoryFrom(final HttpServletRequest httpServletRequest) {
        return IsisContext.getSessionFactory();
    }



}
