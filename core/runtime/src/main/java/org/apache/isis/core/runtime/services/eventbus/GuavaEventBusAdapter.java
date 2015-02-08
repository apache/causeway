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

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * A wrapper for a Guava {@link com.google.common.eventbus.EventBus},
 * allowing arbitrary events to be posted and subscribed to.
 */
public class GuavaEventBusAdapter extends EventBusAdapter {

    private static final com.google.common.eventbus.EventBus eventBus = new com.google.common.eventbus.EventBus(newEventBusSubscriberExceptionHandler());

    protected static SubscriberExceptionHandler newEventBusSubscriberExceptionHandler() {
        return new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception,
                    SubscriberExceptionContext context) {
                Object event = context.getEvent();
                DefaultSubscriberExceptionHandler.processException(exception, event);
            }

        };
    }

    @Override
    public void register(final Object domainService) {
        // NO-OP. On current implementation subscribers list is the one managed
        // by the EventBusService, and used by EventBusServiceDefault.
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
