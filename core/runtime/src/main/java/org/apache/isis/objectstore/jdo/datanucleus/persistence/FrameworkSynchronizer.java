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
import java.util.concurrent.Callable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.object.callbacks.*;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.OidGenerator;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;
import org.datanucleus.enhancer.Persistable;

public class FrameworkSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(FrameworkSynchronizer.class);

    /**
     * Categorises where called from.
     * 
     * <p>
     * Just used for logging.
     */
    public enum CalledFrom {
        EVENT_LOAD,
        EVENT_PRESTORE,
        EVENT_POSTSTORE,
        EVENT_PREDIRTY,
        EVENT_POSTDIRTY,
        OS_QUERY,
        OS_RESOLVE,
        OS_LAZILYLOADED,
        EVENT_PREDELETE,
        EVENT_POSTDELETE
    }


    public void postLoadProcessingFor(final Persistable pojo, CalledFrom calledFrom) {

        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                final Persistable pc = pojo;
                
                // need to do eagerly, because (if a viewModel then) a
                // viewModel's #viewModelMemento might need to use services 
                getPersistenceSession().getServicesInjector().injectServicesInto(pojo);
                
                final Version datastoreVersion = getVersionIfAny(pc);
                
                final RootOid originalOid ;
                ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
                if(adapter != null) {
                    ensureRootObject(pojo);
                    originalOid = (RootOid) adapter.getOid();

                    final Version originalVersion = adapter.getVersion();

                    // sync the pojo held by the adapter with that just loaded
                    getPersistenceSession().getAdapterManager() .remapRecreatedPojo(adapter, pojo);
                    
                    // since there was already an adapter, do concurrency check
                    // (but don't set abort cause if checking is suppressed through thread-local)
                    final RootOid thisOid = originalOid;
                    final Version thisVersion = originalVersion;
                    final Version otherVersion = datastoreVersion;
                    
                    if(thisVersion != null && 
                       otherVersion != null && 
                       thisVersion.different(otherVersion)) {

                        if(ConcurrencyChecking.isCurrentlyEnabled()) {
                            LOG.info("concurrency conflict detected on " + thisOid + " (" + otherVersion + ")");
                            final String currentUser = getAuthenticationSession().getUserName();
                            final ConcurrencyException abortCause = new ConcurrencyException(currentUser, thisOid, thisVersion, otherVersion);
                            getCurrentTransaction().setAbortCause(abortCause);

                        } else {
                            LOG.warn("concurrency conflict detected but suppressed, on " + thisOid + " (" + otherVersion + ")");
                        }
                    }
                } else {
                    final OidGenerator oidGenerator = getOidGenerator();
                    originalOid = oidGenerator.createPersistentOrViewModelOid(pojo, null);
                    
                    // it appears to be possible that there is already an adapter for this Oid, 
                    // ie from ObjectStore#resolveImmediately()
                    adapter = getAdapterManager().getAdapterFor(originalOid);
                    if(adapter != null) {
                        getPersistenceSession().getAdapterManager() .remapRecreatedPojo(adapter, pojo);
                    } else {
                        adapter = getPersistenceSession().getAdapterManager().mapRecreatedPojo(originalOid, pojo);
                        CallbackFacet.Util.callCallback(adapter, LoadedCallbackFacet.class);
                    }
                }
                if(!adapter.isResolved()) {
                    PersistorUtil.startResolving(adapter);
                    PersistorUtil.toEndState(adapter);
                }
                adapter.setVersion(datastoreVersion);
                if(pojo.dnIsDeleted()) {
                    adapter.changeState(ResolveState.DESTROYED);
                }

                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
    }


    /**
     * Called either when an entity is initially persisted, or when an entity is updated; fires the appropriate
     * lifecycle callback.
     *
     * <p>
     * The implementation therefore uses Isis' {@link org.apache.isis.core.metamodel.adapter.oid.Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    public void preStoreProcessingFor(final Persistable pojo, final CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
                if(adapter == null) {
                    // not expected.
                    return;
                }

                final RootOid isisOid = (RootOid) adapter.getOid();
                if (isisOid.isTransient()) {
                    // persisting
                    // previously this was performed in the DataNucleusSimplePersistAlgorithm.
                    CallbackFacet.Util.callCallback(adapter, PersistingCallbackFacet.class);
                } else {
                    // updating

                    // don't call here, already called in preDirty.

                    // CallbackFacet.Util.callCallback(adapter, UpdatingCallbackFacet.class);
                }

            }
        }, calledFrom);
    }

    /**
     * Called either when an entity is initially persisted, or when an entity is updated; fires the appropriate lifecycle callback
     *
     * <p>
     * The implementation therefore uses Isis' {@link org.apache.isis.core.metamodel.adapter.oid.Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    public void postStoreProcessingFor(final Persistable pojo, CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                ensureRootObject(pojo);

                // assert is persistent
                if(!pojo.dnIsPersistent()) {
                    throw new IllegalStateException("Pojo JDO state is not persistent! pojo dnOid: " + JDOHelper.getObjectId(pojo));
                }

                final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
                final RootOid isisOid = (RootOid) adapter.getOid();


                if (isisOid.isTransient()) {
                    // persisting
                    final RootOid persistentOid = getOidGenerator().createPersistentOrViewModelOid(pojo, isisOid);

                    getPersistenceSession().getAdapterManager().remapAsPersistent(adapter, persistentOid);

                    CallbackFacet.Util.callCallback(adapter, PersistedCallbackFacet.class);

                    final IsisTransaction transaction = getCurrentTransaction();
                    transaction.enlistCreated(adapter);
                } else {
                    // updating;
                    // the callback and transaction.enlist are done in the preDirty callback
                    // (can't be done here, as the enlist requires to capture the 'before' values)
                    CallbackFacet.Util.callCallback(adapter, UpdatedCallbackFacet.class);
                }

                Utils.clearDirtyFor(adapter);

                Version versionIfAny = getVersionIfAny(pojo);
                adapter.setVersion(versionIfAny);

                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
    }

    public void preDirtyProcessingFor(final Persistable pojo, CalledFrom calledFrom) {
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

                CallbackFacet.Util.callCallback(adapter, UpdatingCallbackFacet.class);

                final IsisTransaction transaction = getCurrentTransaction();
                transaction.enlistUpdating(adapter);

                ensureRootObject(pojo);
                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
    }


    public ObjectAdapter lazilyLoaded(final Persistable pojo, CalledFrom calledFrom) {
        return withLogging(pojo, new Callable<ObjectAdapter>() {
            @Override
            public ObjectAdapter call() {
                if(getJdoPersistenceManager().getObjectId(pojo) == null) {
                    return null;
                }
                final RootOid oid = getPersistenceSession().getOidGenerator().createPersistentOrViewModelOid(pojo, null);
                final ObjectAdapter adapter = getPersistenceSession().getAdapterManager().mapRecreatedPojo(oid, pojo);
                return adapter;
            }
        }, calledFrom);
    }

    
    public void preDeleteProcessingFor(final Persistable pojo, final CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
                
                final IsisTransaction transaction = getCurrentTransaction();
                transaction.enlistDeleting(adapter);

                CallbackFacet.Util.callCallback(adapter, RemovingCallbackFacet.class);
                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
        
    }

    public void postDeleteProcessingFor(final Persistable pojo, final CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
                if(adapter == null) {
                    return;
                }
                if(!adapter.isDestroyed()) {
                    adapter.changeState(ResolveState.DESTROYED);
                }


                // previously we called the removed callback (if any).
                // however, this is almost certainly incorrect, because DN will not allow us
                // to "touch" the pojo once deleted.
                //
                // CallbackFacet.Util.callCallback(adapter, RemovedCallbackFacet.class);


                // this is probably still ok to do, however.
                ensureFrameworksInAgreement(pojo);
            }
        }, calledFrom);
        
    }
    
    // /////////////////////////////////////////////////////////
    // Helpers
    // /////////////////////////////////////////////////////////
    
    private <T> T withLogging(Persistable pojo, Callable<T> runnable, CalledFrom calledFrom) {
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
    
    private void withLogging(Persistable pojo, final Runnable runnable, CalledFrom calledFrom) {
        withLogging(pojo, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
            
        }, calledFrom);
    }
    
    private String logString(CalledFrom calledFrom, LoggingLocation location, Persistable pojo) {
        final AdapterManager adapterManager = getAdapterManager();
        final ObjectAdapter adapter = adapterManager.getAdapterFor(pojo);
        // initial spaces just to look better in log when wrapped by IsisLifecycleListener...
        return calledFrom.name() + " " + location.prefix + " oid=" + (adapter !=null? adapter.getOid(): "(null)") + " ,pojo " + pojo;
    }


    // /////////////////////////////////////////////////////////
    // More Helpers...
    // /////////////////////////////////////////////////////////

    void ensureFrameworksInAgreement(final Persistable pojo) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        final Oid oid = adapter.getOid();

        if(!pojo.dnIsPersistent()) {
            // make sure the adapter is transient
            if (!adapter.getResolveState().isTransient()) {
                throw new IsisException(MessageFormat.format("adapter oid={0} has resolve state in invalid state; should be transient but is {1}; pojo: {2}", oid, adapter.getResolveState(), pojo));
            }

            // make sure the oid is transient
            if (!oid.isTransient()) {
                throw new IsisException(MessageFormat.format("adapter oid={0} has oid in invalid state; should be transient; pojo: {1}", oid, pojo));
            }

        } else if(pojo.dnIsDeleted()) {
            
            // make sure the adapter is destroyed
            if (!adapter.getResolveState().isDestroyed()) {
                throw new IsisException(MessageFormat.format("adapter oid={0} has resolve state in invalid state; should be destroyed but is {1}; pojo: {2}", oid, adapter.getResolveState(), pojo));
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
    void ensureRootObject(final Persistable pojo) {
        final Oid oid = getAdapterManager().adapterFor(pojo).getOid();
        if (!(oid instanceof RootOid)) {
            throw new IsisException(MessageFormat.format("Not a RootOid: oid={0}, for {1}", oid, pojo));
        }
    }

    private Version getVersionIfAny(final Persistable pojo) {
        return Utils.getVersionIfAny(pojo, getAuthenticationSession());
    }

    @SuppressWarnings("unused")
    private void ensureObjectNotLoaded(final Persistable pojo) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        if(adapter != null) {
            final Oid oid = adapter.getOid();
            throw new IsisException(MessageFormat.format("Object is already mapped in Isis: oid={0}, for {1}", oid, pojo));
        }
    }


    
    // /////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////

    protected AdapterManagerDefault getAdapterManager() {
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
