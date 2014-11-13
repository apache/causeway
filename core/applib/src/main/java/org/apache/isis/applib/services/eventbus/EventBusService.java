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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * A wrapper for a Guava {@link EventBus}, allowing arbitrary events to be posted and
 * subscribed to.
 *  
 * <p>
 *      Only domain services (not domain entities or view models) should be registered; only they are guaranteed to be
 *      instantiated and resident in memory.
 * </p>
 * <p>
 *     It <i>is</i> possible to register request-scoped services, however they should register their proxy
 *     rather than themselves.  This ensures that the actual subscribers are all singletons.  This implementation uses
 *     reference counting to keep track of whether there are any concurrent instances of a request-scoped service
 *     (keeps things nice and symmetrical).
 * </p>
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
    @Programmatic
    protected abstract EventBus getEventBus();

    //region > register, unregister

    @Programmatic
    public void register(Object domainObject) {
        referenceCountBySubscriber.putIfAbsent(domainObject, new AtomicInteger(0));
        referenceCountBySubscriber.get(domainObject).incrementAndGet();
    }

    @Programmatic
    public void unregister(Object domainObject) {
        final AtomicInteger atomicInteger = referenceCountBySubscriber.get(domainObject);
        atomicInteger.decrementAndGet();
    }

    //endregion

    //region > subscribers

    private final ConcurrentMap<Object, AtomicInteger> referenceCountBySubscriber = new MapMaker().weakKeys().makeMap();

    /**
     * Not API
     */
    @Programmatic
    public Set<Object> getSubscribers() {



        // only those subscribers that are "active"
        Set<Object> subscribers = Sets.newLinkedHashSet();
        for (Map.Entry<Object, AtomicInteger> subscriberReferenceCount : this.referenceCountBySubscriber.entrySet()) {
            if(subscriberReferenceCount.getValue().get()>0) {
                subscribers.add(subscriberReferenceCount.getKey());
            }
        }
        return subscribers;
    }
    //endregion
    
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

