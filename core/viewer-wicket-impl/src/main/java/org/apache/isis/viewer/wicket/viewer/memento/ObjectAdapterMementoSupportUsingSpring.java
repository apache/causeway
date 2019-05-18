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
package org.apache.isis.viewer.wicket.viewer.memento;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.core.runtime.memento.ObjectAdapterMementoSupport;
import org.apache.isis.core.runtime.memento.ObjectAdapterMementoUsingSupport;
import org.apache.isis.core.runtime.persistence.adapter.ObjectAdapterForBean;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;

/**
 * 
 * @since 2.0.0
 *
 */
@Singleton
public class ObjectAdapterMementoSupportUsingSpring implements ObjectAdapterMementoSupport {

//    @Getter(lazy=true) private final BeanSortClassifier beanSortClassifier = IsisBeanTypeRegistry.current();

    @Override
    public ObjectAdapterMemento mementoForRootOid(RootOid rootOid) {
        requires(rootOid, "rootOid");
        
        val spec = specificationLoader.lookupBySpecIdElseLoad(rootOid.getObjectSpecId());
        val sort = spec.getBeanSort();
        return ObjectAdapterMementoUsingSupport.of(null, sort, spec.getSpecId(), rootOid);
    }

    @Override
    public ObjectAdapterMemento mementoForAdapter(ObjectAdapter adapter) {
        requires(adapter, "adapter");
        val spec = adapter.getSpecification();
        return mementoForPojo(adapter.getPojo(), spec);
    }

    @Override
    public ObjectAdapterMemento mementoForPojo(Object pojo) {
        requires(pojo, "pojo");
        val spec = specificationLoader.loadSpecification(pojo.getClass());
        return mementoForPojo(pojo, spec);
    }
    
    private ObjectAdapterMemento mementoForPojo(Object pojo, ObjectSpecification spec) {
        
        val specId = spec.getSpecId();
        val sort = spec.getBeanSort();
        
        val msg = String.format("Storing %s of type %s [%s].", 
                sort, 
                specId,
                spec.getCorrespondingClass()
                );
        probe.println(msg);
        
        switch (sort) {
        case BEAN:
            return ObjectAdapterMementoUsingSupport.of(null, sort, spec.getSpecId(), null);

        case ENTITY:
            
          val isisSession = IsisSession.currentOrElseNull();
          val adapterProvider = isisSession.getObjectAdapterProvider();
          
//          val ps = IsisContext.getPersistenceSession().get();
//          ps.refreshRoot(pojo);
          
          val objectAdapter = adapterProvider.adapterFor(pojo);
          val rootOid = (RootOid)objectAdapter.getOid();
          
          if(rootOid.isPersistent()) {
              return mementoForRootOid(rootOid);    
          } 
          // else fall through
          
            
        case VALUE:
        case VIEW_MODEL:
        case MIXIN:
        case COLLECTION:
        case UNKNOWN: // simple types like String, should be VALUE
            
            val key = UUID.randomUUID();
            
            mementoStore.put(key, pojo);
            
            return ObjectAdapterMementoUsingSupport.of(key, sort, spec.getSpecId(), null);
            
        default:
            break;
        }
                
        val msg2 = String.format("Cannot store %s of type %s [%s].", 
                sort, 
                specId,
                spec.getCorrespondingClass()
                );
        throw _Exceptions.unrecoverable(msg2);
        
    }
    

    _Probe probe = _Probe.unlimited().label("ObjectAdapterMementoSupportUsingSpring");

    @Override
    public ObjectAdapter reconstructObjectAdapter(ObjectAdapterMemento memento) {
        requires(memento, "memento");
        
        val specId = memento.getObjectSpecId(); 
        val spec = specificationLoader.lookupBySpecIdElseLoad(specId);
        val sort = memento.getBeanSort();
        
        probe.println("reconstruct [%s] %s", sort, memento.getObjectSpecId());
        
        //TODO[2112] if always equal remove field 'beanSort' from ObjectAdapterMemento
        Assert.assertEquals("expected same sort", sort, spec.getBeanSort());
        
        val key = memento.getStoreKey();
        
        // we need get some meta info on the object, deciding how to
        switch (sort) {
        case BEAN:
            
            val bean = serviceRegistry.lookupRegisteredBeanByNameElseFail(specId.asString());
            return ObjectAdapterForBean.of(bean, specificationLoader);
        
        case ENTITY:
            
            if(key==null) {
            
                probe.println("fetch from persistence oid='%s'", memento.getRootOid());
                val rootOid = memento.getRootOid();
                Assert.assertTrue("expected persistent", rootOid.isPersistent());
                val ps = IsisContext.getPersistenceSession().get();
                return ps.getObjectAdapterByIdProvider().adapterFor(rootOid);
            } else {
                
                val pojo = mementoStore.get(key);
                return PojoAdapter.ofTransient(pojo, specId);
                
//                val ps = IsisContext.getPersistenceSession().get();
//                
//                ps.refreshRoot(pojo);
//                val adapter = ps.getObjectAdapterProvider().adapterFor(pojo);
//                Assert.assertTrue("expected persistent", adapter.getOid().isPersistent());
//                return adapter;
            }
            
        case VALUE:
        case VIEW_MODEL:
        case MIXIN:
        case COLLECTION:
        case UNKNOWN:
            
            val pojo = mementoStore.get(key);
            if(pojo==null) {
                throw _Exceptions.unrecoverable("cannot find pojo for " + specId);
            }
            return PojoAdapter.of(pojo, Oid.Factory.persistentOf(specId, "deprecated"));
            
        default:
            break;
        }
        val msg = String.format("Cannot handle %s of type %s [%s].", 
                sort, 
                specId,
                spec.getCorrespondingClass()
                );
        throw _Exceptions.unrecoverable(msg);
        
    }

    // -- naive cache
    
    private final static Map<UUID, Object> mementoStore = new ConcurrentHashMap<>();
    
    // -- DEPS
    
    //@Inject private WebSessionManager webSessionManager;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private ServiceRegistry serviceRegistry;
    
    // --


}
