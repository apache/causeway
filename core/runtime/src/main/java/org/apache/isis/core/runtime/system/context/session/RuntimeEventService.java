package org.apache.isis.core.runtime.system.context.session;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.events.ui.CssClassUiEvent;
import org.apache.isis.applib.fixturescripts.events.FixturesInstalledEvent;
import org.apache.isis.applib.fixturescripts.events.FixturesInstallingEvent;
import org.apache.isis.core.runtime.system.session.IsisSession;

import lombok.Getter;
import lombok.Value;

/**
 * 
 * @since 2.0.0-M3
 *
 */
@Singleton
public class RuntimeEventService {

	@Inject Event<AppLifecycleEvent> appLifecycleEvents;
	@Inject Event<SessionLifecycleEvent> sessionLifecycleEvents;
	@Inject Event<FixturesInstallingEvent> fixturesInstallingEvents;
    @Inject Event<FixturesInstalledEvent> fixturesInstalledEvents;
	
	// -- APP
	
	public void fireAppPreMetamodel() {
		appLifecycleEvents.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreMetamodel));
	}
	
	public void fireAppPostMetamodel() {
		appLifecycleEvents.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPostMetamodel));
	}
	
	public void fireAppPreDestroy() {
		appLifecycleEvents.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreDestroy));
	}
	
	// -- SESSION
	
	public void fireSessionOpened(IsisSession session) {
		sessionLifecycleEvents.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionOpened));
	}
	
	public void fireSessionClosing(IsisSession session) {
		sessionLifecycleEvents.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
	}
	
//	public void fireSessionFlushing(IsisSession session) {
//		sessionLifecycleEvent.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
//	}

	// -- FIXTURES
	
	public void fireFixturesInstalling(FixturesInstallingEvent fixturesInstallingEvent) {
		fixturesInstallingEvents.fire(fixturesInstallingEvent);
	}

	public void fireFixturesInstalled(FixturesInstalledEvent fixturesInstalledEvent) {
		fixturesInstalledEvents.fire(fixturesInstalledEvent);
	}
	
	// -- METAMODEL EVENTS
	
	public void fireCssClassUiEvent(CssClassUiEvent<Object> cssClassUiEvent) {
		// TODO Auto-generated method stub
		
	}
	
	// -- APP EVENT CLASSES
	
	@Value(staticConstructor="of")
	public static class AppLifecycleEvent {

		public static enum EventType {
			appPreMetamodel,
			appPostMetamodel,
			appPreDestroy,
		}
		
		@Getter EventType eventType;
		
	}

	



	
	
	

}
