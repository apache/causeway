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


package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;


/**
 * API used solely by the {@link PersistenceSession}.
 */
public interface AdapterManagerPersist {

    /**
     * Remaps the {@link ObjectAdapter adapter} and any associated aggregated (collection) adapters using a 
     * new value for the {@link Oid} provided by the {@link OidGenerator}. 
     * 
     * <p>
     * The {@link Oid} of the supplied {@link ObjectAdapter adapter} should be in such a state that it can be
     * {@link OidGenerator#convertTransientToPersistentOid(Oid) converted from transient to persistent}. Note
     * that some {@link Oid} implementations require an initial state call to do this (eg to read from a
     * database identity or sequence value).
     * 
     * <p>
     * The adapter is remapped in the {@link AdapterManager}, and the {@link Oid#getPrevious() previous} is
     * set to its transient value).  Similarly for any aggregated adapters.  This is needed for client/server so that the client can remap a changed object.
     * 
     * @see AdapterManagerProxy#remapUpdated(Oid)
     */
    void remapAsPersistent(ObjectAdapter adapter);

    /**
     * Either returns an existing {@link ObjectAdapter adapter} (as per {@link #getAdapterFor(Object)} or
     * {@link #getAdapterFor(Oid)}), otherwise re-creates an adapter with the specified (persistent)
     * {@link Oid}.
     * 
     * <p>
     * Typically called when the {@link Oid} is already known, that is, when resolving an already-persisted
     * object. Is also available for <tt>Memento</tt> support however, so {@link Oid} could also represent a
     * {@link Oid#isTransient() transient} object.
     * 
     * <p>
     * If the {@link ObjectAdapter adapter} is recreated, its {@link ResolveState} will be
     * {@link ResolveState#GHOST} if a persistent {@link Oid}, or {@link ResolveState#TRANSIENT} otherwise.
     */
    ObjectAdapter recreateRootAdapter(Oid oid, Object pojo);

}

