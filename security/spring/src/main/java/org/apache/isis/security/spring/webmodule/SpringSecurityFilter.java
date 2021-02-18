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
package org.apache.isis.security.spring.webmodule;

import java.io.IOException;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.standard.SimpleAuthentication;

import lombok.val;

/**
 * @since 2.0 {@index}
 */
public class SpringSecurityFilter implements Filter {

    @Autowired private InteractionFactory isisInteractionFactory;

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

        //val httpServletRequest = (HttpServletRequest) servletRequest;
        val httpServletResponse = (HttpServletResponse) servletResponse;
        
        val springAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if(springAuthentication==null
                || !springAuthentication.isAuthenticated()) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // not authenticated
        }
        
        val principal = springAuthentication.getPrincipal();
        if(! (principal instanceof AuthenticatedPrincipal)) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // unknown principal type, not handled
        }
        
        val authenticatedPrincipal = (AuthenticatedPrincipal)principal;
        
        val userid = authenticatedPrincipal.getName();
        
//        final String userid = header(httpServletRequest, "X-Auth-Userid");
//        final String rolesHeader = header(httpServletRequest, "X-Auth-Roles");
//        final String subjectHeader = header(httpServletRequest, "X-Auth-Subject");
//        if(userid == null || rolesHeader == null || subjectHeader == null) {
//            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return;
//        }
//        final List<String> roles = toClaims(rolesHeader);
//
        val user = UserMemento.ofNameAndRoleNames(userid, 
                Stream.of("org.apache.isis.viewer.wicket.roles.USER"));
        val authentication = SimpleAuthentication.validOf(user);
                //of(user, subjectHeader);
        authentication.setType(Authentication.Type.EXTERNAL);

        isisInteractionFactory.runAuthenticated(
                authentication,
                ()->{
                        filterChain.doFilter(servletRequest, servletResponse);
                });
    }

//    static List<String> toClaims(final String claimsHeader) {
//        final List<String> roles = asRoles(claimsHeader);
//        roles.add("org.apache.isis.viewer.wicket.roles.USER");
//        return roles;
//    }
//
//    static List<String> asRoles(String claimsHeader) {
//        final List<String> roles = new ArrayList<>();
//        if(claimsHeader != null) {
//            roles.addAll(Arrays.asList(claimsHeader.split(",")));
//        }
//        return roles;
//    }
//
//    private String header(final HttpServletRequest httpServletRequest, final String headerName) {
//        final Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
//        while(headerNames.hasMoreElements()) {
//            final String header = headerNames.nextElement();
//            if(header.toLowerCase().equals(headerName.toLowerCase())) {
//                return httpServletRequest.getHeader(header);
//            }
//        }
//        return null;
//    }
}
