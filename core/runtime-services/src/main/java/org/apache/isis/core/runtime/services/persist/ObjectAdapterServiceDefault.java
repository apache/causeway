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
package org.apache.isis.core.runtime.services.persist;

import java.io.Serializable;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.persistsession.ObjectAdapterService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;

@Singleton
public class ObjectAdapterServiceDefault implements ObjectAdapterService {


	@Override
	public ObjectAdapter adapterFor(Object pojo) {

		if(pojo == null) {
			return null;
		}

		val spec = specificationLoader.loadSpecification(pojo.getClass());
		switch (spec.getManagedObjectSort()) {
		case VALUE:
			return PojoAdapter.ofValue((Serializable) pojo);
		case VIEW_MODEL:
		case BEAN:
		case ENTITY:
			return ps().adapterFor(pojo);
		case MIXIN:
		case COLLECTION:
			return PojoAdapter.ofTransient(pojo, spec.getSpecId());
        case UNKNOWN:
            break;
            
		}
		
		_Probe.errOut("UNKNOWN ManagedObject type: '%s'", pojo.getClass());
		
		return null;
		//throw _Exceptions.unmatchedCase("UNKNOWN ManagedObject type: " + pojo.getClass());
	}
	
    @Override
    public ObjectAdapter adapterForBean(BeanAdapter bean) {
        return ps().adapterForBean(bean);
    }

	@Override
	public ObjectAdapter adapterForCollection(
			Object collectionPojo, 
			RootOid parentOid, 
			OneToManyAssociation oneToMany) {
		
        requires(parentOid, "parentOid");
        requires(collectionPojo, "collectionPojo");
		
		val collectionOid = Oid.Factory.parentedOfOneToMany(parentOid, oneToMany);
		
        // the List, Set etc. instance gets wrapped in its own adapter
        val newAdapter = PojoAdapter.of(collectionPojo, collectionOid); 
        return newAdapter;
	}

	@Override
	public ObjectAdapter adapterForViewModel(Object viewModelPojo, String mementoStr) {
		
		val objectSpecification = specificationLoader.loadSpecification(viewModelPojo.getClass());
        val objectSpecId = objectSpecification.getSpecId();
        val newRootOid = Oid.Factory.viewmodelOf(objectSpecId, mementoStr);
        val newAdapter = PojoAdapter.of(viewModelPojo, newRootOid); 
        return newAdapter; 
	}

	@Override
	public ObjectAdapter newTransientInstance(ObjectSpecification spec) {
		val pojo = spec.instantiatePojo();
        val newAdapter = PojoAdapter.ofTransient(pojo, spec.getSpecId());
        newAdapter.injectServices(serviceInjector);
        return newAdapter;
	}

	// -- HELPER

	protected PersistenceSession ps() {
		return IsisContext.getPersistenceSession()
				.orElseThrow(()->new NonRecoverableException("No PersistenceSession on current thread."));
	}
	
	@Inject SpecificationLoader specificationLoader;
	@Inject ServiceInjector serviceInjector;

    @Override
    public ManagedObject disposableAdapterForViewModel(Object viewModelPojo) {
        return ps().disposableAdapterForViewModel(viewModelPojo);
    }

    @Override
    public ObjectSpecification specificationForViewModel(Object viewModelPojo) {
        return ps().specificationForViewModel(viewModelPojo);
    }

    @Override
    public ObjectAdapter recreateViewModelInstance(ObjectSpecification objectSpec, String memento) {
        return ps().recreateViewModelInstance(objectSpec, memento);
    }

    @Override
    public Stream<ObjectAdapter> streamServices() {
        return ps().streamServices();
    }

    @Override
    public ObjectAdapter lookupService(String serviceId) {
        return ps().lookupService(serviceId);

    }


}
