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
package org.apache.isis.runtime.system.context.session;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.runtime.system.persistence.JdoPersistenceLifecycleService;
import org.apache.isis.runtime.system.session.IsisSession;

/**
 * 
 * @since 2.0
 * @implNote listeners to runtime events are hard-wired, because these events are already fired 
 * during bootstrapping, when event handling might not work properly yet.   
 */
@Service
public class RuntimeEventService {

    @Inject JdoPersistenceLifecycleService listener; // dependsOn

    @Inject Event<AppLifecycleEvent> appLifecycleEvents;
    @Inject Event<SessionLifecycleEvent> sessionLifecycleEvents;

    // -- APP

    public void fireAppPreMetamodel() {
        //appLifecycleEvents.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreMetamodel));
        listener.onAppLifecycleEvent(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreMetamodel));
    }

    public void fireAppPostMetamodel() {
        //appLifecycleEvents.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPostMetamodel));
        listener.onAppLifecycleEvent(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPostMetamodel));
    }

    public void fireAppPreDestroy() {
        //appLifecycleEvents.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreDestroy));
        listener.onAppLifecycleEvent(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreDestroy));
    }

    // -- SESSION

    public void fireSessionOpened(IsisSession session) {
        //sessionLifecycleEvents.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionOpened));
        listener.onSessionLifecycleEvent(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionOpened));
    }

    public void fireSessionClosing(IsisSession session) {
        //sessionLifecycleEvents.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
        listener.onSessionLifecycleEvent(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
    }

    //	public void fireSessionFlushing(IsisSession session) {
    //		sessionLifecycleEvent.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
    //	}


}
