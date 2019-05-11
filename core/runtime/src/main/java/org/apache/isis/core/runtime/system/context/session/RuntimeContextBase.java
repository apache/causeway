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
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Tuples.Tuple2;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObjectState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.persistence.FixturesInstalledState;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * 
 * @since 2.0.0-M3
 *
 */
@RequiredArgsConstructor
public abstract class RuntimeContextBase implements RuntimeContext {

	// -- FINAL FIELDS
	
    @Getter protected final IsisConfiguration configuration;
    @Getter protected final ServiceInjector serviceInjector;
    @Getter protected final ServiceRegistry serviceRegistry;
    @Getter protected final SpecificationLoader specificationLoader;
    @Getter protected final AuthenticationSession authenticationSession;
    @Getter protected final ObjectAdapterProvider objectAdapterProvider;
    
    // -- NO ARG CONSTRUCTOR
    
    protected RuntimeContextBase() {
        val mmc = MetaModelContext.current();
    	configuration = mmc.getConfiguration();
        serviceInjector = mmc.getServiceInjector();
        serviceRegistry = mmc.getServiceRegistry();
        specificationLoader = mmc.getSpecificationLoader();
        authenticationSession = mmc.getAuthenticationSession();
        objectAdapterProvider = mmc.getObjectAdapterProvider();
    }
    
    // -- OBJECT ADAPTER SUPPORT
    
    @Override
    public ObjectAdapter adapterOfPojo(Object pojo) {
		return objectAdapterProvider.adapterFor(pojo);
	}
    
    @Override //FIXME [2033] decouple from JDO
    public ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data) {
		return ps().adapterOfMemento(spec, oid, data);
	}
 
    // -- FIXTURE SCRIPT STATE SUPPORT
    
    @Override //FIXME [2033] decouple from JDO
    public FixturesInstalledState getFixturesInstalledState() {
    	return ps().getFixturesInstalledState();
    }
    
    // -- HOMEPAGE LOOKUP SUPPORT
    
    @Override
    public Tuple2<ObjectAdapter, ObjectAction> findHomePageAction() {
    	val finderMixin = new RuntimeContextBase_findHomepage(this);
    	return finderMixin.findHomePageAction();
    }
    
    // -- AUTH
    
    @Override
    public void logoutAuthenticationSession() {
    	// we do the logout (removes this session from those valid)
        // similar code in wicket viewer (AuthenticatedWebSessionForIsis#onInvalidate())
        final AuthenticationSession authenticationSession = getAuthenticationSession();
        IsisContext.getAuthenticationManager().closeSession(authenticationSession);
        IsisContext.getSessionFactory().closeSession();	
    }
    
    // -- ENTITY SUPPORT
    
    @Override
    public ObjectAdapter newTransientInstance(ObjectSpecification domainTypeSpec) {
		return objectAdapterProvider.newTransientInstance(domainTypeSpec);
	}
	
    @Override
    public void makePersistentInTransaction(ObjectAdapter objectAdapter) {
		ps().makePersistentInTransaction(objectAdapter);
	}
    
    @Override
    public Object fetchPersistentPojoInTransaction(RootOid rootOid) {
		return ps().fetchPersistentPojoInTransaction(rootOid);
	}

    @Override
    public ManagedObjectState stateOf(Object domainObject) {
		return ps().stateOf(domainObject);	
	}
    
    // -- PERSISTENCE SUPPORT FOR MANAGED OBJECTS
    
    private final _Lazy<PersistenceSession> persistenceSession = 
            _Lazy.of(IsisContext.getPersistenceSession()::get);
    
    private PersistenceSession ps() {
    	return persistenceSession.get();
    }
    
    // --
	
}
