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
package org.apache.isis.security.keycloak.webmodule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.standard.SimpleSession;

import lombok.val;

public class KeycloakFilter implements Filter {
    
    @Autowired private IsisSessionFactory isisSessionFactory;

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        final String userid = header(httpServletRequest, "X-Auth-Userid");
        final String rolesHeader = header(httpServletRequest, "X-Auth-Roles");
        final String subjectHeader = header(httpServletRequest, "X-Auth-Subject");
        if(userid == null || rolesHeader == null || subjectHeader == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        final List<String> roles = toClaims(rolesHeader);
        
        val authenticationSession = new SimpleSession(userid, roles, subjectHeader);
        authenticationSession.setType(AuthenticationSession.Type.EXTERNAL);
        
        isisSessionFactory.runAuthenticated(
                authenticationSession,
                ()->{
                        filterChain.doFilter(servletRequest, servletResponse);
                });
    }

    static List<String> toClaims(final String claimsHeader) {
        final List<String> roles = asRoles(claimsHeader);
        roles.add("org.apache.isis.viewer.wicket.roles.USER");
        return roles;
    }

    static List<String> asRoles(String claimsHeader) {
        final List<String> roles = new ArrayList<>();
        if(claimsHeader != null) {
            roles.addAll(Arrays.asList(claimsHeader.split(",")));
        }
        return roles;
    }

    private String header(final HttpServletRequest httpServletRequest, final String headerName) {
        final Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            final String header = headerNames.nextElement();
            if(header.toLowerCase().equals(headerName.toLowerCase())) {
                return httpServletRequest.getHeader(header);
            }
        }
        return null;
    }
}
