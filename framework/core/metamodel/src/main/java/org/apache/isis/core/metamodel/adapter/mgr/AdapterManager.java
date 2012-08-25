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

package org.apache.isis.core.metamodel.adapter.mgr;

import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

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
public interface AdapterManager extends Injectable {

    /**
     * Gets the {@link ObjectAdapter adapter} for the {@link Oid} if it exists
     * in the identity map.
     * 
     * @param oid
     *            - must not be <tt>null</tt>
     * @return adapter, or <tt>null</tt> if doesn't exist.
     */
    ObjectAdapter getAdapterFor(Oid oid);

    /**
     * Gets the {@link ObjectAdapter adapter} for the specified domain object if
     * it exists in the identity map.
     * 
     * <p>
     * Provided by the <tt>AdapterManager</tt> when used by framework.
     * 
     * @param pojo
     *            - must not be <tt>null</tt>
     * @return adapter, or <tt>null</tt> if doesn't exist.
     */
    ObjectAdapter getAdapterFor(Object pojo);

    
    public enum ConcurrencyChecking {
        NO_CHECK,
        CHECK
    }

    /**
     * As per {@link #adapterFor(TypedOid, ConcurrencyChecking)}, with
     * {@value ConcurrencyChecking#NO_CHECK no checking}.
     */
    ObjectAdapter adapterFor(TypedOid oid);
    

    /**
     * Either returns an existing {@link ObjectAdapter adapter} (as per 
     * {@link #getAdapterFor(Oid)}), otherwise re-creates an adapter with the 
     * specified (persistent) {@link Oid}.
     * 
     * <p>
     * Typically called when the {@link Oid} is already known, that is, when
     * resolving an already-persisted object. Is also available for
     * <tt>Memento</tt> support however, so {@link Oid} could also represent a
     * {@link Oid#isTransient() transient} object.
     * 
     * <p>
     * The pojo itself is recreated by delegating to a {@link PojoRecreator} implementation.
     * The default impl just uses the {@link ObjectSpecification#createObject()};
     * however object stores (eg JDO/DataNucleus) can provide alternative implementations
     * in order to ensure that the created pojo is attached to a persistence context.
     * 
     * <p>
     * If the {@link ObjectAdapter adapter} is recreated, its
     * {@link ResolveState} will be set to {@link ResolveState#GHOST}.
     *
     * <p>
     * The {@link ConcurrencyChecking} parameter determines whether concurrency checking is performed.
     * If it is requested, then a check is made to ensure that the {@link Oid#getVersion() version} 
     * of the {@link TypedOid oid} of the recreated adapter is the same as that of the provided {@link TypedOid oid}.
     * If the version differs, then a {@link ConcurrencyException} is thrown. 
     */
    ObjectAdapter adapterFor(TypedOid oid, ConcurrencyChecking concurrencyChecking);

    /**
     * Looks up or creates a standalone (value) or root adapter.
     */
    ObjectAdapter adapterFor(Object domainObject);
    
    /**
     * Looks up or creates a standalone (value), aggregated or root adapter.
     */
    ObjectAdapter adapterFor(Object domainObject, ObjectAdapter parentAdapter);

    /**
     * Looks up or creates a collection adapter.
     */
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter parentAdapter, OneToManyAssociation collection);

}
