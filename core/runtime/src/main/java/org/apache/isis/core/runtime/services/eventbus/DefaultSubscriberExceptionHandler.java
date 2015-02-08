package org.apache.isis.core.runtime.services.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

public class DefaultSubscriberExceptionHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSubscriberExceptionHandler.class);

    //region > exception handling
    
    public static void processException(Throwable exception,
            Object event) {
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
    
    private static void abortTransaction(Throwable exception) {
        getTransactionManager().getTransaction().setAbortCause(new IsisApplicationException(exception));
        return;
    }

    protected static IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    //endregion

}
