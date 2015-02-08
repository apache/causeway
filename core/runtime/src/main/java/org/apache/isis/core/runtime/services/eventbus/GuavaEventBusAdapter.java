package org.apache.isis.core.runtime.services.eventbus;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

public class GuavaEventBusAdapter extends EventBusAdapter {

    private static final com.google.common.eventbus.EventBus eventBus = new com.google.common.eventbus.EventBus(newEventBusSubscriberExceptionHandler());

    protected static SubscriberExceptionHandler newEventBusSubscriberExceptionHandler() {
        return new SubscriberExceptionHandler(){
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                Object event = context.getEvent();
                DefaultSubscriberExceptionHandler.processException(exception, event);
            }

        };
    }

    @Override
    public void register(final Object domainService) {
        // NO-OP. On current implementation subscribers list is the one managed by the EventBusService, and used by EventBusServiceDefault.
    }

    @Override
    public void unregister(final Object domainService) {
        // Intentionally no-op.
        // this.eventBus.unregister(domainService);
    }

    @Override
    public void post(final Object event) {
        GuavaEventBusAdapter.eventBus.post(event);
    }

}
