package org.apache.isis.core.runtime.services.eventbus;

import java.util.Map;

import com.google.common.collect.Maps;

import org.axonframework.domain.GenericEventMessage;
import org.axonframework.eventhandling.SimpleEventBus;

public class AxonSimpleEventBusAdapter extends EventBusAdapter {

    private static SimpleEventBus simpleEventBus = new SimpleEventBus();

    private static Map<Object, AxonEventListenerAdapter> adapters = Maps.newConcurrentMap();

    private static AxonEventListenerAdapter adapterFor(final Object domainService) {
        AxonEventListenerAdapter annotationEventListenerAdapter = adapters.get(domainService);
        if (annotationEventListenerAdapter == null) {
            annotationEventListenerAdapter = new AxonEventListenerAdapter(domainService);
            adapters.put(domainService, annotationEventListenerAdapter);
        }
        return annotationEventListenerAdapter;
    }

    @Override
    public void register(final Object domainService) {
        AxonSimpleEventBusAdapter.simpleEventBus.subscribe(AxonSimpleEventBusAdapter.adapterFor(domainService));

    }

    @Override
    public void unregister(final Object domainService) {
        // Seems it's needed to be a no-op (See EventBusService).
        // AxonSimpleEventBusAdapter.simpleEventBus.unsubscribe(AxonSimpleEventBusAdapter.adapterFor(domainService));

    }

    /*
     * {@inheritDoc} 
     * <p> 
     * Logic equivalent to Guava Event Bus. Despite that,
     * event processing cannot be followed after an Exception is thrown.
     */
    @Override
    public void post(final Object event) {
        AxonSimpleEventBusAdapter.simpleEventBus.publish(GenericEventMessage.asEventMessage(event));
    }

}