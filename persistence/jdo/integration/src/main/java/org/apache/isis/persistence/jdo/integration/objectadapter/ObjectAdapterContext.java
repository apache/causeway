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
package org.apache.isis.persistence.jdo.integration.objectadapter;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.RuntimeContextBase;
import org.apache.isis.persistence.jdo.integration.persistence.IsisPersistenceSessionJdo;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Encapsulate ObjectAdpater life-cycling.  
 *  
 * @since 2.0
 */
@Log4j2
final public class ObjectAdapterContext {

    public static ObjectAdapterContext openContext(
            MetaModelContext mmc,
            IsisPersistenceSessionJdo persistenceSession) {
        
        val objectAdapterContext = 
                new ObjectAdapterContext(mmc, persistenceSession);
        objectAdapterContext.open();
        return objectAdapterContext;
    }

    private final IsisPersistenceSessionJdo persistenceSession; 
    @Getter private final SpecificationLoader specificationLoader;
    private final ObjectAdapterContext_ObjectAdapterProvider objectAdapterProviderMixin;
    private final ObjectAdapterContext_NewIdentifier newIdentifierMixin;
    private final ServiceInjector serviceInjector;

    private ObjectAdapterContext(
            MetaModelContext mmc, 
            IsisPersistenceSessionJdo persistenceSession) {

        val runtimeContext = new RuntimeContextBase(mmc) {};

        this.objectAdapterProviderMixin = new ObjectAdapterContext_ObjectAdapterProvider(this, runtimeContext);
        this.newIdentifierMixin = new ObjectAdapterContext_NewIdentifier(persistenceSession, runtimeContext.getSpecificationLoader());

        this.persistenceSession = persistenceSession;
        this.specificationLoader = mmc.getSpecificationLoader();
        this.serviceInjector = mmc.getServiceInjector();
    }

    // -- DEBUG

    void printContextInfo(String msg) {
        if(log.isDebugEnabled()) {
            String id = Integer.toHexString(this.hashCode());
            String session = ""+persistenceSession;
            log.debug(String.format("%s id=%s session='%s'", msg, id, session));
        }
    }

    // -- LIFE-CYCLING

    private void open() {
        printContextInfo("OPEN_");
    }

    public void close() {
        printContextInfo("CLOSE");
    }

    // -- NEW IDENTIFIER

    public RootOid createPersistentOrViewModelOid(Object pojo) {
        return newIdentifierMixin.createPersistentOid(pojo);
    }

    // -- ADAPTER MANAGER LEGACY

    public ManagedObject fetchPersistent(final Object pojo) {
        if (persistenceSession.getJdoPersistenceManager().getObjectId(pojo) == null) {
            return null;
        }
        final RootOid oid = createPersistentOrViewModelOid(pojo);
        final ManagedObject adapter = recreatePojo(oid, pojo);
        return adapter;
    }

    public ManagedObject recreatePojo(RootOid oid, Object recreatedPojo) {
        final ManagedObject createdAdapter = createRootOrAggregatedAdapter(oid, recreatedPojo);
        return injectServices(createdAdapter);
    }

    // package private
    ManagedObject injectServices(final @NonNull ManagedObject adapter) {
        val spec = adapter.getSpecification();
        if(spec==null 
                || spec.isValue()) {
            return adapter; // guard against value objects
        }
        val pojo = adapter.getPojo();
        serviceInjector.injectServicesInto(pojo);
        return adapter;
    }

    private ManagedObject createRootOrAggregatedAdapter(final RootOid oid, final Object pojo) {
        if(oid instanceof RootOid) {
            final RootOid rootOid = (RootOid) oid;
            return PojoAdapter.of(pojo, rootOid, getSpecificationLoader());
        } 
        throw _Exceptions.illegalArgument("Parented Oids are no longer supported.");
    }

    // -- OBJECT ADAPTER PROVIDER SUPPORT

    public ObjectAdapterProvider getObjectAdapterProvider() {
        return objectAdapterProviderMixin;
    }


}