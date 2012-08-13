/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.adapter.mgr;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface AdapterRecreator {

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
     */
    ObjectAdapter recreatePersistentAdapter(TypedOid oid);

    

}
