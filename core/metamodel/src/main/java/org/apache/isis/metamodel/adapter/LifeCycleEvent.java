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


package org.apache.isis.metamodel.adapter;

import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.LoadingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.RemovedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatingCallbackFacet;


public class LifeCycleEvent {
    /**
     * Index for the life cycle method marking the logical creation of an object.
     */
    public static final LifeCycleEvent CREATED = new LifeCycleEvent(CreatedCallbackFacet.class);
    /**
     * Index for the life cycle method marking the end of the deleting process.
     */
    public static final LifeCycleEvent DELETED = new LifeCycleEvent(RemovedCallbackFacet.class);
    /**
     * Index for the life cycle method marking the beginning of the deleting process.
     */
    public static final LifeCycleEvent DELETING = new LifeCycleEvent(RemovingCallbackFacet.class);

    /**
     * Index for the life cycle method marking the end of the loading process.
     */
    public static final LifeCycleEvent LOADED = new LifeCycleEvent(LoadedCallbackFacet.class);

    /**
     * Index for the life cycle method marking the beginning of the loading process.
     */
    public static final LifeCycleEvent LOADING = new LifeCycleEvent(LoadingCallbackFacet.class);

    /**
     * Index for the life cycle method marking the end of the save process.
     */
    public static final LifeCycleEvent SAVED = new LifeCycleEvent(PersistedCallbackFacet.class);

    /**
     * Index for the life cycle method marking the beginning of the save process.
     */
    public static final LifeCycleEvent SAVING = new LifeCycleEvent(PersistingCallbackFacet.class);

    /**
     * Index for the life cycle method marking the end of the updating process.
     */
    public static final LifeCycleEvent UPDATED = new LifeCycleEvent(UpdatedCallbackFacet.class);

    /**
     * Index for the life cycle method marking the beginning of the updating process.
     */
    public static final LifeCycleEvent UPDATING = new LifeCycleEvent(UpdatingCallbackFacet.class);

    private final Class<? extends Facet> cls;

    private LifeCycleEvent(final Class<? extends Facet> cls) {
        this.cls = cls;
    }

    public Class<? extends Facet> getFacetClass() {
        return cls;
    }

}

