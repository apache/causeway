package org.apache.isis.core.runtime.services.eventbus;

import org.apache.isis.applib.services.eventbus.EventBus;

public class AxonSimpleEventBusService extends RuntimeEventBusService {

    @Override
    protected EventBus newEventBus() {
        return new AxonSimpleEventBusAdapter();
    }

}
