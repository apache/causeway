package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.spi;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithmAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;


/**
 * A {@link PersistAlgorithm} which simply saves the object made persistent.
 */
public class OpenJpaSimplePersistAlgorithm extends PersistAlgorithmAbstract {
    
    private static final Logger LOG = Logger
            .getLogger(OpenJpaSimplePersistAlgorithm.class);


    // ////////////////////////////////////////////////////////////////
    // name
    // ////////////////////////////////////////////////////////////////

    public String name() {
        return "SimplePersistAlgorithm";
    }


    // ////////////////////////////////////////////////////////////////
    // makePersistent
    // ////////////////////////////////////////////////////////////////

    /**
     * @see NakedInsertPostEventListener#onPostInsert(org.hibernate.event.PostInsertEvent)
     */
    public void makePersistent(final ObjectAdapter adapter,
            final ToPersistObjectSet toPersistObjectSet) {
        if (alreadyPersistedOrNotPersistable(adapter)) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("persist " + adapter);
        }
        CallbackUtils.callCallback(adapter, PersistingCallbackFacet.class);
        toPersistObjectSet.addCreateObjectCommand(adapter);
    }


    // ////////////////////////////////////////////////////////////////
    // toString
    // ////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        return toString.toString();
    }
}
// Copyright (c) Naked Objects Group Ltd.
