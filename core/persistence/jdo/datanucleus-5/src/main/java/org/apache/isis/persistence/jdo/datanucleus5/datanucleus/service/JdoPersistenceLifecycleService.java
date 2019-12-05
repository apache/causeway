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
package org.apache.isis.persistence.jdo.datanucleus5.datanucleus.service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.runtime.system.context.session.AppLifecycleEvent;
import org.apache.isis.runtime.system.context.session.SessionLifecycleEvent;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.system.session.IsisSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class JdoPersistenceLifecycleService {

    @Inject private MetaModelContext metaModelContext;
    @Inject private PersistenceSessionFactory persistenceSessionFactory;
    @Inject private IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;

    @PostConstruct
    public void postConstr() {
        if(log.isDebugEnabled()) {
            log.debug("init entity types {}", 
                    isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry().getEntityTypes());
        }
    }

    @EventListener(AppLifecycleEvent.class)
    public void onAppLifecycleEvent(AppLifecycleEvent event) {

        val eventType = event.getEventType(); 

        log.debug("received app lifecycle event {}", eventType);

        switch (eventType) {
        case appPreMetamodel:
            create();
            break;
        case appPostMetamodel:
            init();
            break;

        default:
            throw _Exceptions.unmatchedCase(eventType);
        }

    }

    @EventListener(SessionLifecycleEvent.class)
    public void onSessionLifecycleEvent(SessionLifecycleEvent event) {

        val eventType = event.getEventType();

        if(log.isDebugEnabled()) {
            log.debug("received session event {}", eventType);
        }

        switch (eventType) {
        case sessionOpened:
            openSession(event.getSession());
            break;
        case sessionClosing:
            closeSession();
            break;
            //		case sessionFlushing:
            //			flushSession();
            //			break;

        default:
            throw _Exceptions.unmatchedCase(eventType);
        }

    }

    // -- HELPER

    private void openSession(IsisSession isisSession) {
        val authenticationSession = isisSession.getAuthenticationSession();
        val persistenceSession =
                persistenceSessionFactory.createPersistenceSession(authenticationSession);

        // to support static call of PersistenceSession.current(PersistenceSession.class)
        _Context.threadLocalPut(PersistenceSession.class, persistenceSession);

        persistenceSession.open();
    }

    private void closeSession() {
        PersistenceSession.current(PersistenceSession.class)
        .getSingleton()
        .ifPresent(PersistenceSession::close);
        _Context.threadLocalClear(PersistenceSession.class);
    }

    //	private void flushSession() {
    //		val persistenceSession = PersistenceSessionJdo.current();
    //		
    //		if(persistenceSession != null) {
    //			persistenceSession.flush();
    //		}
    //	}

    private void create() {
        persistenceSessionFactory.init(metaModelContext);
    }

    private void init() {
        persistenceSessionFactory.catalogNamedQueries();
    }


}
