/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.eventbus;

import com.google.common.eventbus.EventBus;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * A wrapper for a Guava {@link EventBus}, allowing arbitrary events to be posted and
 * subscribed to.
 *  
 * <p>
 * It is highly advisable that only domain services - not domain entities - are registered as subscribers.  
 * Domain services are guaranteed to be instantiated and resident in memory, whereas the same is not true
 * of domain entities.  The typical implementation of a domain service subscriber is to identify the impacted entities,
 * load them using a repository, and then to delegate to the event to them.
 */
@Hidden
public abstract class EventBusService {

    /**
     * A no-op implementation to use as a default for domain objects that are being
     * instantiated and for which the event bus service has not yet been injected.
     */
    public static class Noop extends EventBusService {
        @Override
        public void register(Object domainObject) {}
        @Override
        public void unregister(Object domainObject) {}
        @Override
        public void post(Object event) {}
        @Override
        protected EventBus getEventBus() {
            return null;
        }
    }

    public static final EventBusService NOOP = new Noop();
    
    /**
     * @return an {@link EventBus} scoped to the current session.
     */
    protected abstract EventBus getEventBus();
    
    /**
     * Register the domain object with the service.
     * 
     * <p>
     * This must be called manually, but a good technique is for the domain object to call
     * this method when the service is injected into it.
     * 
     * <p>
     * For example:
     * <pre>
     * private EventBusService eventBusService;
     * public void injectEventBusService(final EventBusService eventBusService) {
     *     this.eventBusService = eventBusService;
     *     eventBusService.register(this);
     * }
     * </pre>
     */
    @Programmatic
    public void register(Object domainObject) {
        getEventBus().register(domainObject);
    }
    
    @Programmatic
    public void unregister(Object domainObject) {
        getEventBus().unregister(domainObject);
    }
    
    /**
     * Post an event.
     */
    @Programmatic
    public void post(Object event) {
        if(skip(event)) {
            return;
        }
        getEventBus().post(event);
    }

    
    /**
     * A hook to allow subclass implementations to skip the publication of certain events.
     * 
     * <p>
     * For example, the <tt>EventBusServiceJdo</tt> does not publish events if the method
     * is called by JDO/DataNucleus infrastructure, eg during hydration or commits.
     */
    protected boolean skip(Object event) {
        return false;
    }
}

