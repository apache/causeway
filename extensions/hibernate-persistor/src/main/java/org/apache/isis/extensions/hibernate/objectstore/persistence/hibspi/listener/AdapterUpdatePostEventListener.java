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
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.runtime.persistence.PersistorUtil;


/**
 * Respond to events within Hibernate which need to be reflected within the [[NAME]] System.
 */
public class AdapterUpdatePostEventListener extends AdapterEventListenerAbstract
        implements PostUpdateEventListener {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(AdapterUpdatePostEventListener.class);

    
    public void onPostUpdate(final PostUpdateEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("PostUpdateEvent " + event.getEntity().getClass() + " " + event.getId());
        }
        final ObjectAdapter adapter = getAdapterFor(event.getEntity());
        clearDirtyFor(adapter);
        if (adapter.getResolveState() == ResolveState.UPDATING) {
            PersistorUtil.end(adapter);
        }
    }

}
