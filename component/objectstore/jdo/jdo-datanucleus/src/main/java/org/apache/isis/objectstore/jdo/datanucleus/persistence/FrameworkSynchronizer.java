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
package org.apache.isis.objectstore.jdo.datanucleus.persistence;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.datanucleus.api.jdo.NucleusJDOHelper;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.OidGenerator;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.FrameworkSynchronizer.CalledFrom;

public class FrameworkSynchronizer {

    private static final Logger LOG = Logger.getLogger(FrameworkSynchronizer.class);

    /**
     * Categorises where called from.
     * 
     * <p>
     * Just used for logging.
     */
    public enum CalledFrom {
        EVENT_LOAD, EVENT_STORE, EVENT_PREDIRTY, OS_QUERY, OS_RESOLVE, OS_LAZILYLOADED, EVENT_PREDELETE
    }


    public void postLoadProcessingFor(final PersistenceCapable pojo, CalledFrom calledFrom) {

        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                final Version datastoreVersion = getVersionIfAny(pojo);
                
                final RootOid oid ;
                ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
                if(adapter != null) {
                    ensureRootObject(pojo);
                    oid = (RootOid) adapter.getOid();

                    final Version previousVersion = adapter.getVersion();

                    // sync the pojo held by the adapter with that just loaded
                    getPersistenceSession().remapRecreatedPojo(adapter, pojo);

                    // since there was already an adapter, do concurrency check
                    if(previousVersion != null && datastoreVersion != null) {
                        if(previousVersion.different(datastoreVersion)) {
                            getCurrentTransaction().setAbortCause(new ConcurrencyException(getAuthenticationSession().getUserName(), oid, previousVersion, datastoreVersion));
                        }
                    }
                } else {
                    final OidGenerator oidGenerator = getOidGenerator();
                    oid = oidGenerator.createPersistent(pojo, null);
                    
                    // it appears to be possible that there is already an adapter for this Oid, 
                    // ie from ObjectStore#resolveImmediately()
                    adapter = getAdapterManager().getAdapterFor(oid);
                    if(adapter != null) {
                        getPersistenceSession().remapRecreatedPojo(adapter, pojo);
                    } else {
                        adapter = getPersistenceSession().mapRecreatedPojo(oid, pojo);
                    }
                }
                if(!adapter.isResolved()) {
                    PersistorUtil.startResolving(adapter);
                    PersistorUtil.toEndState(adapter);
                }
                adapter.setVersion(datastoreVersion);

                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
    }


    public void postStoreProcessingFor(final PersistenceCapable pojo, CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                ensureRootObject(pojo);
                
                // assert is persistent
                if(!pojo.jdoIsPersistent()) {
                    throw new IllegalStateException("Pojo JDO state is not persistent! pojo dnOid: " + JDOHelper.getObjectId(pojo));
                }

                final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
                final RootOid isisOid = (RootOid) adapter.getOid();
                
                Class<? extends CallbackFacet> callbackFacetClass;
                if (isisOid.isTransient()) {
                    // persisting
                    final RootOid persistentOid = getOidGenerator().createPersistent(pojo, isisOid);
                    
                    getPersistenceSession().remapAsPersistent(adapter, persistentOid);

                    callbackFacetClass = PersistedCallbackFacet.class;
                    
                    final IsisTransaction transaction = getCurrentTransaction();
                    transaction.enlistCreated(adapter);
                } else {
                    // updating
                    callbackFacetClass = UpdatedCallbackFacet.class;
                    
                    // no need to call transaction.enlist(..); 
                    // already called in preDirty and the post value is captured lazily
                }
                
                Utils.clearDirtyFor(adapter);
                
                Version versionIfAny = getVersionIfAny(pojo);
                adapter.setVersion(versionIfAny);
                CallbackUtils.callCallback(adapter, callbackFacetClass);

                
                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
    }

    public void preDirtyProcessingFor(final PersistenceCapable pojo, CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
                if (adapter == null) {
                    // seen this happen in the case when a parent entity (LeaseItem) has a collection of children
                    // objects (LeaseTerm) for which we haven't had a loaded callback fired and so are not yet
                    // mapped.
                    
                    // it seems reasonable in this case to simply map into Isis here ("just-in-time"); presumably
                    // DN would not be calling this callback if the pojo was not persistent.
                    
                    adapter = lazilyLoaded(pojo, CalledFrom.EVENT_PREDIRTY);
                    if(adapter == null) {
                        throw new RuntimeException("DN could not find objectId for pojo (unexpected) and so could not map into Isis; pojo=[" +  pojo + "]");
                    }
                }
                if(adapter.isTransient()) {
                    // seen this happen in the case when there's a 1<->m bidirectional collection, and we're
                    // attaching the child object, which is being persisted by DN as a result of persistence-by-reachability,
                    // and it "helpfully" sets up the parent attribute on the child, causing this callback to fire.
                    // 
                    // however, at the same time, Isis has only queued up a CreateObjectCommand for the transient object, but it
                    // hasn't yet executed, so thinks that the adapter is still transient. 
                    return;
                }
                final IsisTransaction transaction = getCurrentTransaction();
                transaction.enlistUpdating(adapter);

                ensureRootObject(pojo);
                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
    }


    
    public ObjectAdapter lazilyLoaded(final PersistenceCapable pojo, CalledFrom calledFrom) {
        return withLogging(pojo, new Callable<ObjectAdapter>() {
            @Override
            public ObjectAdapter call() {
                if(getJdoPersistenceManager().getObjectId(pojo) == null) {
                    return null;
                }
                final RootOid oid = getPersistenceSession().getOidGenerator().createPersistent(pojo, null);
                final ObjectAdapter adapter = getPersistenceSession().mapRecreatedPojo(oid, pojo);
                return adapter;
            }
        }, calledFrom);
    }

    
    public void preDeleteProcessingFor(final PersistenceCapable pojo, final CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
                
                final IsisTransaction transaction = getCurrentTransaction();
                transaction.enlistDeleting(adapter);

                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
        
    }

