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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import org.apache.isis.applib.annotation.Programmatic;

public abstract class EventBusService {

    /**
     * A no-op implementation to use as a default for domain objects that are being
     * instantiated and for which the event bus service has not yet been injected. 
     */
    public static final EventBusService NOOP = new EventBusService() {
        @Override
        public void register(Object domainObject) {};
        @Override
        public void unregister(Object domainObject) {};
        @Override
        public void post(Object event, java.util.Collection<?>... collections) {}
        @Override
        protected EventBus getEventBus() {
            return null;
        }
    };
    
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
     * Post an event, but ensuring that any possible subscribers 
     * to that event have been brought into memory.
     */
    @Programmatic
    public void post(Object event, Collection<?>... collections ) {
        if(skip(event)) {
            return;
        }
        final List<Object> list = Lists.newArrayList();
        for (Collection<?> collection : collections) {
            list.addAll(collection);
        }
        ensureLoaded(list);
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

    /**
     * Overrideable hook method.
     * 
     * <p>
     * If using JDO objectstore, then use the <tt>EventBusServiceJdo</tt> implementation, 
     * which overrides this method to load objects from the database.
     */
    protected void ensureLoaded(final Collection<?> collection) {
    }
    
}

