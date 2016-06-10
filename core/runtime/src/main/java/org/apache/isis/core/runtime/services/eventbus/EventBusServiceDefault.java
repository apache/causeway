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

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;

import com.google.common.base.Strings;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.eventbus.EventBusImplementation;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.runtime.services.RequestScopedService;
import org.apache.isis.core.runtime.services.eventbus.adapter.EventBusImplementationForAxonSimple;
import org.apache.isis.core.runtime.services.eventbus.adapter.EventBusImplementationForGuava;

/**
 * Holds common runtime logic for EventBusService implementations.
 */
public abstract class EventBusServiceDefault extends EventBusService {

    public static final String KEY_ALLOW_LATE_REGISTRATION = "isis.services.eventbus.allowLateRegistration";
    public static final String KEY_EVENT_BUS_IMPLEMENTATION = "isis.services.eventbus.implementation";

    //region > register
    /**
     * {@inheritDoc}
     *
     * This service overrides the method to perform additional validation that (a) request-scoped services register
     * their proxies, not themselves, and (b) that singleton services are never registered after the event bus has
     * been created.
     *
     * <p>
     *     Note that we <i>do</i> allow for request-scoped services to register (their proxies) multiple times, ie at
     *     the beginning of each transaction.  Because the subscribers are stored in a set, these additional
     *     registrations are in effect ignored.
     * </p>
     */
    @Override
    public void register(final Object domainService) {
        if(domainService instanceof RequestScopedService) {
            // ok; allow to be registered multiple times (each xactn) since stored in a set.
        } else {
            if (Annotations.getAnnotation(domainService.getClass(), RequestScoped.class) != null) {
                throw new IllegalArgumentException("Request-scoped services must register their proxy, not themselves");
            }
            // a singleton
            if (!allowLateRegistration && hasPosted()) {
                // ... coming too late to the party.
                throw new IllegalStateException("Attempting to register '" + domainService.getClass().getSimpleName() + "' as a subscriber.  However events have already been posted and it is too late to register any further (singleton) subscribers.  Either use @DomainServiceLayout(menuOrder=...) on subscribing services to ensure that subscribers are initialized before any services that might post events, or alternatively use '" + KEY_ALLOW_LATE_REGISTRATION + "' configuration property to relax this check (meaning that some subscribers will miss some posted events)");
            }
        }
        super.register(domainService);
    }

    //endregion

    //region > init, shutdown
    @Programmatic
    @PostConstruct
    public void init(final Map<String, String> properties) {
        this.allowLateRegistration = getElseFalse(properties, KEY_ALLOW_LATE_REGISTRATION);
        this.implementation = getNormalized(properties.get(KEY_EVENT_BUS_IMPLEMENTATION));
    }

    private static String getNormalized(final String implementation) {
        if(Strings.isNullOrEmpty(implementation)) {
            return "guava";
        } else {
            final String implementationTrimmed = implementation.trim();
            if("guava".equalsIgnoreCase(implementationTrimmed)) {
                return "guava";
            } else if("axon".equalsIgnoreCase(implementationTrimmed)) {
                return "axon";
            } else {
                return implementationTrimmed;
            }
        }
    }

    private static boolean getElseFalse(final Map<String, String> properties, final String key) {
        final String value = properties.get(key);
        return !Strings.isNullOrEmpty(value) && Boolean.parseBoolean(value);
    }
    //endregion

    private boolean allowLateRegistration;
    boolean isAllowLateRegistration() {
        return allowLateRegistration;
    }

    /**
     * Either &lt;guava&gt; or &lt;axon&gt;, or else the fully qualified class name of an
     * implementation of {@link org.apache.isis.applib.services.eventbus.EventBusImplementation}.
     */
    private String implementation;
    String getImplementation() {
        return implementation;
    }

    @Override
    protected org.apache.isis.applib.services.eventbus.EventBusImplementation newEventBus() {
        final EventBusImplementation implementation = instantiateEventBus();
        serviceRegistry2.injectServicesInto(implementation);
        return implementation;
    }

    private EventBusImplementation instantiateEventBus() {
        if("guava".equals(implementation)) {
            return new EventBusImplementationForGuava();
        }
        if("axon".equals(implementation)) {
            return new EventBusImplementationForAxonSimple();
        }

        final Class<?> aClass = ClassUtil.forName(implementation);
        if(EventBusImplementation.class.isAssignableFrom(aClass)) {
            try {
                return (EventBusImplementation) aClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new NonRecoverableException(e);
            }
        }
        throw new NonRecoverableException(
                "Could not instantiate event bus implementation '" + implementation + "'");
    }
    //endregion

    @javax.inject.Inject
    ServiceRegistry2 serviceRegistry2;

}