    // /////////////////////////////////////////////////////////
    // Helpers
    // /////////////////////////////////////////////////////////
    
    private <T> T withLogging(PersistenceCapable pojo, Callable<T> runnable, CalledFrom calledFrom) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(calledFrom, LoggingLocation.ENTRY, pojo));
        }
        try {
            return runnable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug(logString(calledFrom, LoggingLocation.EXIT, pojo));
            }
        }
    }
    
    private void withLogging(PersistenceCapable pojo, final Runnable runnable, CalledFrom calledFrom) {
        withLogging(pojo, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
            
        }, calledFrom);
    }
    
    private String logString(CalledFrom calledFrom, LoggingLocation location, PersistenceCapable pojo) {
        final AdapterManager adapterManager = getAdapterManager();
        final ObjectAdapter adapter = adapterManager.getAdapterFor(pojo);
        // initial spaces just to look better in log when wrapped by IsisLifecycleListener...
        return calledFrom.name() + " " + location.prefix + " oid=" + (adapter !=null? adapter.getOid(): "(null)") + " ,pojo " + pojo;
    }


    // /////////////////////////////////////////////////////////
    // More Helpers...
    // /////////////////////////////////////////////////////////

    void ensureFrameworksInAgreement(final PersistenceCapable pojo) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        final Oid oid = adapter.getOid();

        if(!pojo.jdoIsPersistent()) {
            // make sure the adapter is transient
            if (!adapter.getResolveState().isTransient()) {
                throw new IsisException(MessageFormat.format("adapter oid={0} has resolve state in invalid state; should be transient but is {1}; pojo: {2}", oid, adapter.getResolveState(), pojo));
            }

            // make sure the oid is transient
            if (!oid.isTransient()) {
                throw new IsisException(MessageFormat.format("adapter oid={0} has oid in invalid state; should be transient; pojo: {1}", oid, pojo));
            }

        } else {
            // make sure the adapter is persistent
            if (!adapter.getResolveState().representsPersistent()) {
                throw new IsisException(MessageFormat.format("adapter oid={0} has resolve state in invalid state; should be in a persistent but is {1}; pojo: {2}", oid, adapter.getResolveState(), pojo));
            }

            // make sure the oid is persistent
            if (oid.isTransient()) {
                throw new IsisException(MessageFormat.format("adapter oid={0} has oid in invalid state; should be persistent; pojo: {1}", oid, pojo));
            }
        }
    }

    // make sure the entity is known to Isis and is a root
    // TODO: will probably need to handle aggregated entities at some point...
    void ensureRootObject(final PersistenceCapable pojo) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        if(adapter == null) {
            throw new IsisException(MessageFormat.format("Object not yet known to Isis: {0}", pojo));
        }
        final Oid oid = adapter.getOid();
        if (!(oid instanceof RootOid)) {
            throw new IsisException(MessageFormat.format("Not a RootOid: oid={0}, for {1}", oid, pojo));
        }
    }

    private Version getVersionIfAny(final PersistenceCapable pojo) {
        return Utils.getVersionIfAny(pojo, getAuthenticationSession());
    }

    @SuppressWarnings("unused")
    private static Filter<ObjectAssociation> dirtyFieldFilterFor(final PersistenceCapable pojo) {
        String[] dirtyFields = NucleusJDOHelper.getDirtyFields(pojo, JDOHelper.getPersistenceManager(pojo));
        final List<String> dirtyFieldList = Arrays.asList(dirtyFields);
        Filter<ObjectAssociation> dirtyFieldsFilter = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation t) {
                String id = t.getId();
                return dirtyFieldList.contains(id);
            }};
        return dirtyFieldsFilter;
    }

    @SuppressWarnings("unused")
    private void ensureObjectNotLoaded(final PersistenceCapable pojo) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        if(adapter != null) {
            final Oid oid = adapter.getOid();
            throw new IsisException(MessageFormat.format("Object is already mapped in Isis: oid={0}, for {1}", oid, pojo));
        }
    }


    
    // /////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected OidGenerator getOidGenerator() {
        return getPersistenceSession().getOidGenerator();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected IsisTransaction getCurrentTransaction() {
        return IsisContext.getCurrentTransaction();
    }

    protected PersistenceManager getJdoPersistenceManager() {
        final DataNucleusObjectStore objectStore = getObjectStore();
        return objectStore.getPersistenceManager();
    }

    protected DataNucleusObjectStore getObjectStore() {
        return (DataNucleusObjectStore) IsisContext.getPersistenceSession().getObjectStore();
    }




}
