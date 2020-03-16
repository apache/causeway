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
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.context._Context;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.context.session.AppLifecycleEvent;
import org.apache.isis.core.runtime.context.session.IsisSessionLifecycleEvent;
import org.apache.isis.core.runtime.persistence.session.PersistenceSession;
import org.apache.isis.core.runtime.persistence.session.PersistenceSessionFactory;
import org.apache.isis.core.runtime.session.IsisSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisJdoDn5.JdoPersistenceLifecycleService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
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

    @EventListener(IsisSessionLifecycleEvent.class)
    public void onSessionLifecycleEvent(IsisSessionLifecycleEvent event) {

        val eventType = event.getEventType();

        if(log.isDebugEnabled()) {
            log.debug("received session event {}", eventType);
        }

        switch (eventType) {
        case OPENED:
            openSession(event.getIsisSession());
            break;
        case CLOSING:
            closeSession(event.getIsisSession());
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

        val persistenceSession =
                persistenceSessionFactory.createPersistenceSession();

        // to support static call of PersistenceSession.current(PersistenceSession.class)

        // TODO: review - rather than using a thread-local, and alternative might be to have
        //  IsisSession provide a "userData" map to allow arbitrary session-scoped objects to be stored there...
        //  ... of which PersistenceSession is one (the other is IsisTransactionObject).
        //  Then, only IsisSessionFactory needs to maintain a thread-local (and if we change to some other way of
        //  finding the current IsisSession, eg from HttpRequest, then there's no impact elsewhere).

        _Context.threadLocalPut(PersistenceSession.class, persistenceSession);

        persistenceSession.open();
    }

    private void closeSession(IsisSession isisSession) {
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
