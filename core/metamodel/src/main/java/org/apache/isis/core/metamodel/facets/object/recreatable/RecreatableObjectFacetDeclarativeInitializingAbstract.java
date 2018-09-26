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

package org.apache.isis.core.metamodel.facets.object.recreatable;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public abstract class RecreatableObjectFacetDeclarativeInitializingAbstract 
extends RecreatableObjectFacetAbstract {

    private final ObjectAdapterProvider adapterProvider;

    public RecreatableObjectFacetDeclarativeInitializingAbstract(
            final FacetHolder holder,
            final RecreationMechanism recreationMechanism,
            final ObjectAdapterProvider adapterProvider,
            final ServicesInjector servicesInjector,
            final PostConstructMethodCache postConstructMethodCache) {
        super(holder, recreationMechanism, postConstructMethodCache, servicesInjector);
        this.adapterProvider = adapterProvider;
    }

    @Override
    protected void doInitialize(
            final Object viewModelPojo,
            final String mementoStr) {

        final UrlEncodingService codec = servicesInjector.lookupService(UrlEncodingService.class);
        final SerializingAdapter serializer = servicesInjector.lookupService(SerializingAdapter.class);

        final _Mementos.Memento memento = _Mementos.parse(codec, serializer, mementoStr);

        final Set<String> mementoKeys = memento.keySet();

        final ObjectAdapter viewModelAdapter = adapterProvider.adapterForViewModel(
                viewModelPojo, 
                (ObjectSpecId objectSpecId)->
                    Oid.Factory.viewmodelOf(objectSpecId, mementoStr) );
                    

        final ObjectSpecification spec = viewModelAdapter.getSpecification();
        final Stream<OneToOneAssociation> properties = spec.streamProperties(Contributed.EXCLUDED);
        
        properties.forEach(property->{
            final String propertyId = property.getId();

            Object propertyValue = null;

            if(mementoKeys.contains(propertyId)) {
                final Class<?> propertyType = property.getSpecification().getCorrespondingClass();
                propertyValue = memento.get(propertyId, propertyType);
            }

            if(propertyValue != null) {
                property.set(viewModelAdapter, adapterProvider.adapterFor(propertyValue), InteractionInitiatedBy.FRAMEWORK);
            }
        });
        
    }

    @Override
    public String memento(Object viewModelPojo) {

        final UrlEncodingService codec = servicesInjector.lookupService(UrlEncodingService.class);
        final SerializingAdapter serializer = servicesInjector.lookupService(SerializingAdapter.class);

        final _Mementos.Memento memento = _Mementos.create(codec, serializer);

        final ObjectAdapter ownerAdapter = adapterProvider.disposableAdapterForViewModel(viewModelPojo);
        final ObjectSpecification spec = ownerAdapter.getSpecification();
        
        final Stream<OneToOneAssociation> properties = spec.streamProperties(Contributed.EXCLUDED);
        
        properties
        // ignore read-only
        .filter(property->property.containsDoOpFacet(PropertySetterFacet.class)) 
        // ignore those explicitly annotated as @NotPersisted
        .filter(property->!property.isNotPersisted())
        .forEach(property->{
            final ObjectAdapter propertyValueAdapter = property.get(ownerAdapter,
                    InteractionInitiatedBy.FRAMEWORK);
            if(propertyValueAdapter != null) {
                final Object propertyValue = propertyValueAdapter.getObject();

                memento.put(property.getId(), propertyValue);
            }
        });
        
        return memento.asString();
    }
    

}
