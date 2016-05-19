package org.apache.isis.core.metamodel.services.msgbroker;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

@DomainService(nature = NatureOfService.DOMAIN)
public class MessageBrokerServiceInternalNoop implements MessageBrokerServiceInternal {

    @Override
    public void informUser(final String message) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void warnUser(final String message) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void raiseError(final String message) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

}
