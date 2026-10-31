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
package org.apache.causeway.core.webapp.session;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.session.SessionSubscriber;

@Service
@Named("causeway.webapp.SessionEventListener")
public class SessionEventListener{

    private final List<SessionSubscriber> sessionSubscribers;
    private final Map<String, UUID> sessionGuids = new ConcurrentHashMap<>();

    public SessionEventListener(final List<SessionSubscriber> sessionSubscribers) {
        this.sessionSubscribers = sessionSubscribers != null
    		? sessionSubscribers
			: List.of();
    }

    /**
     * Handle login via Spring Security authentication success
     */
    @EventListener
    public void onAuthenticationSuccess(final AuthenticationSuccessEvent event) {
    	final HttpSession session = SessionManagerDefault.httpSession().orElse(null);
        if (session == null)
        	return;

        String httpSessionId = session.getId();
        String username = event.getAuthentication().getName();
        UUID sessionGuid = UUID.randomUUID();

        sessionGuids.put(httpSessionId, sessionGuid);

        notifySubscribers(
                SessionSubscriber.Type.LOGIN,
                username,
                new Date(),
                SessionSubscriber.CausedBy.USER,
                sessionGuid,
                httpSessionId
        );
    }

    /**
     * Handle logout via Spring Security logout success
     */
    @EventListener
    public void onLogoutSuccess(final LogoutSuccessEvent event) {
        // Extract username and session ID from the event
        Authentication auth = event.getAuthentication();
        String username = auth != null ? auth.getName() : "UNKNOWN";

        // Note: LogoutSuccessEvent doesn't provide HttpSession directly
        // You may need to use a logout handler for this
        System.out.println("User logged out: " + username);
    }

    // -- HELPER

    private void notifySubscribers(
            final SessionSubscriber.Type type,
            final String username,
            final Date date,
            final SessionSubscriber.CausedBy causedBy,
            final UUID sessionGuid,
            final String httpSessionId) {

        for (SessionSubscriber subscriber : sessionSubscribers) {
            try {
                subscriber.log(type, username, date, causedBy, sessionGuid, httpSessionId);
            } catch (Exception e) {
                System.err.println("Error notifying SessionSubscriber: " + e.getMessage());
            }
        }
    }

}
