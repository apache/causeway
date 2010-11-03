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

package org.apache.isis.runtime.persistence.adaptermanager;

import static org.apache.isis.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.Iterator;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.debug.DebugInfo;
import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.object.aggregated.AggregatedFacet;
import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.metamodel.facets.propcoll.access.PropertyAccessorFacet;
import org.apache.isis.metamodel.services.ServicesInjector;
import org.apache.isis.metamodel.services.ServicesInjectorAware;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.identifier.Identified;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.SpecificationLoaderAware;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactoryAware;
import org.apache.isis.runtime.persistence.adaptermanager.internal.OidAdapterHashMap;
import org.apache.isis.runtime.persistence.adaptermanager.internal.OidAdapterMap;
import org.apache.isis.runtime.persistence.adaptermanager.internal.PojoAdapterHashMap;
import org.apache.isis.runtime.persistence.adaptermanager.internal.PojoAdapterMap;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.persistence.oidgenerator.OidGeneratorAware;
import org.apache.log4j.Logger;

public class AdapterManagerDefault extends AdapterManagerAbstract implements AdapterFactoryAware,
    SpecificationLoaderAware, OidGeneratorAware, ServicesInjectorAware, DebugInfo {

    private static final Logger LOG = Logger.getLogger(AdapterManagerDefault.class);

    /**
     * Optionally injected, otherwise will default.
     */
    protected PojoAdapterMap pojoAdapterMap;
    /**
     * Optionally injected, otherwise will default.
     */
    protected OidAdapterMap oidAdapterMap;

    /**
     * Injected using dependency injection.
     */
    private AdapterFactory adapterFactory;

    /**
     * Injected using dependency injection.
     */
    private SpecificationLoader specificationLoader;

    /**
     * Injected using dependency injection.
     */
    private OidGenerator oidGenerator;

    /**
     * Injected using dependency injection.
     */
    private ServicesInjector servicesInjector;

    // //////////////////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////////////////

    public AdapterManagerDefault() {
        // does nothing
    }

    // //////////////////////////////////////////////////////////////////
    // open, close
    // //////////////////////////////////////////////////////////////////

    @Override
    public void open() {
        ensureThatState(adapterFactory, is(notNullValue()));
        ensureThatState(specificationLoader, is(notNullValue()));
        ensureThatState(oidGenerator, is(notNullValue()));
        ensureThatState(servicesInjector, is(notNullValue()));

        if (oidAdapterMap == null) {
            oidAdapterMap = new OidAdapterHashMap();
        }
        if (pojoAdapterMap == null) {
            pojoAdapterMap = new PojoAdapterHashMap();
        }

        oidAdapterMap.open();
        pojoAdapterMap.open();
    }

    @Override
    public void close() {
        oidAdapterMap.close();
        pojoAdapterMap.close();
    }

    // //////////////////////////////////////////////////////////////////
    // reset
    // //////////////////////////////////////////////////////////////////

    @Override
    public void reset() {
        oidAdapterMap.reset();
        pojoAdapterMap.reset();
    }

    // //////////////////////////////////////////////////////////////////
    // Iterable
    // //////////////////////////////////////////////////////////////////

    @Override
    public Iterator<ObjectAdapter> iterator() {
        return getPojoAdapterMap().iterator();
    }

    // //////////////////////////////////////////////////////////////////
    // Backdoor
    // //////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter addExistingAdapter(final ObjectAdapter adapter) {
        mapAndInjectServices(adapter);
        return adapter;
    }

    // //////////////////////////////////////////////////////////////////
    // Adapter lookup
    // //////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter getAdapterFor(final Object pojo) {
        ensureThatArg(pojo, is(notNullValue()));

        return getPojoAdapterMap().getAdapter(pojo);
    }

    @Override
    public ObjectAdapter getAdapterFor(final Oid oid) {
        ensureThatArg(oid, is(notNullValue()));
        ensureMapsConsistent(oid);

        return getOidAdapterMap().getAdapter(oid);
    }

    // //////////////////////////////////////////////////////////////////
    // Adapter lookup/creation
    // //////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Looks up {@link #getAdapterFor(Object)} or returns a new transient
     */
    @Override
    public ObjectAdapter adapterFor(final Object pojo) {

        // attempt to locate adapter for the pojo
        final ObjectAdapter adapter = getAdapterFor(pojo);
        if (adapter != null) {
            return adapter;
        }

        // need to create (and possibly map) the adapter.
        final ObjectSpecification noSpec = getSpecificationLoader().loadSpecification(pojo.getClass());

        // we create value facets as standalone (so not added to maps)
        if (noSpec.containsFacet(ValueFacet.class)) {
            return createStandaloneAdapter(pojo);
        }

        // root objects
        return map(createOrRecreateRootAdapter(pojo));
    }

    @Override
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter, Identified identified) {

        // attempt to locate adapter for the pojo
        final ObjectAdapter adapter = getAdapterFor(pojo);
        if (adapter != null) {
            return adapter;
        }

        // need to create (and possibly map) the adapter.
        final ObjectSpecification noSpec = getSpecificationLoader().loadSpecification(pojo.getClass());

        // we create value facets as standalone (so not added to maps)
        if (noSpec.containsFacet(ValueFacet.class)) {
            return createStandaloneAdapter(pojo);
        }

        // aggregated objects are either intrinsically aggregate (on their spec) or
        // the reference to them are aggregated.
        //
        // can only do this if have been given an ownerAdapter & identified arguments to act as context.
        if (ownerAdapter != null && identified != null) {
            if (specIsAggregated(noSpec) || referenceIsAggregated(identified)) {
                ObjectAdapter newAdapter = createAggregatedAdapter(pojo, ownerAdapter, identified);
                return mapAndInjectServices(newAdapter);
            }
        }

        // root objects
        return map(createOrRecreateRootAdapter(pojo));
    }

    private boolean specIsAggregated(final ObjectSpecification noSpec) {
        return noSpec.containsFacet(AggregatedFacet.class);
    }

    private boolean referenceIsAggregated(Identified identified) {
        return identified.containsFacet(AggregatedFacet.class);
    }

    @Override
    public ObjectAdapter recreateRootAdapter(final Oid oid, final Object pojo) {

        // attempt to locate adapter for the pojo
        final ObjectAdapter adapterLookedUpByPojo = getAdapterFor(pojo);
        if (adapterLookedUpByPojo != null) {
            return adapterLookedUpByPojo;
        }

        // attempt to locate adapter for the Oid
        final ObjectAdapter adapterLookedUpByOid = getAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }

        return map(createOrRecreateRootAdapter(pojo, oid));
    }

    // //////////////////////////////////////////////////////////////////
    // adapter maintenance
    // //////////////////////////////////////////////////////////////////

    @Override
    public void remapUpdated(final Oid oid) {
        ensureThatArg(oid.hasPrevious(), is(true));

        final Oid previousOid = oid.getPrevious();
        if (LOG.isDebugEnabled()) {
            LOG.debug("remapping oid: " + oid + " with previous oid of: " + previousOid);
        }

        final ObjectAdapter lookedUpAdapter = oidAdapterMap.getAdapter(previousOid);
        if (lookedUpAdapter == null) {
            LOG.warn("could not locate previousOid: " + previousOid);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("removing previous oid" + previousOid);
        }
        oidAdapterMap.remove(previousOid);

        // we can't replace the Oid on the looked-up adapter, so instead we
        // just make it the same value as the Oid we were originally passed in
        final Oid lookedUpAdapterOid = lookedUpAdapter.getOid();
        lookedUpAdapterOid.copyFrom(oid);

        // finally re-map the adapter
        oidAdapterMap.add(lookedUpAdapterOid, lookedUpAdapter);
    }

    // //////////////////////////////////////////////////////////////////
    // adapter deletion
    // //////////////////////////////////////////////////////////////////

    /**
     * Removes the specified object from both the identity-adapter map, and the pojo-adapter map.
     * 
     * <p>
     * This indicates that the object is no longer in use, and therefore that no objects exists within the system.
     * 
     * <p>
     * If an {@link ObjectAdapter adapter} is removed while its pojo still is referenced then a subsequent interaction
     * of that pojo will create a different {@link ObjectAdapter adapter}, in a {@link ResolveState#TRANSIENT transient}
     * state.
     * 
     * <p>
     * TODO: should do a cascade remove of any aggregated objects.
     */
    @Override
    public void removeAdapter(final ObjectAdapter adapter) {
        ensureMapsConsistent(adapter);

        if (LOG.isDebugEnabled()) {
            LOG.debug("removing adapter: " + adapter);
        }

        unmap(adapter);
    }

    // //////////////////////////////////////////////////////////////////
    // Persist API
    // //////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note that there is no management of {@link Version}s here. That is because the {@link PersistenceSession} is
     * expected to manage this. (In practice this is done by the <tt>ObjectAdapterStore</tt> implementation delegated by
     * the <tt>PersistenceSessionObjectStore</tt>, and propogated back to client-side as required).
     */
    @Override
    public void remapAsPersistent(final ObjectAdapter adapter) {
        // don't do this because the Oid has been updated already
        // ensureMapsConsistent(adapter);

        AggregateAdapters aggregateAdapters = aggregateAdaptersFor(adapter);
        remapAsPersistent(aggregateAdapters);
    }

    private AggregateAdapters aggregateAdaptersFor(final ObjectAdapter rootAdapter) {
        AggregateAdapters aggregateAdapters = new AggregateAdapters(rootAdapter);
        Oid rootOid = rootAdapter.getOid();

        for (OneToManyAssociation otma : rootAdapter.getSpecification().getCollectionList()) {
            AggregatedOid aggregatedOid = new AggregatedOid(rootOid, otma.getIdentifier());
            ObjectAdapter collectionAdapter = getAdapterFor(aggregatedOid);
            if (collectionAdapter != null) {
                // collection adapters are lazily created and so there may not be one.
                aggregateAdapters.addCollectionAdapter(otma, collectionAdapter);
            }
        }
        return aggregateAdapters;
    }

    private void remapAsPersistent(final AggregateAdapters aggregateAdapters) {

        ObjectAdapter rootAdapter = aggregateAdapters.getRootAdapter();
        // although the Oid reference doesn't change, the Oid internal values will change
        final Oid oid = rootAdapter.getOid();
        if (LOG.isDebugEnabled()) {
            LOG.debug("remapAsPersistent: " + oid);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("removing root adapter from oid map");
        }
        boolean removed = getOidAdapterMap().remove(oid);
        if (!removed) {
            LOG.warn("could not remove oid: " + oid);
            // should we fail here with a more serious error?
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("removing collection adapter(s) from oid map");
        }
        for (ObjectAdapter collectionAdapter : aggregateAdapters) {
            Oid collectionOid = collectionAdapter.getOid();
            removed = getOidAdapterMap().remove(collectionOid);
            if (!removed) {
                LOG.warn("could not remove collectionOid: " + collectionOid);
                // should we fail here with a more serious error?
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("updating the Oid");
        }
        getOidGenerator().convertTransientToPersistentOid(oid);

        // re-map the root adapter
        if (LOG.isDebugEnabled()) {
            LOG.debug("re-adding into maps; oid is now: " + oid);
        }
        getOidAdapterMap().add(oid, rootAdapter);

        // re-map the collection adapters
        if (LOG.isDebugEnabled()) {
            LOG.debug("re-adding collection adapter(s) to oid map");
        }
        for (ObjectAdapter collectionAdapter : aggregateAdapters) {
            AggregatedOid previousCollectionOid = (AggregatedOid) collectionAdapter.getOid();
            getOidAdapterMap().add(previousCollectionOid, collectionAdapter);
        }

        // replace any pojos if required, remapping in the pojo map
        if (LOG.isDebugEnabled()) {
            LOG.debug("replacing any collection pojos, remapping in pojo map");
        }
        for (OneToManyAssociation otma : aggregateAdapters.getCollections()) {
            ObjectAdapter collectionAdapter = aggregateAdapters.getCollectionAdapter(otma);

            Object collectionPojoWrappedByAdapter = collectionAdapter.getObject();
            Object collectionPojoOnRootPojo = getCollectionPojo(otma, rootAdapter);

            if (collectionPojoOnRootPojo != collectionPojoWrappedByAdapter) {
                getPojoAdapterMap().remove(collectionAdapter);
                collectionAdapter.replacePojo(collectionPojoOnRootPojo);
                getPojoAdapterMap().add(collectionPojoOnRootPojo, collectionAdapter);
            }
        }

        // update the adapter's state
        rootAdapter.changeState(ResolveState.RESOLVED);

        if (LOG.isDebugEnabled()) {
            LOG.debug("made persistent " + rootAdapter + "; was " + oid.getPrevious());
        }
    }

    public Object getCollectionPojo(final OneToManyAssociation association, final ObjectAdapter ownerAdapter) {
        final PropertyAccessorFacet accessor = association.getFacet(PropertyAccessorFacet.class);
        return accessor.getProperty(ownerAdapter);
    }

    // //////////////////////////////////////////////////////////////////
    // TestSupport
    // //////////////////////////////////////////////////////////////////

    /**
     * For testing purposes only.
     */
    @Override
    public ObjectAdapter testCreateTransient(final Object pojo, final Oid oid) {
        if (!oid.isTransient()) {
            throw new IllegalArgumentException(
                "Oid should be transient; use standard API to recreate adapters for persistent Oids");
        }
        return map(createOrRecreateRootAdapter(pojo, oid));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Helpers
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new root {@link ObjectAdapter adapter} for the supplied domain object.
     * 
     * @see #createOid(Object)
     */
    protected AggregateAdapters createOrRecreateRootAdapter(final Object pojo) {
        Oid transientOid = createOid(pojo);
        return createOrRecreateRootAdapter(pojo, transientOid);
    }

    /**
     * The default implementation will always create a new transient {@link ObjectAdapter adapter}, using the
     * {@link OidGenerator}. However, the method has <tt>protected</tt> visibility so can be overridden if required. For
     * example, some object stores (eg Hibernate) may be able to infer from the pojo itself what the {@link Oid} and
     * persistence state of the object is.
     */
    protected Oid createOid(final Object pojo) {
        return getOidGenerator().createTransientOid(pojo);
    }

    /**
     * Creates an {@link ObjectAdapter adapter} with an {@link AggregatedOid} (so that its version and its persistence
     * are the same as its owning parent).
     * 
     * <p>
     * Should only be called if the pojo is known not to be {@link #getAdapterFor(Object) mapped}.
     * 
     * <p>
     * Helper method, but <tt>protected</tt> so can be overridden if required. For example, some object stores
     * (specifically, the XML object store at time of writing) do not support aggregated Oids for anything other than
     * collections.
     */
    protected ObjectAdapter createAggregatedAdapter(Object pojo, ObjectAdapter ownerAdapter, Identified identified) {

        Identifier identifier = identified.getIdentifier();
        ensureMapsConsistent(ownerAdapter);
        Assert.assertNotNull(pojo);

        // persistence of aggregated follows the parent
        final Oid aggregatedOid = new AggregatedOid(ownerAdapter.getOid(), identifier);
        ObjectAdapter aggregatedAdapter = createOrRecreateAdapter(pojo, aggregatedOid);

        // we copy over the type onto the adapter itself
        // [not sure why this is really needed, surely we have enough info in the adapter
        // to look this up on the fly?]
        aggregatedAdapter.setTypeOfFacet(identified.getFacet(TypeOfFacet.class));

        // same locking as parent
        aggregatedAdapter.setOptimisticLock(ownerAdapter.getVersion());

        return aggregatedAdapter;
    }

    /**
     * Creates a {@link ObjectAdapter adapter} with no {@link Oid}.
     * 
     * <p>
     * Should only be called if the pojo is known not to be {@link #getAdapterFor(Object) mapped}, and for immutable
     * value types referenced.
     */
    private ObjectAdapter createStandaloneAdapter(Object pojo) {
        return createOrRecreateAdapter(pojo, null);
    }

    /**
     * Helper method that creates but does not {@link #mapAndInjectServices(ObjectAdapter) add}) the root
     * {@link ObjectAdapter adapter} along with adapters for all collections, an {@link ObjectAdapter adapter}, and sets
     * its {@link ResolveState} based on the {@link Oid}.
     * 
     * <p>
     * The {@link ResolveState} state will be:
     * <ul>
     * <li> {@link ResolveState#TRANSIENT} if the {@link Oid} is {@link Oid#isTransient() transient}.
     * <li> {@link ResolveState#GHOST} if the {@link Oid} is persistent (not {@link Oid#isTransient() transient}).
     * <li> {@link ResolveState#VALUE} if no {@link Oid} was supplied.
     * </ul>
     */
    private AggregateAdapters createOrRecreateRootAdapter(final Object pojo, final Oid oid) {
        ObjectAdapter rootAdapter = createOrRecreateAdapter(pojo, oid);

        AggregateAdapters aggregateAdapters = new AggregateAdapters(rootAdapter);

        // failed experiment to try to ensure that all adapters are loaded for the root;
        // left in in case we want to re-instate
        // eagerlyCreateCollectionAdapters(rootAdapter, aggregateAdapters);
        return aggregateAdapters;
    }

    @SuppressWarnings("unused")
    private void eagerlyCreateCollectionAdapters(ObjectAdapter rootAdapter, AggregateAdapters aggregateAdapters) {
        for (OneToManyAssociation otma : rootAdapter.getSpecification().getCollectionList()) {
            Object referencedCollection = getCollectionPojo(otma, rootAdapter);
            ObjectAdapter collectionAdapter = createAggregatedAdapter(referencedCollection, rootAdapter, otma);

            aggregateAdapters.addCollectionAdapter(otma, collectionAdapter);
        }
    }

    private ObjectAdapter createOrRecreateAdapter(final Object pojo, final Oid oid) {
        ObjectAdapter adapter = getAdapterFactory().createAdapter(pojo, oid);
        if (oid == null) {
            adapter.changeState(ResolveState.VALUE);
        } else {
            adapter.changeState(oid.isTransient() ? ResolveState.TRANSIENT : ResolveState.GHOST);
        }
        return adapter;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Helpers: map & unmap
    // //////////////////////////////////////////////////////////////////////////

    private ObjectAdapter map(final AggregateAdapters aggregateAdapters) {
        Assert.assertNotNull(aggregateAdapters);
        ObjectAdapter adapter = aggregateAdapters.getRootAdapter();
        mapAndInjectServices(adapter);
        for (ObjectAdapter collectionAdapter : aggregateAdapters) {
            mapAndInjectServices(collectionAdapter);
        }
        return adapter;
    }

    private ObjectAdapter mapAndInjectServices(final ObjectAdapter adapter) {
        Assert.assertNotNull(adapter);
        final Object pojo = adapter.getObject();
        Assert.assertFalse("POJO Map already contains object", pojo, getPojoAdapterMap().containsPojo(pojo));

        if (LOG.isDebugEnabled()) {
            // don't interact with the underlying object because may be a ghost
            // and would trigger a resolve
            // don't call toString() on adapter because calls hashCode on underlying object,
            // may also trigger a resolve.
            LOG.debug("adding identity for adapter with oid=" + adapter.getOid());
        }

        // new design... always map ObjectAdapter provided not standalone.
        if (adapter.getResolveState().isValue()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("not mapping value adapter");
            }
            servicesInjector.injectDependencies(pojo);
            return adapter;
        }

        // add all aggregated collections
        ObjectSpecification noSpec = adapter.getSpecification();
        if (!adapter.isAggregated() || adapter.isAggregated() && !noSpec.isImmutable()) {
            getPojoAdapterMap().add(pojo, adapter);
        }

        // order is important - add to pojo map first, then identity map
        getOidAdapterMap().add(adapter.getOid(), adapter);

        // must inject after mapping, otherwise infinite loop
        servicesInjector.injectDependencies(pojo);

        return adapter;
    }

    private void unmap(final ObjectAdapter adapter) {
        ensureMapsConsistent(adapter);

        final Oid oid = adapter.getOid();
        if (oid != null) {
            getOidAdapterMap().remove(oid);
        }
        getPojoAdapterMap().remove(adapter);
    }

    // //////////////////////////////////////////////////////////////////////////
    // Helpers: ensure invariants
    // //////////////////////////////////////////////////////////////////////////

    /**
     * Fail early if any problems.
     */
    private void ensureMapsConsistent(final ObjectAdapter adapter) {
        // it isn't possible to guaranteee that the Oid will have no previous, because
        // if running standalone or server-side then we don't know that we can clear the previous
        //
        // ensureThat(adapter.getOid().hasPrevious(), is(false), "adapter's Oid has a previous value.");

        if (adapter.getResolveState().isValue()) {
            return;
        }
        ensurePojoAdapterMapConsistent(adapter);
        ensureOidAdapterMapConsistent(adapter);
    }

    /**
     * Fail early if any problems.
     */
    private void ensureMapsConsistent(final Oid oid) {
        ensureThatArg(oid, is(notNullValue()));

        // it isn't possible to guaranteee that the Oid will have no previous, because
        // if running standalone or server-side then we don't know that we can clear the previous
        //
        // ensureThat(oid.hasPrevious(), is(false), "adapter's Oid has a previous value.");

        ObjectAdapter adapter = getOidAdapterMap().getAdapter(oid);
        if (adapter == null) {
            return;
        }
        ensureOidAdapterMapConsistent(adapter);
        ensurePojoAdapterMapConsistent(adapter);
    }

    private void ensurePojoAdapterMapConsistent(final ObjectAdapter adapter) {
        Object adapterPojo = adapter.getObject();
        ObjectAdapter adapterAccordingToPojoAdapterMap = getPojoAdapterMap().getAdapter(adapterPojo);
        ensureThatArg(adapter, is(adapterAccordingToPojoAdapterMap), "mismatch in PojoAdapterMap: adapter's Pojo: "
            + adapterPojo + ", \n" + "provided adapter: " + adapter + "; \n" + " but map's adapter was : "
            + adapterAccordingToPojoAdapterMap);
    }

    private void ensureOidAdapterMapConsistent(final ObjectAdapter adapter) {
        Oid adapterOid = adapter.getOid();
        ObjectAdapter adapterAccordingToOidAdapterMap = getOidAdapterMap().getAdapter(adapterOid);
        ensureThatArg(adapter, is(adapterAccordingToOidAdapterMap), "mismatch in OidAdapter map: " + "adapter's Oid: "
            + adapterOid + ", " + "provided adapter: " + adapter + "; " + "map's adapter: "
            + adapterAccordingToOidAdapterMap);
    }

    // //////////////////////////////////////////////////////////////////
    // Replaced code
    // (just commented out for now to act as a reference)
    // //////////////////////////////////////////////////////////////////

    // /**
    // * TODO: to go through
    // */
    // public ObjectAdapter adapterFor(Object pojo) {
    // return adapterFor(pojo, (Oid)null, (Version)null);
    // }

    // replacing this code, just commented out for now to act as a reference.

    // public ObjectAdapter adapterFor(final Object domainObject, final Oid oid, final Version version) {
    // if (oid != null) {
    // return adapterFor(domainObject, oid);
    // }
    //
    // if (domainObject == null) {
    // return null;
    // }
    //
    // final ObjectAdapter adapter = identityMap.getAdapterFor(domainObject);
    // if (adapter != null) {
    // return adapter;
    // }
    //
    // return createAdapterForNewObjectAndMap(domainObject);
    // }

    // replacing this code, just commented out for now to act as a reference.

    // /**
    // * Creates a new adapter with a new transient OID, and then added to the identity map.
    // *
    // * <p>
    // * The {@link ObjectAdapter adapter's} {@link ObjectAdapter#getResolveState() resolve state}
    // * is set to either {@link ResolveState#TRANSIENT} or {@link ResolveState#STANDALONE}
    // * as required.
    // *
    // * <p>
    // * <b><i>REVIEW: think this is wrong; we should set to
    // * {@link ResolveState#STANDALONE} is the {@link ObjectAdapter adapter} has been created
    // * with no identity ({@link ObjectAdapter#getOid() is null}), not based on the {@link
    // ObjectSpecification#isAggregated()}</i></b>
    // */
    // private ObjectAdapter createAdapterForNewObjectAndMap(final Object domainObject) {
    // final Oid transientOid = getOidGenerator().createTransientOid(domainObject);
    // final ObjectAdapter adapter = getAdapterFactory().createAdapter(domainObject, transientOid);
    // if (LOG.isDebugEnabled()) {
    // // don't call toString() because may still be a ghost and any interaction could resolve
    // LOG.debug("creating adapter (transient) for adapter with oid=" + adapter.getOid());
    // }
    // identityMap.addAdapter(adapter);
    //
    // final ObjectSpecification specification = adapter.getSpecification();
    // if (specification.isAggregated()) {
    // adapter.changeState(ResolveState.STANDALONE);
    // } else {
    // adapter.changeState(ResolveState.TRANSIENT);
    // }
    //
    // return adapter;
    // }

    // replacing this code, just commented out for now to act as a reference.

    // private ObjectAdapter adapterFor(final Object domainObject, final Oid oid) {
    // ObjectAdapter adapter = getAdapterFor(oid);
    // if (adapter != null) {
    //
    // // TODO: REVIEW: was tripping the exception setting a Boolean on a @NotPersistable.
    // // is it safe for immutable values to use equals(.) instead?
    //
    // ObjectSpecification noSpec = adapter.getSpecification();
    // ValueFacet valueFacet = noSpec.getFacet(ValueFacet.class);
    // ImmutableFacet immutableFacet = valueFacet != null? valueFacet.getFacet(ImmutableFacet.class): null;
    // if (immutableFacet != null) {
    // if (!adapter.getObject().equals(domainObject)) {
    // throw new
    // AdapterException("Mapped adapter (of immutable value type) is for a domain object with different value: "
    // + domainObject + "; " + adapter);
    // }
    // } else {
    // if (adapter.getObject() != domainObject) {
    // throw new AdapterException("Mapped adapter is for different domain object: " + domainObject + "; " +
    // adapter);
    // }
    // }
    // return adapter;
    // }
    //
    // adapter = getAdapterFor(domainObject);
    // if (adapter != null) {
    // if (!adapter.getOid().equals(oid)) {
    // throw new AdapterException("Mapped adapter has oid: " + oid + "; " + adapter);
    // }
    // return adapter;
    // }
    // return createAdapterAndMap(domainObject, oid);
    // }

    // /**
    // * Should only be called if it is known that the domainObject and Oid do not
    // * correspond to any existing {@link ObjectAdapter}.
    // */
    // private ObjectAdapter createAdapterAndMap(final Object domainObject, final Oid oid) {
    // return map(createAdapter(domainObject, oid));
    // }

    // //////////////////////////////////////////////////////////////////
    // debug
    // //////////////////////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "Identity map (adapter manager)";
    }

    @Override
    public void debugData(final DebugString debug) {
        debug.appendTitle(pojoAdapterMap.debugTitle());
        pojoAdapterMap.debugData(debug);
        debug.appendln();

        debug.appendTitle(oidAdapterMap.debugTitle());
        oidAdapterMap.debugData(debug);

    }

    // //////////////////////////////////////////////////////////////////////////
    // Optionally Injected: OidAdapterMap
    // //////////////////////////////////////////////////////////////////////////

    private OidAdapterMap getOidAdapterMap() {
        return oidAdapterMap;
    }

    /**
     * For dependency injection.
     * 
     * <p>
     * If not injected, will be instantiated within {@link #init()} method.
     */
    public void setOidAdapterMap(final OidAdapterMap identityAdapterMap) {
        this.oidAdapterMap = identityAdapterMap;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Optionally Injected: IdentityAdapterMap
    // //////////////////////////////////////////////////////////////////////////

    private PojoAdapterMap getPojoAdapterMap() {
        return pojoAdapterMap;
    }

    /**
     * For dependency injection.
     * 
     * <p>
     * If not injected, will be instantiated within {@link #init()} method.
     */
    public void setPojoAdapterMap(final PojoAdapterMap pojoAdapterMap) {
        this.pojoAdapterMap = pojoAdapterMap;
    }

    // /////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // /////////////////////////////////////////////////////////////////

    /**
     * @see #setAdapterFactory(AdapterFactory)
     */
    public AdapterFactory getAdapterFactory() {
        return adapterFactory;
    }

    /**
     * Injected.
     */
    @Override
    public void setAdapterFactory(AdapterFactory adapterFactory) {
        this.adapterFactory = adapterFactory;
    }

    /**
     * @see #setSpecificationLoader(SpecificationLoader)
     */
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * Injected.
     */
    @Override
    public void setSpecificationLoader(SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

    /**
     * @see #setOidGenerator(OidGenerator)
     */
    public OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    /**
     * Injected.
     */
    @Override
    public void setOidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    /**
     * Injected.
     */
    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
