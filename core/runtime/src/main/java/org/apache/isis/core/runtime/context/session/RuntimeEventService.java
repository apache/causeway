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
package org.apache.isis.core.runtime.context.session;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.runtime.iacnt.IsisInteraction;

/**
 * 
 * @since 2.0
 * @implNote Listeners to runtime events can only reliably receive these after the 
 * post-construct phase has finished and before the pre-destroy phase has begun.
 */
@Service
@Named("isisRuntime.RuntimeEventService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class RuntimeEventService {
    
    @Inject private EventBusService eventBusService;  

   // -- APP

    public void fireAppPreMetamodel() {
        eventBusService.post(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreMetamodel));
    }

    public void fireAppPostMetamodel() {
        eventBusService.post(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPostMetamodel));
    }

    // -- SESSION

    public void fireSessionOpened(IsisInteraction session) {
        eventBusService.post(IsisInteractionLifecycleEvent.of(session, IsisInteractionLifecycleEvent.EventType.OPENED));
    }

    public void fireSessionClosing(IsisInteraction session) {
        eventBusService.post(IsisInteractionLifecycleEvent.of(session, IsisInteractionLifecycleEvent.EventType.CLOSING));
    }

    //	public void fireSessionFlushing(IsisInteraction session) {
    //		sessionLifecycleEvent.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
    //	}


}
