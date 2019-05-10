/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.wrapper;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrappingObject;
import org.apache.isis.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.isis.applib.services.wrapper.events.ActionInvocationEvent;
import org.apache.isis.applib.services.wrapper.events.ActionUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.ActionVisibilityEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionAccessEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionAddToEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionMethodEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionRemoveFromEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionVisibilityEvent;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.events.ObjectTitleEvent;
import org.apache.isis.applib.services.wrapper.events.ObjectValidityEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyModifyEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyVisibilityEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.wrapper.dispatchers.InteractionEventDispatcher;
import org.apache.isis.core.wrapper.dispatchers.InteractionEventDispatcherTypeSafe;
import org.apache.isis.core.wrapper.handlers.ProxyContextHandler;
import org.apache.isis.core.wrapper.proxy.ProxyCreator;

/**
 * This service provides the ability to &quot;wrap&quot; of a domain object such that it can
 * be interacted with while enforcing the hide/disable/validate rules implies by
 * the Isis programming model.
 *
 * <p>
 * This implementation has no UI-visible actions and is the supported implementation, so it is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService}.   This means that by including
 * <tt>o.a.i.core:isis-core-wrapper</tt> on the classpath, the service is automatically registered; no further
 * configuration is required.
 */
@Singleton
public class WrapperFactoryDefault implements WrapperFactory {

    private final List<InteractionListener> listeners = new ArrayList<InteractionListener>();
    private final Map<Class<? extends InteractionEvent>, InteractionEventDispatcher> dispatchersByEventClass = new HashMap<Class<? extends InteractionEvent>, InteractionEventDispatcher>();


    private final ProxyContextHandler proxyContextHandler;

