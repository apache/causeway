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
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusImplementation;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

public abstract class EventBusImplementationAbstract implements EventBusImplementation {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusImplementationAbstract.class);

    protected static void processException(
            final Throwable exception,
            final Object event) {
        if (!(event instanceof AbstractDomainEvent)) {
            if (LOG.isDebugEnabled()) {
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
            interactionEvent.disable(exception.getMessage() != null ? exception.getMessage() : exception.getClass().getName() + " thrown.");
            break;
        case VALIDATE:
            LOG.warn("Exception thrown during VALIDATE phase, to be safe will veto (invalidate) the interaction event, msg='{}', class='{}'", exception.getMessage(), exception.getClass().getName());
            interactionEvent.invalidate(exception.getMessage() != null ? exception.getMessage() : exception.getClass().getName() + " thrown.");
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


    // region > exception handling

    private static void abortTransaction(final Throwable exception) {
        getTransactionManager().getTransaction().setAbortCause(new IsisApplicationException(exception));
        return;
    }

    private static IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    // endregion

}
