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

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterByIdProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * Encapsulate ObjectAdpater life-cycling.  
 *  
 * @since 2.0.0-M2
 */
final public class ObjectAdapterContext {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext.class);

    public static ObjectAdapterContext openContext(
            ServicesInjector servicesInjector, 
            AuthenticationSession authenticationSession, 
            SpecificationLoader specificationLoader, 
            PersistenceSession persistenceSession) {
        final ObjectAdapterContext objectAdapterContext = 
                new ObjectAdapterContext(servicesInjector, authenticationSession, 
                        specificationLoader, persistenceSession);
        objectAdapterContext.open();
        return objectAdapterContext;
    }

    private final PersistenceSession persistenceSession; 
    private final SpecificationLoader specificationLoader;
    private final ObjectAdapterContext_ObjectAdapterProvider objectAdapterProviderMixin;
    private final ObjectAdapterContext_MementoSupport mementoSupportMixin;
    private final ObjectAdapterContext_ServiceLookup serviceLookupMixin;
    private final ObjectAdapterContext_NewIdentifier newIdentifierMixin;
    private final ObjectAdapterContext_ObjectAdapterByIdProvider objectAdapterByIdProviderMixin;
    private final ObjectAdapterContext_DependencyInjection dependencyInjectionMixin;
    final ObjectAdapterContext_ObjectCreation objectCreationMixin;
    private final ObjectAdapterContext_LifecycleEventSupport lifecycleEventMixin;
    private final ServicesInjector servicesInjector;

    private ObjectAdapterContext(
            ServicesInjector servicesInjector, 
            AuthenticationSession authenticationSession, 
            SpecificationLoader specificationLoader, 
            PersistenceSession persistenceSession) {

        this.objectAdapterProviderMixin = new ObjectAdapterContext_ObjectAdapterProvider(this, persistenceSession);
        this.mementoSupportMixin = new ObjectAdapterContext_MementoSupport(this, persistenceSession);
        this.serviceLookupMixin = new ObjectAdapterContext_ServiceLookup(this, servicesInjector);
        this.newIdentifierMixin = new ObjectAdapterContext_NewIdentifier(this, persistenceSession);
        this.objectAdapterByIdProviderMixin = new ObjectAdapterContext_ObjectAdapterByIdProvider(this, persistenceSession, authenticationSession);
        this.dependencyInjectionMixin = new ObjectAdapterContext_DependencyInjection(this, persistenceSession);
        this.objectCreationMixin = new ObjectAdapterContext_ObjectCreation(this, persistenceSession);
        this.lifecycleEventMixin = new ObjectAdapterContext_LifecycleEventSupport(this, persistenceSession);

        this.persistenceSession = persistenceSession;
        this.specificationLoader = specificationLoader;
        this.servicesInjector = servicesInjector;

        this.objectAdapterFactories = new ObjectAdapterContext_Factories(
                authenticationSession, 
                specificationLoader, 
                persistenceSession);
    }

    // -- DEBUG

    void printContextInfo(String msg) {
        if(LOG.isDebugEnabled()) {
            String id = Integer.toHexString(this.hashCode());
            String session = ""+persistenceSession;
            LOG.debug(String.format("%s id=%s session='%s'", msg, id, session));
        }
    }

    // -- LIFE-CYCLING

    public void open() {
        printContextInfo("OPEN_");
    }

    public void close() {
        printContextInfo("CLOSE");
    }

    // -- NEW IDENTIFIER

    public RootOid createPersistentOrViewModelOid(Object pojo) {
        return newIdentifierMixin.createPersistentOid(pojo);
    }

    // -- SERVICE LOOKUP

    // package private
    ObjectAdapter lookupServiceAdapterFor(RootOid rootOid) {
        return serviceLookupMixin.lookupServiceAdapterFor(rootOid);
    }

    // -- BY-ID SUPPORT

    public ObjectAdapterByIdProvider getObjectAdapterByIdProvider() {
        return objectAdapterByIdProviderMixin;
    }

    // -- DEPENDENCY INJECTION

    Object instantiateAndInjectServices(ObjectSpecification objectSpec) {
        return dependencyInjectionMixin.instantiateAndInjectServices(objectSpec);
    }

    // -- FACTORIES

    // package private
    static interface ObjectAdapterFactories {

        /**
         * Creates (but does not {@link #mapAndInjectServices(ObjectAdapter) map}) a new
         * root {@link ObjectAdapter adapter} for the supplied domain object.
         *
         * @see #createStandaloneAdapter(Object)
         * @see #createCollectionAdapter(Object, ParentedCollectionOid)
         */
        ObjectAdapter createRootAdapter(Object pojo, RootOid rootOid);

        ObjectAdapter createCollectionAdapter(Object pojo, ParentedCollectionOid collectionOid);

        /**
         * Creates an {@link ObjectAdapter adapter} to represent a collection
         * of the parent.
         *
         * <p>
         * The returned adapter will have a {@link ParentedCollectionOid}; its version
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

    @Deprecated // don't expose caching
    public ObjectAdapter fetchPersistent(final Object pojo) {
        if (persistenceSession.getPersistenceManager().getObjectId(pojo) == null) {
            return null;
        }
        final RootOid oid = createPersistentOrViewModelOid(pojo);
        final ObjectAdapter adapter = recreatePojo(oid, pojo);
        return adapter;
    }
    
    @Deprecated
    public ObjectAdapter recreatePojo(Oid oid, Object recreatedPojo) {
        final ObjectAdapter createdAdapter = createRootOrAggregatedAdapter(oid, recreatedPojo);
        return injectServices(createdAdapter);
    }

    @Deprecated
    public ObjectAdapter injectServices(final ObjectAdapter adapter) {
        Objects.requireNonNull(adapter);
        if(adapter.isValue()) {
            return adapter; // guard against value objects
        }
        final Object pojo = adapter.getObject();
        servicesInjector.injectServicesInto(pojo);
        return adapter;
    }
    
    @Deprecated
    public ObjectAdapter createRootOrAggregatedAdapter(final Oid oid, final Object pojo) {
        final ObjectAdapter createdAdapter;
        if(oid instanceof RootOid) {
            final RootOid rootOid = (RootOid) oid;
            createdAdapter = getFactories().createRootAdapter(pojo, rootOid);
        } else /*if (oid instanceof CollectionOid)*/ {
            final ParentedCollectionOid collectionOid = (ParentedCollectionOid) oid;
            createdAdapter = getFactories().createCollectionAdapter(pojo, collectionOid);
        }
        return createdAdapter;
    }
    

    // -- OBJECT ADAPTER PROVIDER SUPPORT

    public ObjectAdapterProvider getObjectAdapterProvider() {
        return objectAdapterProviderMixin;
    }

    // -- MEMENTO SUPPORT

    public static interface MementoRecreateObjectSupport {
        ObjectAdapter recreateObject(ObjectSpecification spec, Oid oid, Data data);
    }

    public MementoRecreateObjectSupport mementoSupport() {
        return mementoSupportMixin;
    }

    // -- LIFECYCLE EVENT SUPPORT

    public void postLifecycleEventIfRequired(
            final ObjectAdapter adapter,
            final Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {
        lifecycleEventMixin.postLifecycleEventIfRequired(adapter, lifecycleEventFacetClass);
    }

    // ------------------------------------------------------------------------------------------------

    // package private
    ObjectAdapter adapterForViewModel(Object viewModelPojo, Function<ObjectSpecId, RootOid> rootOidFactory) {
        final ObjectSpecification objectSpecification = 
                specificationLoader.loadSpecification(viewModelPojo.getClass());
        final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
        final RootOid newRootOid = rootOidFactory.apply(objectSpecId);

        final ObjectAdapter viewModelAdapter = recreatePojo(newRootOid, viewModelPojo);
        return viewModelAdapter;
    }

    /**
     * Note that there is no management of {@link Version}s here. That is
     * because the {@link PersistenceSession} is expected to manage this.
     *
     * @param newRootOid - allow a different persistent root oid to be provided.
     * @param session 
     */
    @Deprecated // expected to be moved
    public void remapAsPersistent(final ObjectAdapter rootAdapter, RootOid newRootOid, PersistenceSession session) {
        Objects.requireNonNull(newRootOid);
        Assert.assertFalse("expected to not be a parented collection", rootAdapter.isParentedCollection());
        if(newRootOid.isTransient()) {
            throw new IsisAssertException("hintRootOid must be persistent");
        }
        final ObjectSpecId hintRootOidObjectSpecId = newRootOid.getObjectSpecId();
        final ObjectSpecId adapterObjectSpecId = rootAdapter.getSpecification().getSpecId();
        if(!hintRootOidObjectSpecId.equals(adapterObjectSpecId)) {
            throw new IsisAssertException("hintRootOid's objectType must be same as that of adapter " +
                    "(was: '" + hintRootOidObjectSpecId + "'; adapter's is " + adapterObjectSpecId + "'");
        }
    }

    @Deprecated
    public ObjectAdapter remapRecreatedPojo(ObjectAdapter adapter, final Object pojo) {
        final ObjectAdapter newAdapter = adapter.withPojo(pojo);
        injectServices(newAdapter);
        return newAdapter;
    }



}