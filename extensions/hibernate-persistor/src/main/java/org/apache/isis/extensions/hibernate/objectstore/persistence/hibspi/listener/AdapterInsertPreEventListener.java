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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener;

import org.apache.log4j.Logger;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;


/**
 * Respond to events within Hibernate which need to be reflected within the [[NAME]] System.
 */
public class AdapterInsertPreEventListener extends AdapterEventListenerAbstract
        implements PreInsertEventListener {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(AdapterInsertPreEventListener.class);

    
    /////////////////////////////////////////////////////////
    // loading
    /////////////////////////////////////////////////////////


    /**
     * If {@link PreInsertEvent#getEntity() pojo} is still
     * {@link ResolveState#TRANSIENT transient}, then changes state of its
     * {@link ObjectAdapter adapter} to {@link ResolveState#UPDATING updating}.
     */
    public boolean onPreInsert(final PreInsertEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("PreInsertEvent " + logString(event));
        }
        final Object entity = event.getEntity();
        final ObjectAdapter adapter = getAdapterFor(entity);
        if (adapter.getResolveState() == ResolveState.TRANSIENT) {
            // need to make sure object won't respond to changes in its state
            // This may have already been done by the PersistAlgorithm, and though this is
            // probably the case it can't be guaranteed
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.UPDATING);
        }
        return false; // object not changed
    }


    /////////////////////////////////////////////////////////
    // Helpers (for logging)
    /////////////////////////////////////////////////////////

    private String logString(final PreInsertEvent event) {
        return event.getEntity().getClass() + " " + event.getId();
    }



}
