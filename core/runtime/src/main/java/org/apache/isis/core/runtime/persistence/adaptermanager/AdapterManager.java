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


package org.apache.isis.core.runtime.persistence.adaptermanager;

import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.identifier.Identified;


/**
 * Responsible for managing the {@link ObjectAdapter adapter}s and {@link Oid identities} for each and every
 * POJO that is being used by the framework.
 * 
 * <p>
 * It provides a consistent set of adapters in memory, providing an {@link ObjectAdapter adapter} for the POJOs
 * that are in use ensuring that the same object is not loaded twice into memory.
 * 
 * <p>
 * Each POJO is given an {@link ObjectAdapter adapter} so that the framework can work with the POJOs even though
 * it does not understand their types. Each POJO maps to an {@link ObjectAdapter adapter} and these are reused.
 */
public interface AdapterManager extends AdapterManagerLookup, Injectable {

    
    
    ///////////////////////////////////////////////////////////
    // lookup/creation
    ///////////////////////////////////////////////////////////

    /**
     * Either returns an existing adapter (as per {@link #getAdapterFor(Object)}), otherwise creates either a
     * transient, standalone or aggregated {@link ObjectAdapter adapter} for the supplied domain object,
     * depending on its {@link ObjectSpecification} and the context arguments provided.
     * 
     * <p>
     * If no adapter is found for the provided pojo, then the rules for creating the {@link ObjectAdapter
     * adapter} are as follows:
     * <ul>
     * <li>if the pojo's {@link ObjectSpecification specification} indicates that this is an immutable
     * value, then a {@link ResolveState#VALUE} {@link ObjectAdapter adapter} is created
     * <li>otherwise, if context <tt>ownerAdapter</tt> and <tt>identified</tt> arguments have both been
     * provided and also either the {@link Identified} argument indicates that for this particular
     * property/collection the object is aggregated <i>or</i> that the pojo's own
     * {@link ObjectSpecification specification} indicates that the pojo is intrinsically aggregated,
     * then an {@link ObjectAdapter#isAggregated() aggregated} adapter is created. Note that the
     * {@link ResolveState} of such {@link ObjectAdapter's} is independent of its <tt>ownerAdapter</tt>, but it
     * has the same {@link ObjectAdapter#setOptimisticLock(Version) optimistic locking version}.
     * <li>otherwise, a {@link ResolveState#TRANSIENT} {@link ObjectAdapter adapter} is created.
     * </ul>
     * 
     * @param pojo
     *            - pojo to adapt
     * @param ownerAdapter
     *            - only used if aggregated
     * @param identifier
     *            - only used if aggregated
     */
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter, Identified identified);

    /**
     * Either returns an existing adapter (as per {@link #getAdapterFor(Object)}), otherwise creates either a
     * transient root or a standalone {@link ObjectAdapter adapter} for the supplied domain object, depending on
     * its {@link ObjectSpecification}.
     * 
     * <p>
     * The rules for creating a {@link ResolveState#VALUE standalone} vs {@link ResolveState#TRANSIENT
     * transient} root {@link ObjectAdapter adapter} are as for
     * {@link #adapterFor(Object, ObjectAdapter, Identified)}.
     * 
     * <p>
     * Historical notes: previously called <tt>createAdapterForTransient</tt>, though this name wasn't quite
     * right.
     */
    ObjectAdapter adapterFor(Object pojo);


    ///////////////////////////////////////////////////////////
    // removal
    ///////////////////////////////////////////////////////////

    /**
     * Removes the specified {@link ObjectAdapter adapter} from the identity maps.
     */
    void removeAdapter(ObjectAdapter adapter);

    /**
     * Removes the {@link ObjectAdapter adapter} identified by the specified {@link Oid}.
     * 
     * <p>
     * Should be same as {@link #getAdapterFor(Oid)} followed by {@link #removeAdapter(ObjectAdapter)}.
     */
	void removeAdapter(Oid oid);

}

