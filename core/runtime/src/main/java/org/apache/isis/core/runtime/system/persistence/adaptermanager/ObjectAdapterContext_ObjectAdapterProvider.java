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

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
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
//    private final PersistenceSession persistenceSession;
    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader; 
    private final OidFactory oidFactory; 
    
    ObjectAdapterContext_ObjectAdapterProvider(ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession) {
        this.objectAdapterContext = objectAdapterContext;
//        this.persistenceSession = persistenceSession;
        this.servicesInjector = persistenceSession.getServicesInjector();
        this.specificationLoader = servicesInjector.getSpecificationLoader();
        
        this.oidFactory = OidFactory.builder(pojo->specificationLoader.loadSpecification(pojo.getClass()))
                .add(new OidProviders.GuardAgainstRootOid())
                .add(new OidProviders.OidForServices())
                .add(new OidProviders.OidForValues())
                .add(new OidProviders.OidForViewModels())
                .add(new OidProviders.OidForPersistent())
                .add(new OidProviders.OidForOthers())
                .build();
    }

    @Override
    public ObjectAdapter adapterFor(Object pojo) {

        if(pojo == null) {
            return null;
        }
        
        final RootOid rootOid = oidFactory.oidFor(pojo);
        final ObjectAdapter newAdapter = objectAdapterContext.getFactories().createRootAdapter(pojo, rootOid);
        return objectAdapterContext.injectServices(newAdapter);
    }
    
    
    @Override
    public ObjectAdapter adapterFor(Object pojo, RootOid parentOid, OneToManyAssociation collection) {

        requires(parentOid, "parentOid");
        requires(collection, "collection");

        // the List, Set etc. instance gets wrapped in its own adapter
        final ObjectAdapter newAdapter = objectAdapterContext.getFactories()
                .createCollectionAdapter(pojo, parentOid, collection);

        return objectAdapterContext.injectServices(newAdapter);
    }

    @Override
    public ObjectSpecification specificationForViewModel(Object viewModelPojo) {
        final ObjectSpecification objectSpecification = 
                specificationLoader.loadSpecification(viewModelPojo.getClass());
        return objectSpecification;
    }
    
    @Override
    public ObjectAdapter disposableAdapterForViewModel(Object viewModelPojo) {
        final ObjectSpecification objectSpecification = 
                specificationLoader.loadSpecification(viewModelPojo.getClass());
        final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
        final RootOid newRootOid = RootOid.create(objectSpecId, UUID.randomUUID().toString());
        final ObjectAdapter createdAdapter = objectAdapterContext.createRootOrAggregatedAdapter(newRootOid, viewModelPojo);
        return createdAdapter;
    }

    @Override
    public ObjectAdapter adapterForViewModel(Object viewModelPojo, Function<ObjectSpecId, RootOid> rootOidFactory) {
        return objectAdapterContext.adapterForViewModel(viewModelPojo, rootOidFactory);
    }
    
    // -- DOMAIN OBJECT CREATION SUPPORT
    
    @Override
    public ObjectAdapter newTransientInstance(ObjectSpecification objectSpec) {
        return objectAdapterContext.objectCreationMixin.newInstance(objectSpec);
    }
    
    @Override
    public ObjectAdapter recreateViewModelInstance(ObjectSpecification objectSpec, final String memento) {
        return objectAdapterContext.objectCreationMixin.recreateInstance(objectSpec, memento);
    }
    
    // -- SERVICE SUPPORT
    
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
    
   
}