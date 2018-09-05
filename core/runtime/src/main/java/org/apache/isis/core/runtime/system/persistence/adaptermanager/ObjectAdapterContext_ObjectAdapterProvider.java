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
package org.apache.isis.core.runtime.system.persistence.adaptermanager;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.IsisJdoMetamodelPlugin;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: provides ObjectAdapterProvider implementation
 * </p> 
 * @since 2.0.0-M2
 */
class ObjectAdapterContext_ObjectAdapterProvider implements ObjectAdapterProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext_ObjectAdapterProvider.class);
    private final ObjectAdapterContext objectAdapterContext;
    private final PersistenceSession persistenceSession;
    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader; 
    private final IsisJdoMetamodelPlugin isisJdoMetamodelPlugin; 
    
    ObjectAdapterContext_ObjectAdapterProvider(ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession) {
        this.objectAdapterContext = objectAdapterContext;
        this.persistenceSession = persistenceSession;
        this.servicesInjector = persistenceSession.getServicesInjector();
        this.specificationLoader = servicesInjector.getSpecificationLoader();
        this.isisJdoMetamodelPlugin = IsisJdoMetamodelPlugin.get();
    }

    @Override
    public Oid oidFor(Object pojo) {
        if(pojo == null) {
            return null;
        }
        final Oid persistentOrValueOid = persistentOrValueOid(pojo);
        if(persistentOrValueOid != null) {
            return persistentOrValueOid;
        }
        // Creates a new transient root for the supplied domain object
        final RootOid rootOid = persistenceSession.createTransientOrViewModelOid(pojo);
        return rootOid;
    }
    
    @Override
    public ObjectAdapter adapterFor(Object pojo) {
        
        if(pojo == null) {
            return null;
        }
        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }

        // Creates a new transient root {@link ObjectAdapter adapter} for the supplied domain
        final RootOid rootOid = persistenceSession.createTransientOrViewModelOid(pojo);
        final ObjectAdapter newAdapter = objectAdapterContext.getFactories().createRootAdapter(pojo, rootOid);

        return objectAdapterContext.mapAndInjectServices(newAdapter);
    }

    @Override
    public ObjectAdapter adapterFor(Object pojo, ObjectAdapter parentAdapter, OneToManyAssociation collection) {

        assert parentAdapter != null;
        assert collection != null;

        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }

        objectAdapterContext.ensureMapsConsistent(parentAdapter);

        // the List, Set etc. instance gets wrapped in its own adapter
        final ObjectAdapter newAdapter = objectAdapterContext.getFactories()
                .createCollectionAdapter(pojo, parentAdapter, collection);

        return objectAdapterContext.mapAndInjectServices(newAdapter);
    }

    @Override
    public ObjectAdapter specificationForViewModel(Object viewModelPojo) {
        return objectAdapterContext.specificationForViewModel(viewModelPojo);
    }

    @Override
    public ObjectAdapter adapterForViewModel(Object viewModelPojo, Function<ObjectSpecId, RootOid> rootOidFactory) {
        return objectAdapterContext.adapterForViewModel(viewModelPojo, rootOidFactory);
    }
    
    protected ObjectAdapter addPersistentToCache(final Object pojo) {
        if (persistenceSession.getPersistenceManager().getObjectId(pojo) == null) {
            return null;
        }
        final RootOid oid = persistenceSession.createPersistentOrViewModelOid(pojo);
        final ObjectAdapter adapter = objectAdapterContext.addRecreatedPojoToCache(oid, pojo);
        return adapter;
    }
    
    @Override
    public List<ObjectAdapter> getServices() {
        final List<Object> services = servicesInjector.getRegisteredServices();
        final List<ObjectAdapter> serviceAdapters = _Lists.newArrayList();
        for (final Object servicePojo : services) {
            ObjectAdapter serviceAdapter = objectAdapterContext.lookupAdapterFor(servicePojo);
            if(serviceAdapter == null) {
                throw new IllegalStateException("ObjectAdapter for service " + servicePojo + " does not exist?!?");
            }
            serviceAdapters.add(serviceAdapter);
        }
        return serviceAdapters;
    }
    
    // -- HELPER
    
    private Oid persistentOrValueOid(Object pojo) {
        
        Oid oid;

        // equivalent to  isInstanceOfPersistable = pojo instanceof Persistable;
        final boolean isInstanceOfPersistable = isisJdoMetamodelPlugin.isPersistenceEnhanced(pojo.getClass());
        
        // pojo may have been lazily loaded by object store, but we haven't yet seen it
        if (isInstanceOfPersistable) {
            oid = persistentOid(pojo);

            // TODO: could return null if the pojo passed in !dnIsPersistent() || !dnIsDetached()
            // in which case, we would ought to map as a transient object, rather than fall through and treat as a value?
        } else {
            oid = null;
        }

        if(oid != null) {
            return oid;
        }
        
        // need to create (and possibly map) the adapter.
        final ObjectSpecification objSpec = specificationLoader.loadSpecification(pojo.getClass());

        // we create value facets as standalone (so not added to maps)
        if (objSpec.containsFacet(ValueFacet.class)) {
            //TODO[ISIS-1976] don't need an adapter, just its oid
            oid = objectAdapterContext.getFactories().createStandaloneAdapter(pojo).getOid(); 
            return oid;
        }

        return null;
    }
    
    protected Oid persistentOid(final Object pojo) {
        if (persistenceSession.getPersistenceManager().getObjectId(pojo) == null) {
            return null;
        }
        final RootOid oid = persistenceSession.createPersistentOrViewModelOid(pojo);
        return oid;
    }
    
    private ObjectAdapter existingOrValueAdapter(Object pojo) {

        // attempt to locate adapter for the pojo
        ObjectAdapter adapter = objectAdapterContext.lookupAdapterByPojo(pojo);
        if (adapter != null) {
            return adapter;
        }

        // equivalent to  isInstanceOfPersistable = pojo instanceof Persistable;
        final boolean isInstanceOfPersistable = isisJdoMetamodelPlugin.isPersistenceEnhanced(pojo.getClass());
        
        // pojo may have been lazily loaded by object store, but we haven't yet seen it
        if (isInstanceOfPersistable) {
            adapter = addPersistentToCache(pojo);

            // TODO: could return null if the pojo passed in !dnIsPersistent() || !dnIsDetached()
            // in which case, we would ought to map as a transient object, rather than fall through and treat as a value?
        } else {
            adapter = null;
        }

        if(adapter != null) {
            return adapter;
        }
        
        // need to create (and possibly map) the adapter.
        final ObjectSpecification objSpec = specificationLoader.loadSpecification(pojo.getClass());

        // we create value facets as standalone (so not added to maps)
        if (objSpec.containsFacet(ValueFacet.class)) {
            adapter = objectAdapterContext.getFactories().createStandaloneAdapter(pojo);
            return adapter;
        }

        return null;
    }


    
   
}