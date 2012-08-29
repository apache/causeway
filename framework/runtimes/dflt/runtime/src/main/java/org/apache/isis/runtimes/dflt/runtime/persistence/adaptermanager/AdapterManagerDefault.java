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

package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.Iterator;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.CollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.object.aggregated.ParentedFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.typeof.ElementSpecificationProviderFromTypeOfFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.log4j.Logger;

public class AdapterManagerDefault implements AdapterManagerSpi {

    private static final Logger LOG = Logger.getLogger(AdapterManagerDefault.class);

    protected final PojoAdapterHashMap pojoAdapterMap = new PojoAdapterHashMap();
    protected final OidAdapterHashMap oidAdapterMap = new OidAdapterHashMap();

    private final PojoRecreator pojoRecreator;


    // //////////////////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////////////////

    /**
     * For object store implementations (eg JDO) that do not provide any mechanism
     * to allow transient objects to be reattached; can instead provide a
     * {@link PojoRecreator} implementation that is injected into the Adapter Manager.
     * 
     * @see http://www.datanucleus.org/servlet/forum/viewthread_thread,7238_lastpage,yes#35976
     */
    public AdapterManagerDefault(PojoRecreator pojoRecreator) {
        this.pojoRecreator = pojoRecreator;
    }

    // //////////////////////////////////////////////////////////////////
    // open, close
    // //////////////////////////////////////////////////////////////////

    @Override
    public void open() {
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
        return pojoAdapterMap.iterator();
    }


    

    // //////////////////////////////////////////////////////////////////
    // Adapter lookup
    // //////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter getAdapterFor(final Object pojo) {
        ensureThatArg(pojo, is(notNullValue()));

