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
package org.apache.isis.incubator.viewer.vaadin.viewer;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.spring.SpringServlet;

import org.springframework.context.ApplicationContext;

import org.apache.isis.core.runtime.session.IsisInteraction;
import org.apache.isis.core.runtime.session.IsisInteractionFactory;
import org.apache.isis.incubator.viewer.vaadin.ui.auth.AuthSessionStoreUtil;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * An extension of {@link SpringServlet} to support {@link IsisInteraction} life-cycle management.
 * @since Mar 14, 2020
 *
 */
@Log4j2
public class IsisServletForVaadin 
extends SpringServlet {

    private static final long serialVersionUID = 1L;
    
    private final IsisInteractionFactory isisInteractionFactory;

    public IsisServletForVaadin(
            @NonNull final IsisInteractionFactory isisInteractionFactory, 
            @NonNull final ApplicationContext context, 
            final boolean forwardingEnforced) {
        super(context, forwardingEnforced);
        this.isisInteractionFactory = isisInteractionFactory;
    }
   
    
    @Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        
        val authSession = AuthSessionStoreUtil.get(request.getSession(true))
                .orElse(null);
        
        log.debug("new request incoming (authentication={})", authSession);
        
        if(authSession!=null) {
            isisInteractionFactory.runAuthenticated(authSession, ()->{
                super.service(request, response);                
            });
        } else {
            // do not open an IsisSession, instead redirect to login page
            // this should happen afterwards by means of the VaadinAuthenticationHandler
            
            super.service(request, response);    
        }
    }
  

    
}
