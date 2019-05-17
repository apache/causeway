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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.session.RuntimeContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.factories.OidFactory;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: provides ObjectAdapterProvider implementation
 * </p> 
 * @since 2.0.0-M2
 */
class ObjectAdapterContext_ObjectAdapterProvider implements ObjectAdapterProvider {
    
    private final ObjectAdapterContext objectAdapterContext;
    private final RuntimeContext runtimeContext;
    private final SpecificationLoader specificationLoader; 
    private final OidFactory oidFactory; 
    
    ObjectAdapterContext_ObjectAdapterProvider(
            ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession, 
            RuntimeContext runtimeContext) {
        
        this.objectAdapterContext = objectAdapterContext;
        this.runtimeContext = runtimeContext;
        this.specificationLoader = runtimeContext.getSpecificationLoader();
        
        this.oidFactory = OidFactory.builder(pojo->specificationLoader.loadSpecification(pojo.getClass()))
                .add(new ObjectAdapterContext_OidProviders.GuardAgainstRootOid())
                .add(new ObjectAdapterContext_OidProviders.OidForServices())
                .add(new ObjectAdapterContext_OidProviders.OidForValues())
                .add(new ObjectAdapterContext_OidProviders.OidForViewModels())
                .add(new ObjectAdapterContext_OidProviders.OidForPersistent())
                .add(new ObjectAdapterContext_OidProviders.OidForOthers())
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
    public ObjectAdapter adapterForBean(BeanAdapter bean) {
        return ObjectAdapterForBean.of(bean, specificationLoader);
    }
    
    
    @Override
    public ObjectAdapter adapterForCollection(Object pojo, RootOid parentOid, OneToManyAssociation collection) {

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
    public ManagedObject disposableAdapterForViewModel(final Object viewModelPojo) {
        return ManagedObject.of(()->specificationLoader.loadSpecification(viewModelPojo.getClass()), viewModelPojo);
    }

    @Override
    public ObjectAdapter adapterForViewModel(Object viewModelPojo, String mementoString) {
        return objectAdapterContext.adapterForViewModel(viewModelPojo, mementoString);
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
    public Stream<ObjectAdapter> streamServices() {
        return serviceAdapters.get().values().stream();
    }
    
    @Override
    public ObjectAdapter lookupService(final String serviceId) {
        return serviceAdapters.get().get(serviceId);
    }
    
    
    // -- HELPER
    
    private final _Lazy<Map<String, ObjectAdapter>> serviceAdapters = _Lazy.of(this::initServiceAdapters);
    
    private Map<String, ObjectAdapter> initServiceAdapters() {
        
        return runtimeContext.getServiceRegistry().streamRegisteredBeans()
        .map(this::adapterForBean)
        .peek(serviceAdapter->{
            Assert.assertFalse("expected to not be 'transient'", serviceAdapter.getOid().isTransient());
        })
        .collect(Collectors.toMap(ServiceUtil::idOfAdapter, v->v, (o,n)->n, LinkedHashMap::new));
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class ObjectAdapterForBean implements ObjectAdapter {
        
        private final BeanAdapter bean;
        private final SpecificationLoader specificationLoader;
        
        private ObjectSpecification spec;

        @Override
        public ObjectSpecification getSpecification() {
            if(spec==null) {
                spec = specificationLoader.loadSpecification(bean.getBeanClass());
            }
            return spec;
        }

        @Override
        public Object getPojo() {
            return bean.getInstance().iterator().next();
        }

        @Override
        public void checkLock(Version version) {
        }

        @Override
        public Oid getOid() {
            val spec = getSpecification();
            return Oid.Factory.persistentOf(spec.getSpecId(), bean.getId());
        }

        @Override
        public ObjectAdapter getAggregateRoot() {
            return this;
        }

        @Override
        public Version getVersion() {
            return null;
        }

        @Override
        public void setVersion(Version version) {
        }

        @Override
        public boolean isTransient() {
            return false;
        }

        @Override
        public boolean isRepresentingPersistent() {
            return false;
        }

        @Override
        public boolean isDestroyed() {
            return false;
        }
        
        @Override
        public String toString() {
            return "ObjectAdapterForBean[spec="+spec+"]";
        }
        
    }
    
   
}