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
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.runtime.persistence.PersistorUtil;


/**
 * Marks {@link ObjectAdapter adapter} as {@link ResolveState#RESOLVED resolved} 
 * (provided it was in state of {@link ResolveState#RESOLVING resolving}) and also clears the 
 * dirty flag on the adapter. 
 * 
 * <p>
 * Occurs after an entity instance is fully loaded.
 */
public class AdapterLoadPostEventListener extends AdapterEventListenerAbstract
        implements PostLoadEventListener {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(AdapterLoadPostEventListener.class);

    

    public void onPostLoad(final PostLoadEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("PostLoadEvent " + logString(event));
        }
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(event.getEntity());
        if (adapter.getResolveState() == ResolveState.RESOLVING) {
            PersistorUtil.end(adapter); // ie RESOLVED
        }
        clearDirtyFor(adapter);
    }



    /////////////////////////////////////////////////////////
    // Helpers (for logging)
    /////////////////////////////////////////////////////////



    private String logString(final PostLoadEvent event) {
        return event.getEntity().getClass() + " " + event.getId();
    }




}
