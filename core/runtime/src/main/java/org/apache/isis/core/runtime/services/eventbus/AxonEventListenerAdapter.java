package org.apache.isis.core.runtime.services.eventbus;

import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;

public class AxonEventListenerAdapter extends AnnotationEventListenerAdapter {

    public AxonEventListenerAdapter(Object annotatedEventListener) {
        super(annotatedEventListener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adds
     */
    @Override
    public void handle(@SuppressWarnings("rawtypes") EventMessage event) {
        try {
            super.handle(event);
        } catch (Exception exception) {
            DefaultSubscriberExceptionHandler.processException(exception, event);
        }
    }
}
