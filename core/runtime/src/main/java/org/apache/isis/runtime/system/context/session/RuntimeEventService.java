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

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.runtime.system.session.IsisSession;

/**
 * 
 * @since 2.0
 * @implNote Listeners to runtime events can only reliably receive these after the 
 * post-construct phase has finished and before the pre-destroy phase has begin.
 */
@Service
//@Log4j2
public class RuntimeEventService {
    
    @Inject private EventBusService eventBusService;  

   // -- APP

    public void fireAppPreMetamodel() {
        eventBusService.post(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreMetamodel));
    }

    public void fireAppPostMetamodel() {
        eventBusService.post(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPostMetamodel));
    }

//    public void fireAppPreDestroy() {
//        try {
//            eventBusService.post(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreDestroy));
//        } catch(BeanCreationNotAllowedException ex) {
//           log.warn("Unable to post event - ignoring", ex);
//        }
//    }

    // -- SESSION

    public void fireSessionOpened(IsisSession session) {
        eventBusService.post(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionOpened));
    }

    public void fireSessionClosing(IsisSession session) {
        eventBusService.post(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
    }

    //	public void fireSessionFlushing(IsisSession session) {
    //		sessionLifecycleEvent.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
    //	}


}
