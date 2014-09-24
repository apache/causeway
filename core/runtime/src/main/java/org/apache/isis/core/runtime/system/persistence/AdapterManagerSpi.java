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

package org.apache.isis.core.runtime.system.persistence;

import org.apache.isis.applib.annotation.Aggregated;
import org.apache.isis.core.commons.components.Resettable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm;

/**
 * Responsible for managing the {@link ObjectAdapter adapter}s and {@link Oid
 * identities} for each and every POJO that is being used by the framework.
 * 
 * <p>
 * It provides a consistent set of adapters in memory, providing an
 * {@link ObjectAdapter adapter} for the POJOs that are in use ensuring that the
 * same object is not loaded twice into memory.
 * 
 * <p>
 * Each POJO is given an {@link ObjectAdapter adapter} so that the framework can
 * work with the POJOs even though it does not understand their types. Each POJO
 * maps to an {@link ObjectAdapter adapter} and these are reused.
 */
public interface AdapterManagerSpi extends AdapterManager, Iterable<ObjectAdapter>,
    RecreatedPojoRemapper,
    AdapterLifecycleTransitioner,
    SessionScopedComponent, 
    DebuggableWithTitle,  
    Resettable {



    /**
     * Remaps the {@link ObjectAdapter adapter} and any associated 
     * (collection) adapters (ie, those that wrap the List/Set instances).
     * 
     * <p>
     * Note that it isn't necessary to remap any aggregated adapters (as per
     * {@link Aggregated} annotation; the {@link PersistAlgorithm} calls this
     * method and takes responsibility for locating the graph of transient
     * objects that needs to be remapped.
     * 
     * <p>
     * As a consequence of this call, the adapter's {@link Oid} will be
     * {@link ObjectAdapter#replaceOid(Oid) replaced} (Oids are now immutable).
     * The same is true of the associated collection adapters.
     * 
     * @param hintRootOid - primarily for testing purposes, to set the adapter with a specific rootOid.  Is passed through to the {@link OidGenerator}.
     */
    void remapAsPersistent(ObjectAdapter adapter, RootOid hintRootOid);

    /**
     * Removes the specified {@link ObjectAdapter adapter} from the identity
     * maps.
     */
    void removeAdapter(ObjectAdapter adapter);

}
