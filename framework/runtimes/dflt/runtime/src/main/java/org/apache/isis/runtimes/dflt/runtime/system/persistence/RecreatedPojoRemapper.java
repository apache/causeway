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

package org.apache.isis.runtimes.dflt.runtime.system.persistence;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;

public interface RecreatedPojoRemapper {

    /**
     * Either returns an existing {@link ObjectAdapter adapter} (as per
     * {@link #getAdapterFor(Object)} or {@link #getAdapterFor(Oid)}), otherwise
     * re-creates an adapter with the specified (persistent) {@link Oid}.
     * 
     * <p>
     * Typically called when the {@link Oid} is already known, that is, when
     * resolving an already-persisted object. Is also available for
     * <tt>Memento</tt> support however, so {@link Oid} could also represent a
     * {@link Oid#isTransient() transient} object.
     * 
     * <p>
     * If the {@link ObjectAdapter adapter} is recreated, its
     * {@link ResolveState} will be set to either
     * {@link ResolveState#TRANSIENT} or {@link ResolveState#GHOST} based on
     * whether the {@link Oid} is {@link Oid#isTransient() transient} or not.
     * 
     * @param oid
     * @param recreatedPojo - already known to the object store impl, or a service
     */
    ObjectAdapter mapRecreatedPojo(Oid oid, Object recreatedPojo);
    
    void remapRecreatedPojo(ObjectAdapter adapter, Object recreatedPojo);

}
