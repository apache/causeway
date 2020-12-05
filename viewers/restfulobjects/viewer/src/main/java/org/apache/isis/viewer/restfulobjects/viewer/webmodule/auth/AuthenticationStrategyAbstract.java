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
package org.apache.isis.viewer.restfulobjects.viewer.webmodule.auth;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.support.WebApplicationContextUtils;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;

import lombok.val;

public abstract class AuthenticationStrategyAbstract implements AuthenticationStrategy {

    public static final int STATUS_UNAUTHORIZED = 401;
    
    private AuthenticationManager authenticationManager;
    
    protected AuthenticationManager getAuthenticationManager(ServletRequest servletRequest) {
        if(authenticationManager==null) {
            val servletContext = getServletContext(servletRequest);
            val webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            if(webApplicationContext==null) {
                throw _Exceptions.illegalState("Requires a WebApplicationContext (Spring).");
            }
            authenticationManager = webApplicationContext.getBean(AuthenticationManager.class);
        }
        return authenticationManager;
    }

    protected HttpSession getHttpSession(ServletRequest servletRequest) {
        val httpServletRequest = (HttpServletRequest) servletRequest;
        return httpServletRequest.getSession();
    }

    protected ServletContext getServletContext(ServletRequest servletRequest) {
        return servletRequest.getServletContext();
    }

    
    @Override
    public final void invalidate(
            final HttpServletRequest httpServletRequest, 
            final HttpServletResponse httpServletResponse) {
        
        bind(httpServletRequest, httpServletResponse, null);
        httpServletResponse.setStatus(STATUS_UNAUTHORIZED);
    }
    
}
