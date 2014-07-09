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

package org.apache.isis.core.metamodel.facets.properties.interaction;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.PropertyInteractionEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;

public abstract class InteractionWithPropertySetterFacetAbstract
        extends SingleValueFacetAbstract<Class<? extends PropertyInteractionEvent<?,?>>>
        implements InteractionWithPropertySetterFacet, MultiTypedFacet {

    public static Class<? extends Facet> type() {
        return PropertySetterFacet.class;
    }

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertySetterFacet setterFacet;

    private final Helper helper;

    public InteractionWithPropertySetterFacetAbstract(
            final Class<? extends PropertyInteractionEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final PropertySetterFacet setterFacet,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {
        super(type(), eventType, holder);
        this.getterFacet = getterFacet;
        this.setterFacet = setterFacet;
        this.helper = new Helper(servicesInjector);
    }

    @Override
    public void setProperty(ObjectAdapter targetAdapter, ObjectAdapter valueAdapter) {
        if(setterFacet == null) {
            return;
        }
        if(helper.getEventBusService() == null) {
            setterFacet.setProperty(targetAdapter, valueAdapter);
            return;
        }

        final Object oldValue = getterFacet.getProperty(targetAdapter);
        setterFacet.setProperty(targetAdapter, valueAdapter);
        final Object newValue = getterFacet.getProperty(targetAdapter);
        postEventIfChanged(targetAdapter, getIdentified().getIdentifier(), oldValue, newValue);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void postEventIfChanged(
            final ObjectAdapter targetAdapter,
            final Identifier identifier,
            final Object oldValue,
            final Object newValue) {

        if(Objects.equal(oldValue, newValue)) {
            // do nothing.
            return;
        }
        try {
            final Object source = ObjectAdapter.Util.unwrap(targetAdapter);

            final Class type = value();
            final PropertyInteractionEvent<?, ?> event = Util.newEvent(type, source, identifier, oldValue, newValue);

            helper.postEvent(event);
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }


    // //////////////////////////////////////
    // MultiTypedFacet
    // //////////////////////////////////////

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Facet>[] facetTypes() {
        return Lists.newArrayList(
                type(),
                InteractionWithPropertySetterFacet.class
        ).toArray(new Class[]{});
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Facet> T getFacet(Class<T> facet) {
        return (T) this;
    }

}
