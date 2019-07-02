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
package org.apache.isis.runtime.system.persistence;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.runtime.persistence.IsisJdoRuntimePlugin;
import org.apache.isis.runtime.system.context.session.AppLifecycleEvent;
import org.apache.isis.runtime.system.context.session.SessionLifecycleEvent;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Singleton @Log4j2
public class JdoPersistenceLifecycleService {

	private PersistenceSessionFactory persistenceSessionFactory;
	
	@PostConstruct
	public void postConstr() {
		if(log.isDebugEnabled()) {
			log.debug("init entity types {}", IsisBeanTypeRegistry.current().getEntityTypes());
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
		case appPreDestroy:
			shutdown();
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
	
	@Bean @Singleton //XXX note: the resulting singleton is not life-cycle managed by Spring/CDI, nor are InjectionPoints resolved by Spring/CDI
	public PersistenceSessionFactory producePersistenceSessionFactory() {
		return persistenceSessionFactory;
	}

	// -- HELPER

	private void openSession(IsisSession isisSession) {
		val authenticationSession = isisSession.getAuthenticationSession();
		val persistenceSession =
				persistenceSessionFactory.createPersistenceSession(authenticationSession);

	      //TODO [2033] only to support IsisSessionFactoryDefault
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
		persistenceSessionFactory = 
				IsisJdoRuntimePlugin.get().getPersistenceSessionFactory();
		persistenceSessionFactory.init();
	}
	
	private void init() {
		//TODO [2033] specloader should rather be a Spring/CDI managed object
		val isisSessionFactory = _Context.getElseFail(IsisSessionFactory.class); 
		val specificationLoader = isisSessionFactory.getSpecificationLoader();
		_With.requires(specificationLoader, "specificationLoader");
		persistenceSessionFactory.catalogNamedQueries(specificationLoader);
	}

	private void shutdown() {
		if(persistenceSessionFactory!=null) {
			persistenceSessionFactory.shutdown();	
		}
	}

}
