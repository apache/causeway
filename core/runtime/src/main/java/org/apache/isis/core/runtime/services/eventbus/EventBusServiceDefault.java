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

import javax.enterprise.context.RequestScoped;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.runtime.services.RequestScopedService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

/**
 * @deprecated - but only because {@link org.apache.isis.objectstore.jdo.datanucleus.service.eventbus.EventBusServiceJdo}
 * is annotated (with <code>@DomainService</code>) as the default implementation.  The functionality in this implementation
 * is still required.
 */
@Deprecated
public class EventBusServiceDefault extends EventBusService {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusServiceDefault.class);

    /**
     * {@inheritDoc}
     *
     * This service overrides the method to perform additional validation that (a) request-scoped services register
     * their proxies, not themselves, and (b) that singleton services are never registered after the event bus has
     * been created.
     *
     * <p>
     *     Note that we <i>do</i> allow for request-scoped services to register (their proxies) multiple times, ie at
     *     the beginning of each transaction.  Because the subscribers are stored in a set, these additional
     *     registrations are in effect ignored.
     * </p>
     */
    @Override
    public void register(final Object domainService) {
        if(domainService instanceof RequestScopedService) {
            // ok; allow to be registered multiple times (each xactn) since stored in a set.
        } else {
            if (Annotations.getAnnotation(domainService.getClass(), RequestScoped.class) != null) {
                throw new IllegalArgumentException("Request-scoped services must register their proxy, not themselves");
            }
            // a singleton
            if(eventBus != null) {
                // ... coming too late to the party.
                throw new IllegalStateException("Event bus has already been created; too late to register any further (singleton) subscribers");
            }
        }
        super.register(domainService);
    }

    @Override
    protected EventBus newEventBus() {
        return new EventBus(newEventBusSubscriberExceptionHandler());
    }

    protected SubscriberExceptionHandler newEventBusSubscriberExceptionHandler() {
        return new SubscriberExceptionHandler(){
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                Object event = context.getEvent();
                if(!(event instanceof AbstractDomainEvent)) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("Ignoring exception '%s' (%s), not a subclass of AbstractDomainEvent", exception.getMessage(), exception.getClass().getName());
                    }
                    return;
                } 
                final AbstractDomainEvent<?> interactionEvent = (AbstractDomainEvent<?>) event;
                final AbstractDomainEvent.Phase phase = interactionEvent.getEventPhase();
                switch (phase) {
                case HIDE:
                    LOG.warn("Exception thrown during HIDE phase, to be safe will veto (hide) the interaction event, msg='{}', class='{}'", exception.getMessage(), exception.getClass().getName());
                    interactionEvent.hide();
                    break;
                case DISABLE:
                    LOG.warn("Exception thrown during DISABLE phase, to be safe will veto (disable) the interaction event, msg='{}', class='{}'", exception.getMessage(), exception.getClass().getName());
                    interactionEvent.disable(exception.getMessage()!=null?exception.getMessage(): exception.getClass().getName() + " thrown.");
                    break;
                case VALIDATE:
                    LOG.warn("Exception thrown during VALIDATE phase, to be safe will veto (invalidate) the interaction event, msg='{}', class='{}'", exception.getMessage(), exception.getClass().getName());
                    interactionEvent.invalidate(exception.getMessage()!=null?exception.getMessage(): exception.getClass().getName() + " thrown.");
                    break;
                case EXECUTING:
                    LOG.warn("Exception thrown during EXECUTING phase, to be safe will abort the transaction, msg='{}', class='{}'", exception.getMessage(), exception.getClass().getName());
                    abortTransaction(exception);
                    break;
                case EXECUTED:
                    LOG.warn("Exception thrown during EXECUTED phase, to be safe will abort the transaction, msg='{}', class='{}'", exception.getMessage(), exception.getClass().getName());
                    abortTransaction(exception);
                    break;
                }
            }
        };
    }

    private void abortTransaction(Throwable exception) {
        getTransactionManager().getTransaction().setAbortCause(new IsisApplicationException(exception));
        return;
    }

    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    //endregion


}