    public WrapperFactoryDefault() {
        this(new ProxyCreator());
    }
    WrapperFactoryDefault(final ProxyCreator proxyCreator) {

        proxyContextHandler = new ProxyContextHandler(proxyCreator);

        dispatchersByEventClass.put(ObjectTitleEvent.class, new InteractionEventDispatcherTypeSafe<ObjectTitleEvent>() {
            @Override
            public void dispatchTypeSafe(final ObjectTitleEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.objectTitleRead(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(PropertyVisibilityEvent.class, new InteractionEventDispatcherTypeSafe<PropertyVisibilityEvent>() {
            @Override
            public void dispatchTypeSafe(final PropertyVisibilityEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.propertyVisible(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(PropertyUsabilityEvent.class, new InteractionEventDispatcherTypeSafe<PropertyUsabilityEvent>() {
            @Override
            public void dispatchTypeSafe(final PropertyUsabilityEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.propertyUsable(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(PropertyAccessEvent.class, new InteractionEventDispatcherTypeSafe<PropertyAccessEvent>() {
            @Override
            public void dispatchTypeSafe(final PropertyAccessEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.propertyAccessed(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(PropertyModifyEvent.class, new InteractionEventDispatcherTypeSafe<PropertyModifyEvent>() {
            @Override
            public void dispatchTypeSafe(final PropertyModifyEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.propertyModified(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(CollectionVisibilityEvent.class, new InteractionEventDispatcherTypeSafe<CollectionVisibilityEvent>() {
            @Override
            public void dispatchTypeSafe(final CollectionVisibilityEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.collectionVisible(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(CollectionUsabilityEvent.class, new InteractionEventDispatcherTypeSafe<CollectionUsabilityEvent>() {
            @Override
            public void dispatchTypeSafe(final CollectionUsabilityEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.collectionUsable(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(CollectionAccessEvent.class, new InteractionEventDispatcherTypeSafe<CollectionAccessEvent>() {
            @Override
            public void dispatchTypeSafe(final CollectionAccessEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.collectionAccessed(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(CollectionAddToEvent.class, new InteractionEventDispatcherTypeSafe<CollectionAddToEvent>() {
            @Override
            public void dispatchTypeSafe(final CollectionAddToEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.collectionAddedTo(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(CollectionRemoveFromEvent.class, new InteractionEventDispatcherTypeSafe<CollectionRemoveFromEvent>() {
            @Override
            public void dispatchTypeSafe(final CollectionRemoveFromEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.collectionRemovedFrom(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(ActionVisibilityEvent.class, new InteractionEventDispatcherTypeSafe<ActionVisibilityEvent>() {
            @Override
            public void dispatchTypeSafe(final ActionVisibilityEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.actionVisible(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(ActionUsabilityEvent.class, new InteractionEventDispatcherTypeSafe<ActionUsabilityEvent>() {
            @Override
            public void dispatchTypeSafe(final ActionUsabilityEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.actionUsable(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(ActionArgumentEvent.class, new InteractionEventDispatcherTypeSafe<ActionArgumentEvent>() {
            @Override
            public void dispatchTypeSafe(final ActionArgumentEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.actionArgument(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(ActionInvocationEvent.class, new InteractionEventDispatcherTypeSafe<ActionInvocationEvent>() {
            @Override
            public void dispatchTypeSafe(final ActionInvocationEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.actionInvoked(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(ObjectValidityEvent.class, new InteractionEventDispatcherTypeSafe<ObjectValidityEvent>() {
            @Override
            public void dispatchTypeSafe(final ObjectValidityEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.objectPersisted(interactionEvent);
                }
            }
        });
        dispatchersByEventClass.put(CollectionMethodEvent.class, new InteractionEventDispatcherTypeSafe<CollectionMethodEvent>() {
            @Override
            public void dispatchTypeSafe(final CollectionMethodEvent interactionEvent) {
                for (final InteractionListener l : getListeners()) {
                    l.collectionMethodInvoked(interactionEvent);
                }
            }
        });
    }

    // /////////////////////////////////////////////////////////////
    // wrap and unwrap
    // /////////////////////////////////////////////////////////////

    public <T> T wm(final Class<T> mixinClass, final Object mixedIn) {
        return w(factoryService.m(mixinClass, mixedIn));
    }

    public <T> T wrapMixin(final Class<T> mixinClass, final Object mixedIn) {
        return w(factoryService.m(mixinClass, mixedIn));
    }

    @Override
    public <T> T w(final T domainObject) {
        return wrap(domainObject, ExecutionMode.EXECUTE);
    }

    @Override
    public <T> T wrap(final T domainObject) {
        return wrap(domainObject, ExecutionMode.EXECUTE);
    }

    @Override
    public <T> T wrapTry(final T domainObject) {
        return wrap(domainObject, ExecutionMode.TRY);
    }

    @Override
    public <T> T wrapNoExecute(final T domainObject) {
        return wrap(domainObject, ExecutionMode.NO_EXECUTE);
    }

    @Override
    public <T> T wrapSkipRules(final T domainObject) {
        return wrap(domainObject, ExecutionMode.SKIP_RULES);
    }

    @Override
    public <T> T wrap(final T domainObject, final ExecutionMode mode) {
        if (domainObject instanceof WrappingObject) {
            final WrappingObject wrapperObject = (WrappingObject) domainObject;
            final ExecutionMode wrapperMode = wrapperObject.__isis_executionMode();
            if(wrapperMode != mode) {
                final Object underlyingDomainObject = wrapperObject.__isis_wrapped();
                return _Casts.uncheckedCast(createProxy(underlyingDomainObject, mode, isisSessionFactory));
            }
            return domainObject;
        }
        return createProxy(domainObject, mode, isisSessionFactory);
    }

    protected <T> T createProxy(
            final T domainObject,
            final ExecutionMode mode,
            final IsisSessionFactory isisSessionFactory) {
        return proxyContextHandler.proxy(domainObject, mode, isisSessionFactory);
    }

    @Override
    public boolean isWrapper(final Object possibleWrappedDomainObject) {
        return possibleWrappedDomainObject instanceof WrappingObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Programmatic
    public <T> T unwrap(T possibleWrappedDomainObject) {
        if(isWrapper(possibleWrappedDomainObject)) {
            final WrappingObject wrappingObject = (WrappingObject) possibleWrappedDomainObject;
            return (T) wrappingObject.__isis_wrapped();
        }
        return possibleWrappedDomainObject;
    }

    // /////////////////////////////////////////////////////////////
    // Listeners
    // /////////////////////////////////////////////////////////////

    @Override
    public List<InteractionListener> getListeners() {
        return listeners;
    }

    @Override
    public boolean addInteractionListener(final InteractionListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeInteractionListener(final InteractionListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void notifyListeners(final InteractionEvent interactionEvent) {
        final InteractionEventDispatcher dispatcher = dispatchersByEventClass.get(interactionEvent.getClass());
        if (dispatcher == null) {
            throw new RuntimeException("Unknown InteractionEvent - register into dispatchers map");
        }
        dispatcher.dispatch(interactionEvent);
    }


    @Inject
    AuthenticationSessionProvider authenticationSessionProvider;

    @Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

    @Inject
    IsisSessionFactory isisSessionFactory;

    @Inject
    FactoryService factoryService;

}
