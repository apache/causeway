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
package org.apache.isis.incubator.viewer.vaadin.ui.auth;

import javax.inject.Inject;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.stereotype.Service;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.security.authentication.logout.LogoutHandler;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class LogoutHandlerVaa implements LogoutHandler {

    @Inject private MetaModelContext metaModelContext;

    @Override
    public void logout() {

        val sessionVaa = VaadinSession.getCurrent();
        if(sessionVaa==null) {
            return; // ignore if there is no current session
        }

        AuthSessionStoreUtil.get()
        .ifPresent(auth->{
            log.info("logging out {}", auth.getUserName());
            // logout AuthenticationManager
            metaModelContext.getAuthenticationManager().closeSession(auth);
            AuthSessionStoreUtil.clear();
        });

        sessionVaa.close();

    }

    @Override
    public boolean isHandlingCurrentThread() {
        return VaadinRequest.getCurrent()!=null;
    }



}
