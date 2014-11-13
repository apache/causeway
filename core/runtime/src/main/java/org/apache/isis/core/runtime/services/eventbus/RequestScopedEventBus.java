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
package org.apache.isis.core.runtime.services.eventbus;

import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;


/**
 * Manages a guava {@link com.google.common.eventbus.EventBus}, scoped and private to a given request.
 *
 * <p>
 *     This involves obtaining the set of subscribers from the (owning, singleton) {@link org.apache.isis.applib.services.eventbus.EventBusService event bus service}
 *     and registering/unregistering them with the guava event bus at the appropriate time.
 * </p>
 */
@DomainService
@RequestScoped
public class RequestScopedEventBus {

    //region > startRequest, endRequest

    /**
     * Cannot do the setup of the event bus here (so this is asymmetric with <code>@PreDestroy</code>) because there is
     * no guarantee of the order in which <code>@PostConstruct</code> is called on any request-scoped services.  We
     * therefore let the request-scoped services register themselves with (the owning, singleton)
     * {@link org.apache.isis.applib.services.eventbus.EventBusService} in their
     * <code>@PostConstruct</code> and do the actual instantiation of the guava {@link com.google.common.eventbus.EventBus}
     * and registering of subscribers lazily, in {@link #getEventBus()}.  This lifecycle method ({@link #startRequest()})
     * is therefore a no-op.
     *
     * <p>
     *     The guava {@link com.google.common.eventbus.EventBus} can however (and is) be torndown in the
     *     <code>@PreDestroy</code> {@link #endRequest()} lifecycle method.
     * </p>
     */
    @Programmatic
    @PostConstruct
    public void startRequest() {
        // no-op
    }

    @Programmatic
    @PreDestroy
    public void endRequest() {
        teardownEventBus();
    }

    //endregion

    //region > (guava) event bus

    /**
     * Lazily populated in {@link #getEventBus()}.
     */
    private EventBus eventBus;

    /**
     * Lazily populates the event bus and captures the set of subscribers from
     * {@link EventBusServiceDefault#getSubscribers()}}.
     *
     * <p>
     *     These are torn down when the {@link #endRequest() request ends}.
     * </p>
     */
    @Programmatic
    public EventBus getEventBus() {
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
    private Set<Object> subscribers;

    /**
     * Populates {@link #eventBus} and {@link #subscribers}.
     *
     * <p>
     *     Guava event bus will throw an exception if attempt to unsubscribe any subscribers that were not subscribed.
     *     It is therefore the responsibility of this (wrapper) service to remember which services were registered
     *     at the start of the request, and to unregister precisely this same set of services at the end.
     * </p>
     */
    protected void setupEventBus() {
        if(eventBus != null) {
            return;
        }
        this.eventBus = newEventBus();

        // "pulls" subscribers from the (singleton) event bus service
        subscribers = eventBusService.getSubscribers();

        for (Object subscriber : this.subscribers) {
            eventBus.register(subscriber);
        }
    }

    protected void teardownEventBus() {
        if(subscribers != null) {
            for (Object subscriber : this.subscribers) {
                eventBus.unregister(subscriber);
            }
        }

        this.eventBus = null;
    }

    protected EventBus newEventBus() {
        return new EventBus(newEventBusSubscriberExceptionHandler());
    }

    protected SubscriberExceptionHandler newEventBusSubscriberExceptionHandler() {
        return new SubscriberExceptionHandler(){
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                final List<Throwable> causalChain = Throwables.getCausalChain(exception);
                for (Throwable cause : causalChain) {
                    if(cause instanceof RecoverableException || cause instanceof NonRecoverableException) {
                        getTransactionManager().getTransaction().setAbortCause(new IsisApplicationException(exception));
                        return;
                    }
                }
                // otherwise simply ignore
            }
        };
    }

    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    //endregion

    //region > injected services
    private EventBusServiceDefault eventBusService;
    /**
     * Singleton holding the list of subscribers.
     *
     * <p>
     *     Must use an <code>injectXxx</code> method because Isis does not (currently) support field injection into
     *     request-scoped services (the javassist proxy does not delegate on).
     * </p>
     */
    @Programmatic
    public void injectEventBusService(EventBusServiceDefault eventBusService) {
        this.eventBusService = eventBusService;
    }

    //endregion

}

