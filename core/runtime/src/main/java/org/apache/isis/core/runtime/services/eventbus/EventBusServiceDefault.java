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

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.plugins.eventbus.EventBusPlugin;
import org.apache.isis.core.runtime.services.RequestScopedService;

import static org.apache.isis.config.internal._Config.getConfiguration;

/**
 * Holds common runtime logic for EventBusService implementations.
 */
public abstract class EventBusServiceDefault extends EventBusService {

    public static final String KEY_ALLOW_LATE_REGISTRATION = "isis.services.eventbus.allowLateRegistration";
    public static final String KEY_EVENT_BUS_IMPLEMENTATION = "isis.services.eventbus.implementation";

    private static final String EVENT_BUS_IMPLEMENTATION_DEFAULT = "plugin";
    private static final String[] KEYWORDS = {"auto", "plugin", "guava", "axon"};

    // -- register
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



    // -- init, shutdown
    @Override
    @Programmatic
    @PostConstruct
    public void init() {
        IsisConfiguration configuration = getConfiguration();
        this.allowLateRegistration = configuration.getBoolean(KEY_ALLOW_LATE_REGISTRATION, false);
        this.implementation = getNormalized(configuration.getString(KEY_EVENT_BUS_IMPLEMENTATION));
    }

    private static String getNormalized(final String implementation) {
        if(_Strings.isNullOrEmpty(implementation)) {
            return EVENT_BUS_IMPLEMENTATION_DEFAULT;
        } else {
            final String implementationTrimmed = implementation.trim();

            for(String keyword : KEYWORDS) {
                if(keyword.equalsIgnoreCase(implementationTrimmed)) {
                    return keyword;
                }
            }

            return implementationTrimmed;

        }
    }

    protected boolean allowLateRegistration;
    boolean isAllowLateRegistration() {
        return allowLateRegistration;
    }

    /**
     * Either &lt;guava&gt; or &lt;axon&gt;, or else the fully qualified class name of an
     * implementation of {@link org.apache.isis.core.plugins.eventbus.EventBusPlugin}.
     */
    private String implementation;
    String getImplementation() {
        return implementation;
    }

    @Override
    protected org.apache.isis.core.plugins.eventbus.EventBusPlugin newEventBus() {
        final EventBusPlugin implementation = instantiateEventBus();
        serviceRegistry.injectServicesInto(implementation);
        return implementation;
    }

    private EventBusPlugin instantiateEventBus() {

        String fqImplementationName = implementation;

        if( "plugin".equals(implementation) || "auto".equals(implementation) ) {

            return EventBusPlugin.get();

        } else if("guava".equals(implementation)) {
            // legacy of return new EventBusImplementationForGuava();
            fqImplementationName = "org.apache.isis.core.plugins.eventbus.EventBusPluginForGuava";
        } else if("axon".equals(implementation)) {
            // legacy of return new EventBusImplementationForAxonSimple();
            fqImplementationName = "org.apache.isis.core.plugins.eventbus.EventBusPluginForAxon";
        }

        final Class<?> aClass = ClassUtil.forName(fqImplementationName);
        if(EventBusPlugin.class.isAssignableFrom(aClass)) {
            try {
                return (EventBusPlugin) aClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new NonRecoverableException(e);
            }
        }
        throw new NonRecoverableException(
                "Could not instantiate event bus implementation '" + implementation + "'");
    }


    @javax.inject.Inject
    ServiceRegistry serviceRegistry;

}
