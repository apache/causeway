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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.ensure.Assert;
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
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.factories.OidFactory;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: provides ObjectAdapterProvider implementation
 * </p> 
 * @since 2.0.0-M2
 */
class ObjectAdapterContext_ObjectAdapterProvider implements ObjectAdapterProvider {
    
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext_ObjectAdapterProvider.class);
    private final ObjectAdapterContext objectAdapterContext;
    private final PersistenceSession persistenceSession;
    private final AuthenticationSession authenticationSession;
    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader; 
    private final IsisJdoMetamodelPlugin isisJdoMetamodelPlugin;
    private final OidFactory oidFactory; 
    
    ObjectAdapterContext_ObjectAdapterProvider(ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession, AuthenticationSession authenticationSession) {
        this.objectAdapterContext = objectAdapterContext;
        this.persistenceSession = persistenceSession;
        this.authenticationSession = authenticationSession;
        this.servicesInjector = persistenceSession.getServicesInjector();
        this.specificationLoader = servicesInjector.getSpecificationLoader();
        this.isisJdoMetamodelPlugin = IsisJdoMetamodelPlugin.get();
        
        this.oidFactory = OidFactory.builder(pojo->specificationLoader.loadSpecification(pojo.getClass()))
                .add(new OidProviders.OidForServices())
                .add(new OidProviders.OidForValues())
                .add(new OidProviders.OidForViewModels())
                .add(new OidProviders.OidForPersistables())
                .add(new OidProviders.OidForMixins())
                .add(new OidProviders.OidForStandaloneCollections())
                .build();
    }

//    @Override
//    public Oid oidFor(Object pojo) {
//        if(pojo == null) {
//            return null;
//        }
//        final Oid persistentOrValueOid = persistentOrValueOid(pojo);
//        if(persistentOrValueOid != null) {
//            return persistentOrValueOid;
//        }
//        final RootOid rootOid = objectAdapterContext.rootOidFor(pojo);
//        
//        return rootOid;
//    }
    
    @Override
    public ObjectAdapter adapterFor(Object pojo) {
        
        if(pojo == null) {
            return null;
        }
        
        final  ObjectAdapter existing = objectAdapterContext.lookupAdapterByPojo(pojo);
        if (existing != null) {
            return existing;
        }
        
        final RootOid rootOid2 = oidFactory.oidFor(pojo);
        
//        if(rootOid2==null) {
//            System.err.println("!!! "+pojo);
//            _Exceptions.throwUnexpectedCodeReach();
//        }
//        
//        if(rootOid2!=null && rootOid2.isValue()) {
//            return objectAdapterContext.getFactories().createRootAdapter(pojo, null);
//        }
        
        // -- legacy code
        
        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }

        final RootOid rootOid = objectAdapterContext.createTransientOrViewModelOid(pojo);
        
      //at this point we know its not a value
        if(rootOid2==null) {
            System.err.println("!!! expected "+rootOid);
            _Exceptions.throwUnexpectedCodeReach();
        }
        
        
        if(rootOid2!=null && rootOid2.isValue()) {
            Assert.assertEquals("expected same", rootOid, null);
        } else if(!rootOid.isTransient()) {
            Assert.assertEquals("expected same", rootOid, rootOid2);    
        }
        
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
    public ObjectSpecification specificationForViewModel(Object viewModelPojo) {
        final ObjectSpecification objectSpecification = 
                specificationLoader.loadSpecification(viewModelPojo.getClass());
        return objectSpecification;
    }
    
    @Override
    public ObjectAdapter disposableAdapterForViewModel(Object viewModelPojo) {
        return objectAdapterContext.disposableAdapterForViewModel(viewModelPojo);
    }

    @Override
    public ObjectAdapter adapterForViewModel(Object viewModelPojo, Function<ObjectSpecId, RootOid> rootOidFactory) {
        return objectAdapterContext.adapterForViewModel(viewModelPojo, rootOidFactory);
    }
    
    protected ObjectAdapter addPersistentToCache(final Object pojo) {
        if (persistenceSession.getPersistenceManager().getObjectId(pojo) == null) {
            return null;
        }
        final RootOid oid = objectAdapterContext.createPersistentOrViewModelOid(pojo);
        final ObjectAdapter adapter = objectAdapterContext.addRecreatedPojoToCache(oid, pojo);
        return adapter;
    }
    
    @Override
    public List<ObjectAdapter> getServices() {
        return serviceAdapters.get();
    }
    
    // -- HELPER
    
    private final _Lazy<List<ObjectAdapter>> serviceAdapters = _Lazy.of(this::initServiceAdapters);
    
    private List<ObjectAdapter> initServiceAdapters() {
        return servicesInjector.streamRegisteredServiceInstances()
        .map(this::adapterFor)
        .peek(serviceAdapter->{
            Assert.assertFalse("expected to not be 'transient'", serviceAdapter.getOid().isTransient());
        })
        .collect(Collectors.toList());
    }
    
//    private Oid persistentOrValueOid(Object pojo) {
//        
//        Oid oid;
//
//        // equivalent to  isInstanceOfPersistable = pojo instanceof Persistable;
//        final boolean isInstanceOfPersistable = isisJdoMetamodelPlugin.isPersistenceEnhanced(pojo.getClass());
//        
//        // pojo may have been lazily loaded by object store, but we haven't yet seen it
//        if (isInstanceOfPersistable) {
//            oid = persistentOid(pojo);
//
//            // TODO: could return null if the pojo passed in !dnIsPersistent() || !dnIsDetached()
//            // in which case, we would ought to map as a transient object, rather than fall through and treat as a value?
//        } else {
//            oid = null;
//        }
//
//        if(oid != null) {
//            return oid;
//        }
//        
//        // need to create (and possibly map) the adapter.
//        final ObjectSpecification objSpec = specificationLoader.loadSpecification(pojo.getClass());
//
//        // we create value facets as standalone (so not added to maps)
//        if (objSpec.containsFacet(ValueFacet.class)) {
//            //TODO[ISIS-1976] don't need an adapter, just its oid
//            oid = objectAdapterContext.getFactories().createStandaloneAdapter(pojo).getOid(); 
//            return oid;
//        }
//
//        return null;
//    }
    
//    private Oid persistentOid(final Object pojo) {
//        if (persistenceSession.getPersistenceManager().getObjectId(pojo) == null) {
//            return null;
//        }
//        final RootOid oid = objectAdapterContext.createPersistentOrViewModelOid(pojo);
//        return oid;
//    }
    
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