        return pojoAdapterMap.getAdapter(pojo);
    }

    @Override
    public ObjectAdapter getAdapterFor(final Oid oid) {
        ensureThatArg(oid, is(notNullValue()));
        ensureMapsConsistent(oid);

        return oidAdapterMap.getAdapter(oid);
    }

    
    // //////////////////////////////////////////////////////////////////
    // Adapter lookup/creation
    // //////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectAdapter adapterFor(final Object pojo) {

        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }
        
        final ObjectAdapter newAdapter = createTransientRootAdapter(pojo);
        
        return mapAndInjectServices(newAdapter);
    }

    private ObjectAdapter existingOrValueAdapter(Object pojo) {

        // attempt to locate adapter for the pojo
        final ObjectAdapter adapter = getAdapterFor(pojo);
        if (adapter != null) {
            return adapter;
        }
        
        // pojo may have been lazily loaded by object store, but we haven't yet seen it
        final ObjectAdapter lazilyLoadedAdapter = pojoRecreator.lazilyLoaded(pojo);
        if(lazilyLoadedAdapter != null) {
            return lazilyLoadedAdapter;
        }
        
        
        // need to create (and possibly map) the adapter.
        final ObjectSpecification objSpec = getSpecificationLoader().loadSpecification(pojo.getClass());
        
        // we create value facets as standalone (so not added to maps)
        if (objSpec.containsFacet(ValueFacet.class)) {
            ObjectAdapter valueAdapter = createStandaloneAdapterAndSetResolveState(pojo);
            return valueAdapter;
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter parentAdapter) {
        
        Ensure.ensureThatArg(parentAdapter, is(not(nullValue())));
        
        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }
        
        final ObjectSpecification objSpec = getSpecificationLoader().loadSpecification(pojo.getClass());
        
        final ObjectAdapter newAdapter;
        if(isAggregated(objSpec)) {
            final AggregatedOid aggregatedOid = getOidGenerator().createAggregateOid(pojo, parentAdapter);
            newAdapter = createAggregatedAdapter(pojo, aggregatedOid);
        } else {
            newAdapter = createTransientRootAdapter(pojo);
        }
        
        return mapAndInjectServices(newAdapter);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter parentAdapter, final OneToManyAssociation collection) {
        
        Ensure.ensureThatArg(parentAdapter, is(not(nullValue())));
        Ensure.ensureThatArg(collection, is(not(nullValue())));
        
        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }
        
        // the List, Set etc. instance gets wrapped in its own adapter
        final ObjectAdapter newAdapter = createCollectionAdapter(pojo, parentAdapter, collection);
        
        return mapAndInjectServices(newAdapter);
    }

    
    /**
     * Creates an {@link ObjectAdapter adapter} to represent a collection
     * of the parent.
     * 
     * <p>
     * The returned adapter will have a {@link CollectionOid}; its version 
     * and its persistence are the same as its owning parent.
     * 
     * <p>
     * Should only be called if the pojo is known not to be
     * {@link #getAdapterFor(Object) mapped}.
     */
    private ObjectAdapter createCollectionAdapter(final Object pojo, final ObjectAdapter parentAdapter, final OneToManyAssociation otma) {

        ensureMapsConsistent(parentAdapter);
        Assert.assertNotNull(pojo);

        final Oid parentOid = parentAdapter.getOid();

        // persistence of collection follows the parent
        final CollectionOid collectionOid = new CollectionOid((TypedOid) parentOid, otma);
        final ObjectAdapter collectionAdapter = createCollectionAdapterAndInferResolveState(pojo, collectionOid);

        // we copy over the type onto the adapter itself
        // [not sure why this is really needed, surely we have enough info in
        // the adapter
        // to look this up on the fly?]
        final TypeOfFacet facet = otma.getFacet(TypeOfFacet.class);
        collectionAdapter.setElementSpecificationProvider(ElementSpecificationProviderFromTypeOfFacet.createFrom(facet));

        return collectionAdapter;
    }

    private static boolean isAggregated(final ObjectSpecification objSpec) {
        return objSpec.containsFacet(ParentedFacet.class);
    }

    
    // //////////////////////////////////////////////////////////////////
    // Recreate adapter
    // //////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter adapterFor(final TypedOid typedOid) {
        return adapterFor(typedOid, ConcurrencyChecking.NO_CHECK);
    }


    @Override
    public ObjectAdapter adapterFor(final TypedOid typedOid, final ConcurrencyChecking concurrencyChecking) {

        // attempt to locate adapter for the Oid
        ObjectAdapter adapter = getAdapterFor(typedOid);
        if (adapter != null) {
            return adapter;
        } 
        
        final Object pojo = pojoRecreator.recreatePojo(typedOid);
        adapter = mapRecreatedPojo(typedOid, pojo);
        
        final Oid adapterOid = adapter.getOid();
        if(adapterOid instanceof RootOid) {
            final RootOid recreatedOid = (RootOid) adapterOid;
            final RootOid originalOid = (RootOid) typedOid;
            try {
                if(concurrencyChecking == ConcurrencyChecking.CHECK) {
                    recreatedOid.checkLock(getAuthenticationSession().getUserName(), originalOid);
                }
            } finally {
                originalOid.setVersion(recreatedOid.getVersion());
            }
        }
        return adapter;
    }

    

    @Override
    public void remapRecreatedPojo(ObjectAdapter adapter, final Object pojo) {
        removeAdapter(adapter);
        adapter.replacePojo(pojo);
        mapAndInjectServices(adapter);
    }


    @Override
    public ObjectAdapter mapRecreatedPojo(final Oid oid, final Object recreatedPojo) {

        // attempt to locate adapter for the pojo
        // REVIEW: this check is possibly redundant because the pojo will most likely 
        // have just been instantiated, so won't yet be in any maps
        final ObjectAdapter adapterLookedUpByPojo = getAdapterFor(recreatedPojo);
        if (adapterLookedUpByPojo != null) {
            return adapterLookedUpByPojo;
        }

        // attempt to locate adapter for the Oid
        final ObjectAdapter adapterLookedUpByOid = getAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }

        final ObjectAdapter createdAdapter = createRootOrAggregatedAdapter(oid, recreatedPojo);
        return mapAndInjectServices(createdAdapter);
    }

    private ObjectAdapter createRootOrAggregatedAdapter(final Oid oid, final Object pojo) {
        final ObjectAdapter createdAdapter;
        if(oid instanceof RootOid) {
            final RootOid rootOid = (RootOid) oid;
            createdAdapter = createRootAdapterAndInferResolveState(pojo, rootOid);
        } else if (oid instanceof CollectionOid){
            final CollectionOid collectionOid = (CollectionOid) oid;
            createdAdapter = createCollectionAdapterAndInferResolveState(pojo, collectionOid);
        } else {
            final AggregatedOid aggregatedOid = (AggregatedOid) oid;
            createdAdapter = createAggregatedAdapter(pojo, aggregatedOid);
        }
        return createdAdapter;
    }


    // //////////////////////////////////////////////////////////////////
    // adapter deletion
    // //////////////////////////////////////////////////////////////////


    /**
     * Removes the specified object from both the identity-adapter map, and the
     * pojo-adapter map.
     * 
     * <p>
     * This indicates that the object is no longer in use, and therefore that no
     * objects exists within the system.
     * 
     * <p>
     * If an {@link ObjectAdapter adapter} is removed while its pojo still is
     * referenced then a subsequent interaction of that pojo will create a
     * different {@link ObjectAdapter adapter}, in a
     * {@link ResolveState#TRANSIENT transient} state.
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
     * Note that there is no management of {@link Version}s here. That is
     * because the {@link PersistenceSession} is expected to manage this.
     * 
     * @param hintRootOid - allow a different persistent root oid to be provided.
     */
    @Override
    public void remapAsPersistent(final ObjectAdapter adapter, RootOid hintRootOid) {
        
        final ObjectAdapter rootAdapter = adapter.getAggregateRoot();  // REVIEW: think this is redundant; would seem this method is only ever called for roots anyway.
        final RootOid transientRootOid = (RootOid) rootAdapter.getOid();

        Ensure.ensureThatArg(rootAdapter.isTransient(), is(true), "root adapter should be transient; oid:" + transientRootOid);
        Ensure.ensureThatArg(transientRootOid.isTransient(), is(true), "root adapter's OID should be transient; oid:" + transientRootOid);
        
        final RootAndCollectionAdapters rootAndCollectionAdapters = new RootAndCollectionAdapters(adapter, this);
        
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("remapAsPersistent: " + transientRootOid);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing root adapter from oid map");
        }
        
        boolean removed = oidAdapterMap.remove(transientRootOid);
        if (!removed) {
            LOG.warn("could not remove oid: " + transientRootOid);
            // should we fail here with a more serious error?
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing collection adapter(s) from oid map");
        }
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final Oid collectionOid = collectionAdapter.getOid();
            removed = oidAdapterMap.remove(collectionOid);
            if (!removed) {
                LOG.warn("could not remove collectionOid: " + collectionOid);
                // should we fail here with a more serious error?
            }
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating the Oid");
        }
        
        // intended for testing (bit nasty)
        final RootOid persistedRootOid;
        if(hintRootOid != null) {
            if(hintRootOid.isTransient()) {
                throw new IsisAssertException("hintRootOid must be persistent");
            }
            final ObjectSpecId hintRootOidObjectSpecId = hintRootOid.getObjectSpecId();
            final ObjectSpecId adapterObjectSpecId = adapter.getSpecification().getSpecId();
            if(!hintRootOidObjectSpecId.equals(adapterObjectSpecId)) {
                throw new IsisAssertException("hintRootOid's objectType must be same as that of adapter " +
                		"(was: '" + hintRootOidObjectSpecId + "'; adapter's is " + adapterObjectSpecId + "'");
            }
            // ok
            persistedRootOid = hintRootOid;
        } else {
            // normal flow - delegate to OidGenerator to obtain a persistent root oid
            persistedRootOid = getOidGenerator().createPersistent(adapter.getObject(), transientRootOid);
        }
        
        // associate root adapter with the new Oid, and remap
        if (LOG.isDebugEnabled()) {
            LOG.debug("replacing Oid for root adapter and re-adding into maps; oid is now: " + persistedRootOid.enString(getOidMarshaller()) + " (was: " + transientRootOid.enString(getOidMarshaller()) + ")");
        }
        adapter.replaceOid(persistedRootOid);
        oidAdapterMap.add(persistedRootOid, adapter);
        
        // associate the collection adapters with new Oids, and re-map
        if (LOG.isDebugEnabled()) {
            LOG.debug("replacing Oids for collection adapter(s) and re-adding into maps");
        }
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final CollectionOid previousCollectionOid = (CollectionOid) collectionAdapter.getOid();
            final CollectionOid persistedCollectionOid = previousCollectionOid.asPersistent(persistedRootOid);
            oidAdapterMap.add(persistedCollectionOid, collectionAdapter);
        }

        
        // some object store implementations may replace collection instances (eg ORM may replace with a cglib-enhanced
        // proxy equivalent.  So, ensure that the collection adapters still wrap the correct pojos.
        if (LOG.isDebugEnabled()) {
            LOG.debug("synchronizing collection pojos, remapping in pojo map if required");
        }
        for (final OneToManyAssociation otma : rootAndCollectionAdapters.getCollections()) {
            final ObjectAdapter collectionAdapter = rootAndCollectionAdapters.getCollectionAdapter(otma);
        
            final Object collectionPojoWrappedByAdapter = collectionAdapter.getObject();
            final Object collectionPojoActuallyOnPojo = getCollectionPojo(otma, adapter);
        
            if (collectionPojoActuallyOnPojo != collectionPojoWrappedByAdapter) {
                pojoAdapterMap.remove(collectionAdapter);
                collectionAdapter.replacePojo(collectionPojoActuallyOnPojo);
                pojoAdapterMap.add(collectionPojoActuallyOnPojo, collectionAdapter);
            }
        }

        for (final ObjectAssociation association: adapter.getSpecification().getAssociations()) {
            if (association.getSpecification().isParented()) {
                final ObjectAdapter referencedAdapter = association.get(adapter);
    
                if(referencedAdapter == null) {
                    continue;
                }
                final Oid oid = referencedAdapter.getOid();
                if (oid instanceof AggregatedOid) {
                    AggregatedOid aoid = (AggregatedOid) oid;
                    AggregatedOid childOid = new AggregatedOid(aoid.getObjectSpecId(), persistedRootOid, aoid.getLocalId());
                    referencedAdapter.replaceOid(childOid);
                }
            }
        }
        
        // update the adapter's state
        adapter.changeState(ResolveState.RESOLVED);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("made persistent " + adapter + "; was " + transientRootOid);
        }
    }

	private static Object getCollectionPojo(final OneToManyAssociation association, final ObjectAdapter ownerAdapter) {
        final PropertyOrCollectionAccessorFacet accessor = association.getFacet(PropertyOrCollectionAccessorFacet.class);
        return accessor.getProperty(ownerAdapter);
    }


    // ///////////////////////////////////////////////////////////////////////////
    // Helpers
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new transient root {@link ObjectAdapter adapter} for the supplied domain
     * object.
     * 
     * <p>
     * Has <tt>protected</tt> visibility just so can be used by subclasses if required.
     */
    protected final ObjectAdapter createTransientRootAdapter(final Object pojo) {
        final RootOid transientRootOid = getOidGenerator().createTransientOid(pojo);
        return createRootAdapterAndInferResolveState(pojo, transientRootOid);
    }

    /**
     * Creates a {@link ObjectAdapter adapter} with no {@link Oid}.
     *
     * <p>
     * The {@link ResolveState} state will be {@link ResolveState#VALUE}.
     * Standalone adapters are never {@link #mapAndInjectServices(ObjectAdapter) mapped}
     * (they have no {@link Oid}, after all).
     * 
     * <p>
     * Should only be called if the pojo is known not to be
     * {@link #getAdapterFor(Object) mapped}, and for immutable value types
     * referenced.
     */
    private ObjectAdapter createStandaloneAdapterAndSetResolveState(final Object pojo) {
        final ObjectAdapter adapter = getObjectAdapterFactory().createAdapter(pojo, null, this);
        adapter.changeState(ResolveState.VALUE);
        return adapter;
    }

    /**
     * Creates (but does not {@link #mapAndInjectServices(ObjectAdapter) map}) a new 
     * root {@link ObjectAdapter adapter} for the supplied domain object, and 
     * sets its {@link ResolveState} based on the {@link Oid}.
     * 
     * <p>
     * The {@link ResolveState} state will be:
     * <ul>
     * <li> {@link ResolveState#TRANSIENT} if the {@link Oid} is
     * {@link Oid#isTransient() transient}.
     * <li> {@link ResolveState#GHOST} if the {@link Oid} is persistent (not
     * {@link Oid#isTransient() transient}).
     * </ul>
     * 
     * @see #createStandaloneAdapterAndSetResolveState(Object)
     * @see #createCollectionAdapterAndInferResolveState(Object, CollectionOid)
     */
    private ObjectAdapter createRootAdapterAndInferResolveState(final Object pojo, RootOid rootOid) {
        Ensure.ensureThatArg(rootOid, is(not(nullValue())));
        final ObjectAdapter rootAdapter = getObjectAdapterFactory().createAdapter(pojo, rootOid, this);
        rootAdapter.changeState(rootOid.isTransient() ? ResolveState.TRANSIENT : ResolveState.GHOST);
        doPostCreateRootAdapter(rootAdapter);
        return rootAdapter;
    }

    /**
     * Hook method for objectstores to register the newly created root-adapter.
     * 
     * <p>
     * For example, the JDO DataNucleus object store uses this to attach the pojo
     * into its persistence context.  This enables dirty tracking and lazy loading of the
     * pojo.
     */
    protected void doPostCreateRootAdapter(ObjectAdapter rootAdapter) {
        
    }

    private ObjectAdapter createCollectionAdapterAndInferResolveState(final Object pojo, CollectionOid collectionOid) {
        Ensure.ensureThatArg(collectionOid, is(not(nullValue())));
        final ObjectAdapter collectionAdapter = getObjectAdapterFactory().createAdapter(pojo, collectionOid, this);
        collectionAdapter.changeState(collectionOid.isTransient() ? ResolveState.TRANSIENT : ResolveState.GHOST);
        return collectionAdapter;
    }

    private ObjectAdapter createAggregatedAdapter(final Object pojo, AggregatedOid aggregatedOid) {
        Ensure.ensureThatArg(aggregatedOid, is(not(nullValue())));
        final ObjectAdapter aggregatedAdapter = getObjectAdapterFactory().createAdapter(pojo, aggregatedOid, this);
        // aggregated; nothing to do, since transient state determined by its parent.
        return aggregatedAdapter;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Helpers: map & unmap
    // //////////////////////////////////////////////////////////////////////////

    private ObjectAdapter mapAndInjectServices(final ObjectAdapter adapter) {
        // since the whole point of this method is to map an adapter that's just been created.
        // so we *don't* call ensureMapsConsistent(adapter); 

        Assert.assertNotNull(adapter);
        final Object pojo = adapter.getObject();
        Assert.assertFalse("POJO Map already contains object", pojo, pojoAdapterMap.containsPojo(pojo));

        if (LOG.isDebugEnabled()) {
            // don't interact with the underlying object because may be a ghost
            // and would trigger a resolve
            // don't call toString() on adapter because calls hashCode on
            // underlying object, may also trigger a resolve.
            LOG.debug("adding identity for adapter with oid=" + adapter.getOid());
        }

        // value adapters are not mapped (but all others - root and aggregated adapters - are)
        if (adapter.isValue()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("not mapping value adapter");
            }
            getServicesInjector().injectServicesInto(pojo);
            return adapter;
        }

        // add all aggregated collections
        final ObjectSpecification objSpec = adapter.getSpecification();
        if (!adapter.isParented() || adapter.isParented() && !objSpec.isImmutable()) {
            pojoAdapterMap.add(pojo, adapter);
        }

        // order is important - add to pojo map first, then identity map
        oidAdapterMap.add(adapter.getOid(), adapter);

        // must inject after mapping, otherwise infinite loop
        getServicesInjector().injectServicesInto(pojo);

        return adapter;
    }

    private void unmap(final ObjectAdapter adapter) {
        ensureMapsConsistent(adapter);

        final Oid oid = adapter.getOid();
        if (oid != null) {
            oidAdapterMap.remove(oid);
        }
        pojoAdapterMap.remove(adapter);
    }

    // //////////////////////////////////////////////////////////////////////////
    // Helpers: ensure invariants
    // //////////////////////////////////////////////////////////////////////////

    /**
     * Fail early if any problems.
     */
    private void ensureMapsConsistent(final ObjectAdapter adapter) {
        if (adapter.isValue()) {
            return;
        }
        if (adapter.isParented()) {
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

        final ObjectAdapter adapter = oidAdapterMap.getAdapter(oid);
        if (adapter == null) {
            return;
        }
        ensureOidAdapterMapConsistent(adapter);
        ensurePojoAdapterMapConsistent(adapter);
    }

    private void ensurePojoAdapterMapConsistent(final ObjectAdapter adapter) {
        final Object adapterPojo = adapter.getObject();
        final ObjectAdapter adapterAccordingToPojoAdapterMap = pojoAdapterMap.getAdapter(adapterPojo);
        ensureThatArg(adapter, is(adapterAccordingToPojoAdapterMap), "mismatch in PojoAdapterMap: adapter's Pojo: " + adapterPojo + ", \n" + "provided adapter: " + adapter + "; \n" + " but map's adapter was : " + adapterAccordingToPojoAdapterMap);
    }

    private void ensureOidAdapterMapConsistent(final ObjectAdapter adapter) {
        final Oid adapterOid = adapter.getOid();
        final ObjectAdapter adapterAccordingToOidAdapterMap = oidAdapterMap.getAdapter(adapterOid);
        ensureThatArg(adapter, is(adapterAccordingToOidAdapterMap), "mismatch in OidAdapter map: " + "adapter's Oid: " + adapterOid + ", " + "provided adapter: " + adapter + "; " + "map's adapter: " + adapterAccordingToOidAdapterMap);
    }

    // //////////////////////////////////////////////////////////////////
    // debug
    // //////////////////////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "Identity map (adapter manager)";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendTitle(pojoAdapterMap.debugTitle());
        pojoAdapterMap.debugData(debug);
        debug.appendln();

        debug.appendTitle(oidAdapterMap.debugTitle());
        oidAdapterMap.debugData(debug);

    }

    
    // //////////////////////////////////////////////////////////////////////////
    // Injectable
    // //////////////////////////////////////////////////////////////////////////

    @Override
    public void injectInto(final Object candidate) {
        if (AdapterManagerAware.class.isAssignableFrom(candidate.getClass())) {
            final AdapterManagerAware cast = AdapterManagerAware.class.cast(candidate);
            cast.setAdapterManager(this);
        }
    }

    
    // /////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////////////

    protected OidMarshaller getOidMarshaller() {
		return IsisContext.getOidMarshaller();
	}

    public OidGenerator getOidGenerator() {
        return IsisContext.getPersistenceSession().getOidGenerator();
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected ObjectAdapterFactory getObjectAdapterFactory() {
        return getPersistenceSession().getObjectAdapterFactory();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected ServicesInjector getServicesInjector() {
        return IsisContext.getPersistenceSession().getServicesInjector();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }
    


}
