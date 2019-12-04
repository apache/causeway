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
package org.apache.isis.persistence.jdo.objectadapter;

import java.util.Objects;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.persistence.jdo.persistence.IsisPersistenceSessionJdo;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.metamodel.objectmanager.ObjectManager;
import org.apache.isis.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.session.RuntimeContextBase;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.security.api.authentication.AuthenticationSession;

import lombok.Getter;
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
            AuthenticationSession authenticationSession, 
            IsisPersistenceSessionJdo persistenceSession) {
        
        val objectAdapterContext = 
                new ObjectAdapterContext(mmc, authenticationSession, persistenceSession);
        objectAdapterContext.open();
        return objectAdapterContext;
    }

    private final IsisPersistenceSessionJdo persistenceSession; 
    @Getter private final SpecificationLoader specificationLoader;
    private final ObjectAdapterContext_ObjectAdapterProvider objectAdapterProviderMixin;
    private final ObjectAdapterContext_NewIdentifier newIdentifierMixin;
    private final ObjectAdapterContext_DependencyInjection dependencyInjectionMixin;
    private final ServiceInjector serviceInjector;
    final ObjectAdapterContext_ObjectCreation objectCreationMixin;
    private final ObjectAdapterContext_LifecycleEventSupport lifecycleEventMixin;
    private final ObjectManager objectManager;

    private ObjectAdapterContext(
            MetaModelContext mmc,
            AuthenticationSession authenticationSession, 
            IsisPersistenceSessionJdo persistenceSession) {

        val runtimeContext = new RuntimeContextBase(mmc) {};

        this.objectAdapterProviderMixin = new ObjectAdapterContext_ObjectAdapterProvider(this, runtimeContext);
        this.newIdentifierMixin = new ObjectAdapterContext_NewIdentifier(persistenceSession, runtimeContext.getSpecificationLoader());
        this.dependencyInjectionMixin = new ObjectAdapterContext_DependencyInjection(runtimeContext);
        this.objectCreationMixin = new ObjectAdapterContext_ObjectCreation(this, runtimeContext);
        this.lifecycleEventMixin = new ObjectAdapterContext_LifecycleEventSupport(runtimeContext);

        this.objectManager = mmc.getObjectManager();
        
        this.persistenceSession = persistenceSession;
        this.specificationLoader = mmc.getSpecificationLoader();
        this.serviceInjector = mmc.getServiceInjector();

        this.objectAdapterFactories = new ObjectAdapterContext_Factories(persistenceSession);
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

    // -- DEPENDENCY INJECTION

    // package private
    Object instantiateAndInjectServices(ObjectSpecification objectSpec) {
        
        val objectCreateRequest = ObjectCreator.Request.of(objectSpec);
        return objectManager.createObject(objectCreateRequest);
        
        // legacy of
        //return dependencyInjectionMixin.instantiateAndInjectServices(objectSpec);
    }

    // -- FACTORIES

    // package private
    static interface ObjectAdapterFactories {

        /**
         * Creates (but does not {@link #mapAndInjectServices(ObjectAdapter) map}) a new
         * root {@link ObjectAdapter adapter} for the supplied domain object.
         *
         * @see #createStandaloneAdapter(Object)
         * @see #createCollectionAdapter(Object, ParentedOid)
         */
        ObjectAdapter createRootAdapter(Object pojo, RootOid rootOid);

        ObjectAdapter createCollectionAdapter(Object pojo, ParentedOid collectionOid);

        /**
         * Creates an {@link ObjectAdapter adapter} to represent a collection
         * of the parent.
         *
         * <p>
         * The returned adapter will have a {@link ParentedOid}; its version
         * and its persistence are the same as its owning parent.
         *
         * <p>
         * Should only be called if the pojo is known not to be
         * {@link #lookupAdapterFor(Object) mapped}.
         */
        ObjectAdapter createCollectionAdapter(Object pojo, RootOid parentOid, OneToManyAssociation otma);
    }

    private final ObjectAdapterFactories objectAdapterFactories;

    // package private
    ObjectAdapterFactories getFactories() {
        return objectAdapterFactories;
    }

    // -- ADAPTER MANAGER LEGACY

    public ObjectAdapter fetchPersistent(final Object pojo) {
        if (persistenceSession.getJdoPersistenceManager().getObjectId(pojo) == null) {
            return null;
        }
        final RootOid oid = createPersistentOrViewModelOid(pojo);
        final ObjectAdapter adapter = recreatePojo(oid, pojo);
        return adapter;
    }

    public ObjectAdapter recreatePojo(Oid oid, Object recreatedPojo) {
        final ObjectAdapter createdAdapter = createRootOrAggregatedAdapter(oid, recreatedPojo);
        return injectServices(createdAdapter);
    }

    // package private
    ObjectAdapter injectServices(final ObjectAdapter adapter) {
        Objects.requireNonNull(adapter);
        if(adapter.getOid().isValue()) {
            return adapter; // guard against value objects
        }
        final Object pojo = adapter.getPojo();
        serviceInjector.injectServicesInto(pojo);
        return adapter;
    }

    // package private
    ObjectAdapter createRootOrAggregatedAdapter(final Oid oid, final Object pojo) {
        final ObjectAdapter createdAdapter;
        if(oid instanceof RootOid) {
            final RootOid rootOid = (RootOid) oid;
            createdAdapter = getFactories().createRootAdapter(pojo, rootOid);
        } else /*if (oid instanceof CollectionOid)*/ {
            final ParentedOid collectionOid = (ParentedOid) oid;
            createdAdapter = getFactories().createCollectionAdapter(pojo, collectionOid);
        }
        return createdAdapter;
    }


    // -- OBJECT ADAPTER PROVIDER SUPPORT

    public ObjectAdapterProvider getObjectAdapterProvider() {
        return objectAdapterProviderMixin;
    }

    // -- LIFECYCLE EVENT SUPPORT

    public void postLifecycleEventIfRequired(
            final ManagedObject adapter,
            final Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {
        lifecycleEventMixin.postLifecycleEventIfRequired(adapter, lifecycleEventFacetClass);
    }

    // ------------------------------------------------------------------------------------------------

    /**
     * @param newRootOid - allow a different persistent root oid to be provided.
     * @param session 
     */
    public void asPersistent(final ObjectAdapter rootAdapter, PersistenceSession session) {

        final RootOid persistentOid = createPersistentOrViewModelOid(rootAdapter.getPojo());

        Objects.requireNonNull(persistentOid);
        _Assert.assertFalse("expected to not be a parented collection", rootAdapter.isParentedCollection());
        if(persistentOid.isTransient()) {
            throw _Exceptions.unrecoverable("hintRootOid must be persistent");
        }
        final ObjectSpecId hintRootOidObjectSpecId = persistentOid.getObjectSpecId();
        final ObjectSpecId adapterObjectSpecId = rootAdapter.getSpecification().getSpecId();
        if(!hintRootOidObjectSpecId.equals(adapterObjectSpecId)) {
            throw _Exceptions.unrecoverable("hintRootOid's objectType must be same as that of adapter " +
                    "(was: '" + hintRootOidObjectSpecId + "'; adapter's is " + adapterObjectSpecId + "'");
        }
    }





}