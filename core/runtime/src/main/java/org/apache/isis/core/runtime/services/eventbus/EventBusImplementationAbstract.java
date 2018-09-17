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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.plugins.eventbus.EventBusPlugin;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

public abstract class EventBusImplementationAbstract implements EventBusPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusImplementationAbstract.class);

    protected void processException(
            final Throwable exception,
            final Object event) {
        final AbstractDomainEvent<?> domainEvent = asDomainEvent(event);
        if (domainEvent == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Ignoring exception '%s' (%s), not a subclass of AbstractDomainEvent",
                        exception.getMessage(), exception.getClass().getName());
            }
            return;
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("Exception stack trace (to help diagnose issue): ", exception);
        }

        final AbstractDomainEvent.Phase phase = domainEvent.getEventPhase();
        if(phase==null) {
            throw new RuntimeException(exception);
        }
        switch (phase) {
        case HIDE:
        case DISABLE:
        case VALIDATE:
            veto(exception, domainEvent, phase);
            break;
        case EXECUTING:
        case EXECUTED:
            abort(exception, phase);
            throw new RuntimeException(exception);
        }
    }

    private void veto(
            final Throwable exception,
            final AbstractDomainEvent<?> domainEvent,
            final AbstractDomainEvent.Phase phase) {
        final String exceptionMessage = exception.getMessage();
        LOG.warn("Exception thrown during {} phase, to be safe will veto the domain event, msg='{}', class='{}'",
                phase, exceptionMessage, exception.getClass().getName());
        final String message = exceptionMessage != null ? exceptionMessage : exception.getClass().getName() + " thrown.";
        domainEvent.veto(message);
    }

    private void abort(final Throwable exception, final AbstractDomainEvent.Phase phase) {
        LOG.warn("Exception thrown during {} phase, to be safe will abort the transaction, msg='{}', class='{}'",
                phase, exception.getMessage(), exception.getClass().getName());
        abortTransaction(exception);
    }

    /**
     * Mandatory hook, called if exception; attempt to determine the underlying event that was fired.
     *
     * <p>
     *     This is implementation specific; Axon for example wraps the event when reporting an error.
     * </p>
     */
    protected abstract AbstractDomainEvent<?> asDomainEvent(final Object event);


    private void abortTransaction(final Throwable exception) {
        getTransactionManager().getCurrentTransaction().setAbortCause(new IsisApplicationException(exception));
    }

    private IsisTransactionManager getTransactionManager() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession().getTransactionManager();
    }

    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

}
