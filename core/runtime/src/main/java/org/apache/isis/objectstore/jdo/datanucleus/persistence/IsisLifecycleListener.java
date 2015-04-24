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

import java.util.Map;

import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.ClearLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DetachLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.FrameworkSynchronizer.CalledFrom;
import org.datanucleus.enhancer.Persistable;

public class IsisLifecycleListener implements AttachLifecycleListener, ClearLifecycleListener, CreateLifecycleListener, DeleteLifecycleListener, DetachLifecycleListener, DirtyLifecycleListener, LoadLifecycleListener, StoreLifecycleListener, SuspendableListener {

    private static final Logger LOG = LoggerFactory.getLogger(IsisLifecycleListener.class);
    
    private final FrameworkSynchronizer synchronizer;
    
    public IsisLifecycleListener(FrameworkSynchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }


    /////////////////////////////////////////////////////////////////////////
    // callbacks
    /////////////////////////////////////////////////////////////////////////

    @Override
    public void postCreate(final InstanceLifecycleEvent event) {
        withLogging(Phase.POST, event, new RunnableNoop(event));
    }

    @Override
    public void preAttach(final InstanceLifecycleEvent event) {
        withLogging(Phase.PRE, event, new RunnableEnsureFrameworksInAgreement(event));
    }

    @Override
    public void postAttach(final InstanceLifecycleEvent event) {
        withLogging(Phase.POST, event, new RunnableEnsureFrameworksInAgreement(event));
    }

    @Override
    public void postLoad(final InstanceLifecycleEvent event) {
        withLogging(Phase.POST, event, new RunnableAbstract(event){
            @Override
            protected void doRun() {
                final Persistable pojo = Utils.persistenceCapableFor(event);
                synchronizer.postLoadProcessingFor(pojo, CalledFrom.EVENT_LOAD);
            }});
    }

	@Override
    public void preStore(InstanceLifecycleEvent event) {
        withLogging(Phase.PRE, event, new RunnableAbstract(event){
            @Override
            protected void doRun() {
                final Persistable pojo = Utils.persistenceCapableFor(event);
                synchronizer.preStoreProcessingFor(pojo, CalledFrom.EVENT_PRESTORE);

            }});
    }

    @Override
    public void postStore(InstanceLifecycleEvent event) {
        withLogging(Phase.POST, event, new RunnableAbstract(event){
            @Override
            protected void doRun() {
                final Persistable pojo = Utils.persistenceCapableFor(event);
                synchronizer.postStoreProcessingFor(pojo, CalledFrom.EVENT_POSTSTORE);
            }});
    }

    @Override
    public void preDirty(InstanceLifecycleEvent event) {
        withLogging(Phase.PRE, event, new RunnableAbstract(event){
            @Override
            protected void doRun() {
                final Persistable pojo = Utils.persistenceCapableFor(event);
                synchronizer.preDirtyProcessingFor(pojo, CalledFrom.EVENT_PREDIRTY);
            }});
    }

    @Override
    public void postDirty(InstanceLifecycleEvent event) {

        // cannot assert on the frameworks being in agreement, due to the scenario documented
        // in the FrameworkSynchronizer#preDirtyProcessing(...)
        //
        // 1<->m bidirectional, persistence-by-reachability

        withLogging(Phase.POST, event, new RunnableNoop(event));
    }

    @Override
    public void preDelete(InstanceLifecycleEvent event) {

        withLogging(Phase.PRE, event, new RunnableAbstract(event){
            @Override
            protected void doRun() {
                final Persistable pojo = Utils.persistenceCapableFor(event);
                synchronizer.preDeleteProcessingFor(pojo, CalledFrom.EVENT_PREDELETE);
            }
        });
    }

    @Override
    public void postDelete(InstanceLifecycleEvent event) {
        withLogging(Phase.POST, event, new RunnableAbstract(event){
            @Override
            protected void doRun() {
                final Persistable pojo = Utils.persistenceCapableFor(event);
                synchronizer.postDeleteProcessingFor(pojo, CalledFrom.EVENT_POSTDELETE);
            }
        });
    }

    /**
     * Does nothing, not important event for Isis to track.
     */
    @Override
    public void preClear(InstanceLifecycleEvent event) {
        // ignoring, not important to us
    }

    /**
     * Does nothing, not important event for Isis to track.
     */
    @Override
    public void postClear(InstanceLifecycleEvent event) {
        // ignoring, not important to us
    }

    @Override
    public void preDetach(InstanceLifecycleEvent event) {
        withLogging(Phase.PRE, event, new RunnableEnsureFrameworksInAgreement(event));
    }

    @Override
    public void postDetach(InstanceLifecycleEvent event) {
        withLogging(Phase.POST, event, new RunnableEnsureFrameworksInAgreement(event));
    }

    
    /////////////////////////////////////////////////////////////////////////
    // withLogging
    /////////////////////////////////////////////////////////////////////////

    private void withLogging(Phase phase, InstanceLifecycleEvent event, Runnable runnable) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(phase, LoggingLocation.ENTRY, event));
        }
        try {
            runnable.run();
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug(logString(phase, LoggingLocation.EXIT, event));
            }
        }
    }
    
    private abstract class RunnableAbstract implements Runnable {
        final InstanceLifecycleEvent event;
        public RunnableAbstract(final InstanceLifecycleEvent event) {
            this.event = event;
        }
        @Override
        public void run() {
            if (isSuspended()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(" [currently suspended - ignoring]");
                }
                return;
            }
            doRun();
        }
        
        protected abstract void doRun(); 
    }
    
    private class RunnableNoop extends RunnableAbstract {
        RunnableNoop(InstanceLifecycleEvent event) {
            super(event);
        }
        protected void doRun() {} 
    }
    
    private class RunnableEnsureFrameworksInAgreement extends RunnableAbstract {
        RunnableEnsureFrameworksInAgreement(InstanceLifecycleEvent event) {
            super(event);
        }
        protected void doRun() {
            final Persistable pojo = Utils.persistenceCapableFor(event);
            synchronizer.ensureRootObject(pojo);
            synchronizer.ensureFrameworksInAgreement(pojo);
        } 
    }
    

    // /////////////////////////////////////////////////////////
    // SuspendListener
    // /////////////////////////////////////////////////////////

    private boolean suspended;


    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    // /////////////////////////////////////////////////////////
    // Logging
    // /////////////////////////////////////////////////////////

    private enum Phase {
        PRE, POST
    }

    private static Map<Integer, LifecycleEventType> events = Maps.newHashMap();

    private enum LifecycleEventType {
        CREATE(0), LOAD(1), STORE(2), CLEAR(3), DELETE(4), DIRTY(5), DETACH(6), ATTACH(7);

        private LifecycleEventType(int code) {
            events.put(code, this);
        }

        public static LifecycleEventType lookup(int code) {
            return events.get(code);
        }
    }

    private String logString(Phase phase, LoggingLocation location, InstanceLifecycleEvent event) {
        final Persistable pojo = Utils.persistenceCapableFor(event);
        final AdapterManager adapterManager = getAdapterManager();
        final ObjectAdapter adapter = adapterManager.getAdapterFor(pojo);
        return phase + " " + location.prefix + " " + LifecycleEventType.lookup(event.getEventType()) + ": oid=" + (adapter !=null? adapter.getOid(): "(null)") + " ,pojo " + pojo;
    }

    
    // /////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }
}
