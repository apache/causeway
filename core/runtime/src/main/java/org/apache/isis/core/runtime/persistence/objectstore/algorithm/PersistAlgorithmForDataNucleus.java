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
package org.apache.isis.core.runtime.persistence.objectstore.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;


/**
 * A {@link PersistAlgorithm} which simply saves the object made persistent.
 */
class PersistAlgorithmForDataNucleus extends PersistAlgorithmAbstract {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(PersistAlgorithmForDataNucleus.class);

    public PersistAlgorithmForDataNucleus() {

    }


    // ////////////////////////////////////////////////////////////////
    // name
    // ////////////////////////////////////////////////////////////////

    public String name() {
        return "SimplePersistAlgorithm";
    }


    // ////////////////////////////////////////////////////////////////
    // makePersistent
    // ////////////////////////////////////////////////////////////////

    public void makePersistent(final ObjectAdapter adapter,
            final PersistenceSession persistenceSession) {
        if (alreadyPersistedOrNotPersistable(adapter)) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist " + adapter);
        }

        // previously we called the PersistingCallback here.
        // this is now done in the JDO framework synchronizer.
        //
        // the guard below used to be because (apparently)
        // the callback might have caused the adapter to become persistent.
        // leaving it in as think it does no harm...
        if (alreadyPersistedOrNotPersistable(adapter)) {
            return;
        }
        persistenceSession.addCreateObjectCommand(adapter);
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
