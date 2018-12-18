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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
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
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class RecreatableObjectFacetDeclarativeInitializingAbstract extends RecreatableObjectFacetAbstract {

    private final SpecificationLoader specificationLoader;
    private final AdapterManager adapterManager;

    public RecreatableObjectFacetDeclarativeInitializingAbstract(
            final FacetHolder holder,
            final RecreationMechanism recreationMechanism,
            final SpecificationLoader specificationLoader,
            final AdapterManager adapterManager,
            final ServicesInjector servicesInjector,
            final PostConstructMethodCache postConstructMethodCache) {
        super(holder, recreationMechanism, postConstructMethodCache, servicesInjector);
        this.specificationLoader = specificationLoader;
        this.adapterManager = adapterManager;
    }

    @Override
    protected void doInitialize(
            final Object viewModelPojo,
            final String mementoStr) {

        final MementoService mementoService = servicesInjector.lookupService(MementoService.class);
        final BookmarkService bookmarkService = servicesInjector.lookupService(BookmarkService.class);

        final MementoService.Memento memento = mementoService.parse(mementoStr);

        final Set<String> mementoKeys = memento.keySet();

        // manually recreate the adapter in order to be able to query state via the metamodel
        ObjectAdapter viewModelAdapter = adapterManager.getAdapterFor(viewModelPojo);
        if(viewModelAdapter == null) {
            final ObjectSpecification objectSpecification = specificationLoader.loadSpecification(viewModelPojo.getClass());
            final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
            viewModelAdapter = adapterManager.mapRecreatedPojo(new RootOid(objectSpecId, mementoStr, Oid.State.VIEWMODEL), viewModelPojo);
        }

        final ObjectSpecification spec = viewModelAdapter.getSpecification();
        final List<OneToOneAssociation> properties = spec.getProperties(Contributed.EXCLUDED);
        for (OneToOneAssociation property : properties) {
            final String propertyId = property.getId();

            Object propertyValue = null;

            if(mementoKeys.contains(propertyId)) {
                final Class<?> propertyType = property.getSpecification().getCorrespondingClass();
                propertyValue = memento.get(propertyId, propertyType);
            } else if(mementoKeys.contains(propertyId + ".bookmark")) {
                final Bookmark propertyValueBookmark = memento.get(propertyId + ".bookmark", Bookmark.class);
                propertyValue = bookmarkService.lookup(propertyValueBookmark);
            }

            if(propertyValue != null) {
                property.set(viewModelAdapter, adapterManager.adapterFor(propertyValue), InteractionInitiatedBy.FRAMEWORK);
            }
        }
    }
    
    @Override
    public String memento(Object viewModelPojo) {

        final MementoService mementoService = servicesInjector.lookupService(MementoService.class);
        final BookmarkService bookmarkService = servicesInjector.lookupService(BookmarkService.class);

        final MementoService.Memento memento = mementoService.create();

        // this is horrible, but there's a catch-22 here...
        // we need an adapter in order to query the state of the object via the metamodel, on the other hand
        // we can't create an adapter without the identifier, which is what we're trying to derive
        // so... we create a temporary transient adapter, use it to wrap this adapter and interrogate this pojo,
        // then throw away that adapter (remove from the adapter map)
        boolean createdTemporaryAdapter = false;
        ObjectAdapter viewModelAdapter = adapterManager.getAdapterFor(viewModelPojo);
        if(viewModelAdapter == null) {
            final ObjectSpecification objectSpecification = specificationLoader.loadSpecification(viewModelPojo.getClass());
            final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
            viewModelAdapter = adapterManager.mapRecreatedPojo(RootOid.create(objectSpecId, UUID.randomUUID().toString()), viewModelPojo);

            createdTemporaryAdapter = true;
        }

        try {
            final ObjectSpecification spec = viewModelAdapter.getSpecification();
            final List<OneToOneAssociation> properties = spec.getProperties(Contributed.EXCLUDED);
            for (OneToOneAssociation property : properties) {
                // ignore read-only
                if(!property.containsDoOpFacet(PropertySetterFacet.class)) {
                    continue;
                }
                // ignore those explicitly annotated as @NotPersisted
                if(property.isNotPersisted()) {
                    continue;
                }

                // otherwise, include

                // REVIEW: this look to be the same as viewModelAdapter, above?
                final ObjectAdapter ownerAdapter = adapterManager.adapterFor(viewModelPojo);

                final ObjectAdapter propertyValueAdapter = property.get(ownerAdapter,
                        InteractionInitiatedBy.FRAMEWORK);
                if(propertyValueAdapter != null) {
                    final Object propertyValue = propertyValueAdapter.getObject();
                    if(mementoService.canSet(propertyValue)) {
                        memento.set(property.getId(), propertyValue);
                    } else {
                        final Bookmark propertyValueBookmark = bookmarkService.bookmarkFor(propertyValue);
                        memento.set(property.getId() + ".bookmark", propertyValueBookmark);
                    }
                }
            }
            return memento.asString();
        } finally {
            if(createdTemporaryAdapter) {
                adapterManager.removeAdapter(viewModelAdapter);
            }
        }
    }


}
