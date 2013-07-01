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

package org.apache.isis.core.progmodel.facets.properties.event;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.event.PostsPropertyChangedEventFacet;
import org.apache.isis.core.metamodel.facets.properties.event.PostsPropertyChangedEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;

public class PostsPropertyChangedEventFacetAnnotation extends PostsPropertyChangedEventFacetAbstract {

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertySetterFacet setterFacet;
    private final PropertyClearFacet clearFacet;
    private final ServicesProvider servicesProvider;
    
    private EventBusService eventBusService;
    private boolean searchedForEventBusService = false;

    public PostsPropertyChangedEventFacetAnnotation(
            final Class<? extends PropertyChangedEvent<?, ?>> changedEventType, 
            final PropertyOrCollectionAccessorFacet getterFacet, 
            final PropertySetterFacet setterFacet, 
            final PropertyClearFacet clearFacet, 
            final ServicesProvider servicesProvider, 
            final FacetHolder holder) {
        super(changedEventType, holder);
        this.getterFacet = getterFacet;
        this.setterFacet = setterFacet;
        this.clearFacet = clearFacet;
        this.servicesProvider = servicesProvider;
    }

    @Override
    public void setProperty(ObjectAdapter inObject, ObjectAdapter value) {
        if(this.setterFacet == null) {
            return;
        }
        eventBusService = getEventBusService();
        if(eventBusService == null) {
            setterFacet.setProperty(inObject, value);
            return;
        }
        
        final Object oldValue = this.getterFacet.getProperty(inObject);
        this.setterFacet.setProperty(inObject, value);
        final Object newValue = this.getterFacet.getProperty(inObject);
        postEventIfChanged(inObject, oldValue, newValue);
    }

    @Override
    public void clearProperty(ObjectAdapter inObject) {
        if(this.clearFacet == null) {
            return;
        }
        eventBusService = getEventBusService();
        if(eventBusService == null) {
            clearFacet.clearProperty(inObject);
            return;
        }

        final Object oldValue = this.getterFacet.getProperty(inObject);
        this.clearFacet.clearProperty(inObject);
        final Object newValue = this.getterFacet.getProperty(inObject);
        postEventIfChanged(inObject, oldValue, newValue);
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void postEventIfChanged(ObjectAdapter inObject, final Object oldValue, final Object newValue) {
        if(Objects.equal(oldValue, newValue)) {
            // do nothing.
            return;
        }
        final Object source = inObject.getObject();
        try {
            final Class type = value();
            final PropertyChangedEvent<?, ?> event = newEvent(type, oldValue, newValue, source);
            
            eventBusService.post(event);
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    static <S,T> PropertyChangedEvent<S,T> newEvent(final Class<? extends PropertyChangedEvent<S, T>> type, final T oldValue, final T newValue, final S source) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        final PropertyChangedEvent<S, T> event = type.newInstance();
        
        setField("source", event, source);
        setField("oldValue", event, oldValue);
        setField("newValue", event, newValue);
        return event;
    }

    private static void setField(final String name, final PropertyChangedEvent<?, ?> event, final Object sourceValue) throws NoSuchFieldException, IllegalAccessException {
        final Field sourceField = PropertyChangedEvent.class.getDeclaredField(name);
        sourceField.setAccessible(true);
        sourceField.set(event, sourceValue);
    }
    
    private EventBusService getEventBusService() {
        if(!searchedForEventBusService) {
            final List<ObjectAdapter> serviceAdapters = servicesProvider.getServices();
            for (ObjectAdapter serviceAdapter : serviceAdapters) {
                final Object service = serviceAdapter.getObject();
                if(service instanceof EventBusService) {
                    eventBusService = (EventBusService) service;
                    break;
                }
            }
        } 
        searchedForEventBusService = true;
        return eventBusService;
    }
    
    // //////////////////////////////////////
    // MultiTypedFacet


    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Facet>[] facetTypes() {
        return Lists.newArrayList(PostsPropertyChangedEventFacet.class, PropertySetterFacet.class, PropertyClearFacet.class).toArray(new Class[]{});
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Facet> T getFacet(Class<T> facet) {
        return (T) this;
    }


}
