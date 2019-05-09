package org.apache.isis.core.runtime.system.persistence;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.IsisJdoRuntimePlugin;
import org.apache.isis.core.runtime.system.context.session.AppLifecycleEvent;
import org.apache.isis.core.runtime.system.context.session.SessionLifecycleEvent;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

import lombok.val;

@Singleton
public class JdoPersistenceLifecycleService {

	private PersistenceSessionFactory persistenceSessionFactory;

	@EventListener
	public void onAppLifecycleEvent(@Observes AppLifecycleEvent event) {

		val eventType = event.getEventType(); 

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

	@EventListener
	public void onSessionLifecycleEvent(@Observes SessionLifecycleEvent event) {

		val eventType = event.getEventType(); 

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
	
	@Bean @Produces @Singleton //XXX note: the resulting singleton is not life-cycle managed by Spring/CDI, nor are InjectionPoints resolved by Spring/CDI
	public PersistenceSessionFactory producePersistenceSessionFactory() {
		return persistenceSessionFactory;
	}

	// -- HELPER

	private void openSession(IsisSession isisSession) {
		val authenticationSession = isisSession.getAuthenticationSession();
		val persistenceSession =
				persistenceSessionFactory.createPersistenceSession(authenticationSession);
		persistenceSession.open();

		//TODO [2033] only to support IsisSessionFactoryDefault
		_Context.threadLocalPut(PersistenceSession.class, persistenceSession); 
	}

	private void closeSession() {
		PersistenceSession.current(PersistenceSession.class)
                .getSingleton()
                .ifPresent(PersistenceSession::close);
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
