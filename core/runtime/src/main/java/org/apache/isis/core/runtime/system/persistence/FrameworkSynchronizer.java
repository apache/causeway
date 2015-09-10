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
package org.apache.isis.core.runtime.system.persistence;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import javax.jdo.JDOHelper;

import org.datanucleus.enhancement.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;

public class FrameworkSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(FrameworkSynchronizer.class);

    private final PersistenceSession persistenceSession;
    private final AuthenticationSession authenticationSession;

    public FrameworkSynchronizer(
            final PersistenceSession persistenceSession,
            final AuthenticationSession authenticationSession) {
        this.persistenceSession = persistenceSession;
        this.authenticationSession = authenticationSession;

    }

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
                persistenceSession.injectServicesInto(pojo);
                
                final Version datastoreVersion = getVersionIfAny(pc);
                
                final RootOid originalOid ;
                ObjectAdapter adapter = persistenceSession.getAdapterFor(pojo);
                if(adapter != null) {
                    ensureRootObject(pojo);
                    originalOid = (RootOid) adapter.getOid();

                    final Version originalVersion = adapter.getVersion();

                    // sync the pojo held by the adapter with that just loaded
                    persistenceSession.remapRecreatedPojo(adapter, pojo);
                    
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
                            final String currentUser = authenticationSession.getUserName();
                            final ConcurrencyException abortCause = new ConcurrencyException(currentUser, thisOid, thisVersion, otherVersion);
                            persistenceSession.getCurrentTransaction().setAbortCause(abortCause);

                        } else {
                            LOG.warn("concurrency conflict detected but suppressed, on " + thisOid + " (" + otherVersion + ")");
                        }
                    }
                } else {
                    originalOid = persistenceSession.createPersistentOrViewModelOid(pojo);
                    
                    // it appears to be possible that there is already an adapter for this Oid, 
                    // ie from ObjectStore#resolveImmediately()
                    adapter = persistenceSession.getAdapterFor(originalOid);
                    if(adapter != null) {
                        persistenceSession.remapRecreatedPojo(adapter, pojo);
                    } else {
                        adapter = persistenceSession.mapRecreatedPojo(originalOid, pojo);
                        CallbackFacet.Util.callCallback(adapter, LoadedCallbackFacet.class);
                    }
                }

                adapter.setVersion(datastoreVersion);
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
                final ObjectAdapter adapter = persistenceSession.getAdapterFor(pojo);
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

                final ObjectAdapter adapter = persistenceSession.getAdapterFor(pojo);
                final RootOid isisOid = (RootOid) adapter.getOid();


                if (isisOid.isTransient()) {
                    // persisting
                    final RootOid persistentOid = persistenceSession.createPersistentOrViewModelOid(pojo);

                    persistenceSession.remapAsPersistent(adapter, persistentOid);

                    CallbackFacet.Util.callCallback(adapter, PersistedCallbackFacet.class);

                    final IsisTransaction transaction = persistenceSession.getCurrentTransaction();
                    transaction.enlistCreated(adapter);
                } else {
                    // updating;
                    // the callback and transaction.enlist are done in the preDirty callback
                    // (can't be done here, as the enlist requires to capture the 'before' values)
                    CallbackFacet.Util.callCallback(adapter, UpdatedCallbackFacet.class);
                }

                Version versionIfAny = getVersionIfAny(pojo);
                adapter.setVersion(versionIfAny);
            }
        }, calledFrom);
    }



    public ObjectAdapter lazilyLoaded(final Persistable pojo, CalledFrom calledFrom) {
        return withLogging(pojo, new Callable<ObjectAdapter>() {
            @Override
            public ObjectAdapter call() {
                if(persistenceSession.getJdoObjectId(pojo) == null) {
                    return null;
                }
                final RootOid oid = persistenceSession.createPersistentOrViewModelOid(pojo);
                final ObjectAdapter adapter = persistenceSession.mapRecreatedPojo(oid, pojo);
                return adapter;
            }
        }, calledFrom);
    }

    
    public void preDeleteProcessingFor(final Persistable pojo, final CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                ObjectAdapter adapter = persistenceSession.adapterFor(pojo);
                
                final IsisTransaction transaction = persistenceSession.getCurrentTransaction();
                transaction.enlistDeleting(adapter);

                CallbackFacet.Util.callCallback(adapter, RemovingCallbackFacet.class);
            }
        }, calledFrom);
        
    }

    public void postDeleteProcessingFor(final Persistable pojo, final CalledFrom calledFrom) {
        withLogging(pojo, new Runnable() {
            @Override
            public void run() {
                ObjectAdapter adapter = persistenceSession.getAdapterFor(pojo);
                if (adapter == null) {
                    return;
                }

                // previously we called the removed callback (if any).
                // however, this is almost certainly incorrect, because DN will not allow us
                // to "touch" the pojo once deleted.
                //
                // CallbackFacet.Util.callCallback(adapter, RemovedCallbackFacet.class);


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
        final ObjectAdapter adapter = persistenceSession.getAdapterFor(pojo);
        // initial spaces just to look better in log when wrapped by IsisLifecycleListener...
        return calledFrom.name() + " " + location.prefix + " oid=" + (adapter !=null? adapter.getOid(): "(null)") + " ,pojo " + pojo;
    }


    // /////////////////////////////////////////////////////////
    // More Helpers...
    // /////////////////////////////////////////////////////////


    // make sure the entity is known to Isis and is a root
    void ensureRootObject(final Persistable pojo) {
        final Oid oid = persistenceSession.adapterFor(pojo).getOid();
        if (!(oid instanceof RootOid)) {
            throw new IsisException(MessageFormat.format("Not a RootOid: oid={0}, for {1}", oid, pojo));
        }
    }

    private Version getVersionIfAny(final Persistable pojo) {
        return Utils.getVersionIfAny(pojo, authenticationSession);
    }

    @SuppressWarnings("unused")
    private void ensureObjectNotLoaded(final Persistable pojo) {
        final ObjectAdapter adapter = persistenceSession.getAdapterFor(pojo);
        if(adapter != null) {
            final Oid oid = adapter.getOid();
            throw new IsisException(MessageFormat.format("Object is already mapped in Isis: oid={0}, for {1}", oid, pojo));
        }
    }


}
