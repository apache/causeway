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

import java.util.Collections;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
 *
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
        public void register(Object domainService) {}
        @Override
        public void unregister(Object domainService) {}
        @Override
        public void post(Object event) {}
        @Override
        protected EventBus getEventBus() {
            return null;
        }
        @Override
        protected EventBus newEventBus() { return null; }
    }

    public static final EventBusService NOOP = new Noop();

    //region > init, shutdown

    /**
     * Cannot do the setup of the event bus here (so this is asymmetric with <code>@PreDestroy</code>) because there is
     * no guarantee of the order in which <code>@PostConstruct</code> is called on any request-scoped services.  We
     * therefore allow all services (singleton or request-scoped) to {@link #register(Object) register} themselves
     * with this service in their <code>@PostConstruct</code> and do the actual instantiation of the guava
     * {@link com.google.common.eventbus.EventBus} and registering of subscribers lazily, in {@link #getEventBus()}.
     * This lifecycle method ({@link #init()}) is therefore a no-op.
     *
     * <p>
     *     The guava {@link com.google.common.eventbus.EventBus} can however (and is) be torndown in the
     *     <code>@PreDestroy</code> {@link #shutdown()} lifecycle method.
     * </p>
     */
    @Programmatic
    @PostConstruct
    public void init() {
        // no-op
    }

    @Programmatic
    @PreDestroy
    public void shutdown() {
        teardownEventBus();
    }

    //endregion

    //region > register, unregister

    /**
     * Both singleton and request-scoped domain services can register on the event bus; this should be done in their
     * <code>@PostConstruct</code> callback method.
     *
     * <p>
     *     <b>Important:</b> Request-scoped services should register their proxy, not themselves.  This is because it is
     *     the responsibility of the proxy to ensure that the correct underlying (thread-local) instance of the service
     *     is delegated to.  If the actual instance were to be registered, this would cause a memory leak and all sorts
     *     of other unexpected issues.
     * </p>
     *
     * <p>
     *     Also, request-scoped services should <i>NOT</i> unregister themselves.  This is because the
     *     <code>@PreDestroy</code> lifecycle method is called at the end of each transaction.  The proxy needs to
     *     remain registered on behalf for any subsequent transactions.
     * </p>
     *
     * <p>For example:</p>
     * <pre>
     *     @RequestScoped @DomainService
     *     public class SomeSubscribingService {
     *
     *         @Inject private EventBusService ebs;
     *         @Inject private SomeSubscribingService proxy;
     *
     *         @PostConstruct
     *         public void startRequest() {
     *              // register with bus
     *              ebs.register(proxy);
     *         }
     *         @PreDestroy
     *         public void endRequest() {
     *              //no-op
     *         }
     *     }
     * </pre>
     *
     * <p>
     *     The <code>@PostConstruct</code> callback is the correct place to register for both singleton and
     *     request-scoped services.  For singleton domain services, this is done during the initial bootstrapping of
     *     the system.  For request-scoped services, this is done for the first transaction.  In fact, because
     *     singleton domain services are initialized <i>within a current transaction</i>, the request-scoped services
     *     will actually be registered <i>before</i> the singleton services.  Each subsequent transaction will have the
     *     request-scoped service re-register with the event bus, however the event bus stores its subscribers in a
     *     set and so these re-registrations are basically a no-op.
     * </p>
     *
     * @param domainService
     */
    @Programmatic
    public void register(final Object domainService) {
        doRegister(domainService);
    }

    /**
     * Extracted out only to make it easier for subclasses to override {@link #register(Object)} if there were ever a
     * need to.
     */
    protected void doRegister(Object domainService) {
        subscribers.add(domainService);
    }

    /**
     * Notionally allows subscribers to unregister from the event bus; however this is a no-op.
     *
     * <p>
     *     It is safe for singleton services to unregister from the bus, however this is only ever called when the
     *     app is being shutdown so there is no real effect.  For request-scoped services meanwhile that (as
     *     explained in {@link #register(Object)}'s documentation) actually register their proxy, it would be an error
     *     to unregister the proxy; subsequent transactions (for this thread or others) must be routed through that
     *     proxy.
     * </p>
     */
    @Programmatic
    public void unregister(final Object domainService) {
        // intentionally no-op
    }

    //endregion

    //region > subscribers

    private final Set<Object> subscribers = Sets.newConcurrentHashSet();

    /**
     * Returns an immutable snapshot of the current subscribers.
     */
    @Programmatic
    public Set<Object> getSubscribers() {
        return Collections.unmodifiableSet(Sets.newLinkedHashSet(subscribers));
    }
    //endregion

    //region > post

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

    //endregion


    //region > getEventBus

    /**
     * Lazily populated in {@link #getEventBus()}.
     */
    protected EventBus eventBus;

    /**
     * Lazily populates the event bus for the current {@link #getSubscribers() subscribers}.
     */
    @Programmatic
    protected EventBus getEventBus() {
        setupEventBus();
        return eventBus;
    }

    /**
     * Set of subscribers registered with the event bus.
     *
     * <p>
     * Lazily populated in {@link #setupEventBus()}.
     * </p>
     */
    private Set<Object> registeredSubscribers;

    /**
     * Populates {@link #eventBus} with the {@link #registeredSubscribers currently registered subscribers}.
     *
     * <p>
     *     Guava event bus will throw an exception if attempt to unsubscribe any subscribers that were not subscribed.
     *     It is therefore the responsibility of this service to remember which services were registered
     *     at the start of the request, and to unregister precisely this same set of services at the end.
     * </p>
     *
     * <p>
     *     That said, the Guava event bus is only ever instantiated once (it is in essence an application-scoped singleton),
     *     and so once created it is not possible for any subscribers to be registered.  For this reason, the
     *     {@link #register(Object)} will throw an exception if any attempt is made to register once the event bus
     *     has been instantiated.
     * </p>
     */
    protected void setupEventBus() {
        if(eventBus != null) {
            return;
        }
        this.eventBus = newEventBus();

        registeredSubscribers = getSubscribers();

        for (Object subscriber : this.registeredSubscribers) {
            eventBus.register(subscriber);
        }
    }

    protected void teardownEventBus() {
        if(registeredSubscribers != null) {
            for (Object subscriber : this.registeredSubscribers) {
                eventBus.unregister(subscriber);
            }
        }

        this.eventBus = null;
    }

    //endregion

    //region > hook methods (newEventBus, skip)

    /**
     * Mandatory hook method for subclass to instantiate an appropriately configured Guava event bus.
     */
    protected abstract EventBus newEventBus();


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

    //endregion


}

