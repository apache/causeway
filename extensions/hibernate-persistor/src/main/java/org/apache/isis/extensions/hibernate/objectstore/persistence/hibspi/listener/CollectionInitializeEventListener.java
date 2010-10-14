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
import org.hibernate.HibernateException;
import org.hibernate.event.InitializeCollectionEvent;
import org.hibernate.event.InitializeCollectionEventListener;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;


public class CollectionAdapterInitializeEventListener extends AdapterEventListenerAbstract
        implements InitializeCollectionEventListener {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(CollectionAdapterInitializeEventListener.class);

    
   
    /**
     * Marks the adapters for collections to {@link ResolveState#RESOLVED}
     * (provided that they were {@link ResolveState#isResolvable(ResolveState) resolvable}
     * to {@link ResolveState#RESOLVING}).
     */
    public void onInitializeCollection(final InitializeCollectionEvent event) throws HibernateException {
        LOG.info("InitializeCollectionEvent");
        final ObjectAdapter adapter = getAdapterFor(event.getCollection());
        if (adapter.getResolveState().isResolvable(ResolveState.RESOLVING)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting collection to resolved " + adapter);
            }
            adapter.changeState(ResolveState.RESOLVING);
            adapter.changeState(ResolveState.RESOLVED);
        }
    }

}
