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
package org.apache.isis.core.runtime.system.context.session;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Tuples;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObjectState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.persistence.FixturesInstalledState;
import org.apache.isis.core.security.authentication.AuthenticationSession;

/**
 * TODO [2033] this was introduced for refactoring, use MetaModelContext instead if possible
 *  
 * @since 2.0.0-M3
 * 
 */
public interface RuntimeContext {

	AuthenticationSession getAuthenticationSession();
    IsisConfiguration getConfiguration();
    SpecificationLoader getSpecificationLoader();
    ServiceInjector getServiceInjector();
    ServiceRegistry getServiceRegistry();
    
	//Stream<ObjectAdapter> streamServiceAdapters();
    
	_Tuples.Tuple2<ObjectAdapter, ObjectAction> findHomePageAction(); //TODO [2033] there's also a HomepageService
	
	//ObjectAdapter lookupService(String serviceId);
	
	ObjectAdapter adapterOfPojo(Object pojo);
	ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data);
	
	ObjectAdapter newTransientInstance(ObjectSpecification domainTypeSpec);
	
	void makePersistentInTransaction(ObjectAdapter objectAdapter);
	Object fetchPersistentPojoInTransaction(RootOid rootOid);
	
	ManagedObjectState stateOf(Object domainObject);
	FixturesInstalledState getFixturesInstalledState();
	
	void logoutAuthenticationSession();
	
	
}
