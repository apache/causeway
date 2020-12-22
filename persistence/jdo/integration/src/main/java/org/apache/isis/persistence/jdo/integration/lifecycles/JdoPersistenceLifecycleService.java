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
package org.apache.isis.persistence.jdo.integration.lifecycles;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.events.app.AppLifecycleEvent;
import org.apache.isis.core.runtime.events.iactn.IsisInteractionLifecycleEvent;
import org.apache.isis.core.runtime.iactn.InteractionSession;
import org.apache.isis.persistence.jdo.integration.persistence.IsisPersistenceSessionJdo;
import org.apache.isis.persistence.jdo.integration.persistence.PersistenceSession;
import org.apache.isis.persistence.jdo.integration.persistence.PersistenceSessionFactory;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisJdoDn5.JdoPersistenceLifecycleService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class JdoPersistenceLifecycleService {

    @Inject MetaModelContext metaModelContext;
    @Inject PersistenceSessionFactory persistenceSessionFactory;
    @Inject IsisBeanTypeRegistry isisBeanTypeRegistry;

    @PostConstruct
    public void postConstr() {
        if(log.isDebugEnabled()) {
            log.debug("init entity types {}", 
                    isisBeanTypeRegistry.getEntityTypesJdo());
        }
    }

    @EventListener(AppLifecycleEvent.class)
    public void onAppLifecycleEvent(AppLifecycleEvent event) {

        log.debug("received app lifecycle event {}", event);

        switch (event) {
        case PRE_METAMODEL:
            create();
            break;
        case POST_METAMODEL:
            init();
            break;

        default:
            throw _Exceptions.unmatchedCase(event);
        }

    }

    @EventListener(IsisInteractionLifecycleEvent.class)
    public void onInteractionLifecycleEvent(IsisInteractionLifecycleEvent event) {

        val eventType = event.getEventType();
        val interactionSession = event.getInteractionSession();

        if(log.isDebugEnabled()) {
            log.debug("received session event {}", eventType);
        }

        switch (eventType) {
        case HAS_STARTED:
            onInteractionStarted(interactionSession);
            break;
        case IS_ENDING:
            onInteractionEnding(interactionSession);
            break;
        case FLUSH_REQUEST:
            onInteractionFlushRequest(interactionSession);
            break;

        default:
            throw _Exceptions.unmatchedCase(eventType);
        }

    }

    // -- HELPER

    private void onInteractionStarted(final InteractionSession interactionSession) {
        val persistenceSession =
                persistenceSessionFactory.createPersistenceSession();
        interactionSession.putAttribute(IsisPersistenceSessionJdo.class, persistenceSession);
        persistenceSession.open();
    }

    private void onInteractionEnding(final InteractionSession interactionSession) {
        currentSession(interactionSession)
        .ifPresent(PersistenceSession::close);
    }

    private void onInteractionFlushRequest(final InteractionSession interactionSession) {
        currentSession(interactionSession)
        .ifPresent(PersistenceSession::flush);
    }

    private Optional<IsisPersistenceSessionJdo> currentSession(final InteractionSession interactionSession) {
        return Optional.ofNullable(interactionSession)
                .map(session->session.getAttribute(IsisPersistenceSessionJdo.class));
    }
    
    private void create() {
        persistenceSessionFactory.init(metaModelContext);
    }

    private void init() {
        persistenceSessionFactory.catalogNamedQueries();
    }


}
