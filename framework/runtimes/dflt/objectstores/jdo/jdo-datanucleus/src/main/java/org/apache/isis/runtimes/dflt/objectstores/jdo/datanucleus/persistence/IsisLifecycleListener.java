package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.ClearLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DetachLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;
import javax.jdo.spi.PersistenceCapable;

import com.google.common.collect.Maps;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistorUtil;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionHydrator;

public class IsisLifecycleListener implements AttachLifecycleListener, ClearLifecycleListener, CreateLifecycleListener, DeleteLifecycleListener, DetachLifecycleListener, DirtyLifecycleListener, LoadLifecycleListener, StoreLifecycleListener, SuspendableListener {

    private static final Logger LOG = Logger.getLogger(IsisLifecycleListener.class);

    private boolean suspended;

    @Override
    public void postCreate(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.POST, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
    }

    @Override
    public void preAttach(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.PRE, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }

        ensureRootObject(event);
        ensureFrameworksInAgreement(event);
    }

    @Override
    public void postAttach(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.POST, event));
        }

        ensureRootObject(event);
        ensureFrameworksInAgreement(event);
    }

    @Override
    public void postLoad(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.POST, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
        
        final PersistenceCapable pojo = persistenceCapableFor(event);
        
        final RootOid oid ;
        ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        if(adapter != null) {
            ensureRootObject(event);
            oid = (RootOid) adapter.getOid();
        } else {
            final OidGenerator oidGenerator = getOidGenerator();
            oid = oidGenerator.createPersistent(pojo, null);
            
            // it's possible that there is already an adapter for this Oid, ie from ObjectStore#resolveImmediately()
            adapter = getAdapterManager().getAdapterFor(oid);
            if(adapter != null) {
                getAdapterManager().removeAdapter(adapter);
                adapter.replacePojo(pojo);
                getAdapterManager().addExistingAdapter(adapter);
            } else {
                PersistenceSessionHydrator hydrator = getPersistenceSession();
                adapter = hydrator.recreateAdapter(oid, pojo);
                PersistorUtil.startResolving(adapter);
                PersistorUtil.endResolving(adapter);
            }
        }
        adapter.setVersion(getVersionIfAny(pojo));

        ensureFrameworksInAgreement(event);
    }

    @Override
    public void preStore(InstanceLifecycleEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.PRE, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }

    }

    @Override
    public void postStore(InstanceLifecycleEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.POST, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }

        ensureRootObject(event);
        
        
        final PersistenceCapable pojo = persistenceCapableFor(event);
        
        
        if(!pojo.jdoIsPersistent()) {
            throw new IllegalStateException("Pojo JDO state is not persistent! pojo dnOid: " + JDOHelper.getObjectId(pojo));
        }

        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        final RootOid isisOid = (RootOid) adapter.getOid();
        
        Class<? extends CallbackFacet> callbackFacetClass;
        if (isisOid.isTransient()) {
            final RootOid persistentOid = getOidGenerator().createPersistent(pojo, isisOid);
            
            // most of the magic is here...
            getAdapterManager().remapAsPersistent(adapter, persistentOid);

            callbackFacetClass = PersistedCallbackFacet.class;
        } else {
            callbackFacetClass = UpdatedCallbackFacet.class;
        }
        
        clearDirtyFor(adapter);
        
        adapter.setVersion(getVersionIfAny(pojo));
        CallbackUtils.callCallback(adapter, callbackFacetClass);

        ensureFrameworksInAgreement(event);
    }

    @Override
    public void preDirty(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.PRE, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }

        ensureRootObject(event);
        ensureFrameworksInAgreement(event);
    }

    @Override
    public void postDirty(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.POST, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
        ensureRootObject(event);
        ensureFrameworksInAgreement(event);
    }

    @Override
    public void preDelete(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.PRE, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
        ensureRootObject(event);
        ensureFrameworksInAgreement(event);
    }

    @Override
    public void postDelete(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.POST, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
        ensureRootObject(event);
        ensureFrameworksInAgreement(event);
    }

    @Override
    public void preClear(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.PRE, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
        //ensureRootObject(event);
        //ensureFrameworksInAgreement(event);
    }

    @Override
    public void postClear(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.POST, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
        //ensureRootObject(event);
        //ensureFrameworksInAgreement(event);
    }

    @Override
    public void preDetach(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.PRE, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
        ensureRootObject(event);
        ensureFrameworksInAgreement(event);
    }

    @Override
    public void postDetach(InstanceLifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(Phase.POST, event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }
        ensureRootObject(event);
        ensureFrameworksInAgreement(event);
    }

    // /////////////////////////////////////////////////////////
    // SuspendListener
    // /////////////////////////////////////////////////////////

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    // /////////////////////////////////////////////////////////
    // Helpers
    // /////////////////////////////////////////////////////////

    private void ensureFrameworksInAgreement(InstanceLifecycleEvent event) {
        final PersistenceCapable pojo = persistenceCapableFor(event);
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        final Oid oid = adapter.getOid();

        if(!pojo.jdoIsPersistent()) {
            // make sure the adapter is transient
            if (!adapter.getResolveState().isTransient()) {
                throw new IsisException(MessageFormat.format("adapter is in invalid state; should be {0} but is {1}", ResolveState.TRANSIENT, adapter.getResolveState()));
            }

            // make sure the oid is transient
            if (!oid.isTransient()) {
                throw new IsisException(MessageFormat.format("Not transient: oid={0}, for {1}", oid, pojo));
            }

        } else {
            // make sure the adapter is persistent
            if (!adapter.getResolveState().representsPersistent()) {
                throw new IsisException(MessageFormat.format("adapter is in invalid state; should be in a persistent state but is {1}", ResolveState.RESOLVED, adapter.getResolveState()));
            }

            // make sure the oid is persistent
            if (oid.isTransient()) {
                throw new IsisException(MessageFormat.format("Transient: oid={0}, for {1}", oid, pojo));
            }

        }
    }

    // make sure the entity is known to Isis and is a root
    // TODO: will probably need to handle aggregated entities at some point...
    private void ensureRootObject(InstanceLifecycleEvent event) {
        final PersistenceCapable pojo = persistenceCapableFor(event);
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        if(adapter == null) {
            throw new IsisException(MessageFormat.format("Object not yet known to Isis: {0}", pojo));
        }
        final Oid oid = adapter.getOid();
        if (!(oid instanceof RootOid)) {
            throw new IsisException(MessageFormat.format("Not a RootOid: oid={0}, for {1}", oid, pojo));
        }
    }



    private void ensureObjectNotLoaded(InstanceLifecycleEvent event) {
        final PersistenceCapable pojo = persistenceCapableFor(event);
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        if(adapter != null) {
            final Oid oid = adapter.getOid();
            throw new IsisException(MessageFormat.format("Object is already mapped in Isis: oid={0}, for {1}", oid, pojo));
        }
    }

    
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

    private static String logString(Phase phase, InstanceLifecycleEvent event) {
        return phase + " " + LifecycleEventType.lookup(event.getEventType()) + ": pojo " + event.getSource();
    }

    private static void clearDirtyFor(final ObjectAdapter adapter) {
        adapter.getSpecification().clearDirty(adapter);
    }

    private static PersistenceCapable persistenceCapableFor(InstanceLifecycleEvent event) {
        return (PersistenceCapable)event.getSource();
    }

    @SuppressWarnings("unused")
    private static Object jdoObjectIdFor(InstanceLifecycleEvent event) {
        PersistenceCapable persistenceCapable = persistenceCapableFor(event);
        Object jdoObjectId = persistenceCapable.jdoGetObjectId();
        return jdoObjectId;
    }

    private Version getVersionIfAny(final PersistenceCapable pojo) {
        Object jdoVersion = pojo.jdoGetVersion();
        if(jdoVersion instanceof Long) {
            return new SerialNumberVersion((Long) jdoVersion, getAuthenticationSession().getUserName(), new Date()); 
        } 
        return null;
    }


    // /////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////

    protected AdapterManagerExtended getAdapterManager() {
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


}
