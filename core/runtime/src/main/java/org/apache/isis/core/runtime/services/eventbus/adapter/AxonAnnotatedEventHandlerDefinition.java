package org.apache.isis.core.runtime.services.eventbus.adapter;

import org.axonframework.common.annotation.AbstractAnnotatedHandlerDefinition;
import org.axonframework.eventhandling.annotation.EventHandler;

class AxonAnnotatedEventHandlerDefinition
        extends AbstractAnnotatedHandlerDefinition<EventHandler> {

    public static final AxonAnnotatedEventHandlerDefinition INSTANCE = new AxonAnnotatedEventHandlerDefinition();

    private AxonAnnotatedEventHandlerDefinition() {
        super(EventHandler.class);
    }

    @Override
    protected Class<?> getDefinedPayload(EventHandler annotation) {
        return annotation.eventType();
    }
}
