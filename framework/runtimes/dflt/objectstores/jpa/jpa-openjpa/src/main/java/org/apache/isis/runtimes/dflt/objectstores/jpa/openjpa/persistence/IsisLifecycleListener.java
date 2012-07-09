package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence;

import java.text.MessageFormat;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.log4j.Logger;
import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.LifecycleEvent;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionHydrator;

public class IsisLifecycleListener extends AbstractLifecycleListener implements SuspendableListener {

    private static final Logger LOG = Logger.getLogger(IsisLifecycleListener.class);

    private boolean suspended;

    @Override
    public void beforePersist(LifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }

        
        final Object pojo = event.getSource();
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        final Oid oid = adapter.getOid();
        
        // make sure the adapter is transient
        if (!adapter.representsTransient()) {
            throw new IsisException(MessageFormat.format("adapter is in invalid state; should be {0} but is {1}", ResolveState.TRANSIENT, adapter.getResolveState()));
        }

        // make sure the oid is transient
        if (!oid.isTransient()) {
            throw new IsisException(MessageFormat.format("Not transient: oid={0}, for {1}", oid, pojo));
        }

        // make sure the entity is a root
        // TODO: will probably need to handle aggregated entities at some point...
        if (!(oid instanceof RootOid)) {
            throw new IsisException(MessageFormat.format("Not a RootOid: oid={0}, for {1}", oid, pojo));
        }
    }


    
    @Override
    public void afterPersist(LifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }

        final Object pojo = event.getSource();

        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
        final RootOid transientOid = (RootOid) adapter.getOid();

        final RootOid persistentOid = getOidGenerator().createPersistent(pojo, transientOid);

        // most of the magic is here...
        getAdapterManager().remapAsPersistent(adapter, persistentOid);

        clearDirtyFor(adapter);
        CallbackUtils.callCallback(adapter, PersistedCallbackFacet.class);
    }

    @Override
    public void afterLoad(LifecycleEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(logString(event));
        }
        if (isSuspended()) {
            LOG.debug(" [currently suspended - ignoring]");
            return;
        }

        final Object pojo = event.getSource();

        final OidGenerator oidGenerator = getOidGenerator();
        final RootOid oid = oidGenerator.createPersistent(pojo, null);

        PersistenceSessionHydrator hydrator = getPersistenceSession();
        hydrator.recreateAdapter(oid, pojo);
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

    private static Map<Integer, LifecycleEventType> events = Maps.newHashMap();
    private enum LifecycleEventType {
        BEFORE_PERSIST(0),
        AFTER_PERSIST(1),
        AFTER_PERSIST_PERFORMED(18),
        AFTER_LOAD(2),
        BEFORE_STORE(3),
        AFTER_STORE(4),
        BEFORE_CLEAR(5),
        AFTER_CLEAR(6),
        BEFORE_DELETE(7),
        AFTER_DELETE(8),
        AFTER_DELETE_PERFORMED(19),
        BEFORE_DIRTY(9),
        AFTER_DIRTY(10),
        BEFORE_DIRTY_FLUSHED(11),
        AFTER_DIRTY_FLUSHED(12),
        BEFORE_DETACH(13),
        AFTER_DETACH(14),
        BEFORE_ATTACH(15),
        AFTER_ATTACH(16),
        AFTER_REFRESH(17),
        BEFORE_UPDATE(20),
        AFTER_UPDATE_PERFORMED(21);

        private LifecycleEventType(int code) {
            events.put(code, this);
        }
        
        public static LifecycleEventType lookup(int code) {
            return events.get(code);
        }
    }


    private static String logString(LifecycleEvent event) {
        return LifecycleEventType.lookup(event.getType()) + ": pojo " + event.getSource();
    }


    private static void clearDirtyFor(final ObjectAdapter adapter) {
        adapter.getSpecification().clearDirty(adapter);
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

}
