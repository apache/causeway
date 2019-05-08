package org.apache.isis.core.runtime.system.context.session;

import org.apache.isis.core.runtime.system.session.IsisSession;

import lombok.Getter;
import lombok.Value;

@Value(staticConstructor="of")
public class SessionLifecycleEvent {

	public static enum EventType {
		
		sessionOpened,
		sessionClosing,
		//sessionFlushing,
	}
	
	@Getter IsisSession session;
	@Getter EventType eventType;
	
}
