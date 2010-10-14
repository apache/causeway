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


package org.apache.isis.extensions.hibernate.objectstore.persistence.algorithm;

import org.apache.log4j.Logger;
import org.apache.isis.commons.lang.ToString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.metamodel.util.CallbackUtils;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.runtime.persistence.objectstore.algorithm.PersistAlgorithmAbstract;
import org.apache.isis.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;


/**
 * A {@link PersistAlgorithm} which simply saves the object made persistent. 
 * 
 * <p>
 * This allows Hibernate to determine which objects should be saved - mappings 
 * should be created with <tt>cascade="save-update"</tt>.
 * 
 * <p>
 * An alternative is to let [[NAME]] do the work, using the
 * {@link TwoPassPersistAlgorithm}.
 */
public class SimplePersistAlgorithm extends PersistAlgorithmAbstract {
    private static final Logger LOG = Logger.getLogger(PersistAlgorithm.class);


    //////////////////////////////////////////////////////////////////
    // name
    //////////////////////////////////////////////////////////////////

    public String name() {
        return "SimplePersistAlgorithm";
    }


    //////////////////////////////////////////////////////////////////
    // makePersistent
    //////////////////////////////////////////////////////////////////

    /**
     * @param toPersistObjectSet - will actually be implemented by the {@link PersistenceSession}.
     */
    public void makePersistent(final ObjectAdapter object, final ToPersistObjectSet toPersistObjectSet) {
        if (alreadyPersistedOrNotPersistable(object)) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("persist " + object);
        }
        // Isis.getObjectLoader().madePersistent(object);
        // Don't do here - allow EventListener to propogate persistent state
        CallbackUtils.callCallback(object, PersistingCallbackFacet.class);
        toPersistObjectSet.addPersistedObject(object);
        CallbackUtils.callCallback(object, PersistedCallbackFacet.class);
    }



    //////////////////////////////////////////////////////////////////
    // toString
    //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        return toString.toString();
    }
}
