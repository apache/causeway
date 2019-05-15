package org.apache.isis.core.runtime.system.persistence;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.IsisJdoRuntimePlugin;
import org.apache.isis.core.runtime.system.context.session.AppLifecycleEvent;
import org.apache.isis.core.runtime.system.context.session.SessionLifecycleEvent;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

import lombok.val;

@Singleton
public class JdoPersistenceLifecycleService {

    private final _Probe probe = _Probe.unlimited().label("JdoPersistenceLifecycleService");
    
	private PersistenceSessionFactory persistenceSessionFactory;
	
	@PostConstruct
	public void postConstr() {
	    probe.println("!!! init");
	}

	@EventListener(AppLifecycleEvent.class)
	public void onAppLifecycleEvent(AppLifecycleEvent event) {

		val eventType = event.getEventType(); 
		
		probe.println("received app lifecycle event %s", eventType);

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
		
		probe.println("received session event %s", eventType);

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
