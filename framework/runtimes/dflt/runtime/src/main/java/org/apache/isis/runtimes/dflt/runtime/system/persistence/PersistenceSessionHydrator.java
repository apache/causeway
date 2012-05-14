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

import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface PersistenceSessionHydrator extends Injectable {

    /**
     * Returns an {@link ObjectAdapter adapter} of the
     * {@link ObjectSpecification type} specified.
     * 
     * <p>
     * If an adapter exists in the {@link AdapterManager map} then that adapter
     * is returned immediately. Otherwise a new domain object of the type
     * specified is {@link ObjectFactory created} and then an adapter is
     * recreated as per {@link #recreateAdapter(Oid, Object)}.
     * 
     * <p>
     * Note: the similar looking method
     * {@link PersistenceSessionContainer#loadObject(TypedOid)}
     * retrieves the existing object from the persistent store (if not available
     * in the {@link AdapterManager maps} . Once the object has been retrieved,
     * the object store calls back to {@link #recreateAdapter(Oid, Object)} to
     * map it.
     * 
     * @see #recreateAdapter(Oid, Object)
     * @see PersistenceSessionContainer#loadObject(TypedOid)
     */
    ObjectAdapter recreateAdapter(ObjectSpecification specification, Oid oid);

    /**
     * Returns an {@link ObjectAdapter adapter} with the
     * {@link ObjectSpecification type} determined from the provided
     * {@link RootOidWithSpecification oid}.
     *
     * <p>
     * If an adapter exists in the {@link AdapterManager map} then that adapter
     * is returned immediately. Otherwise a new domain object of the type
     * specified is {@link ObjectFactory created} and then an adapter is
     * recreated as per {@link #recreateAdapter(Oid, Object)}.
     * 
     * <p>
     * Note: the similar looking method
     * {@link PersistenceSessionContainer#loadObject(TypedOid)}
     * retrieves the existing object from the persistent store (if not available
     * in the {@link AdapterManager maps} . Once the object has been retrieved,
     * the object store calls back to {@link #recreateAdapter(Oid, Object)} to
     * map it.
     * 
     * @see #recreateAdapter(Oid, Object)
     * @see PersistenceSessionContainer#loadObject(TypedOid)
     */
    ObjectAdapter recreateAdapter(TypedOid oid);

    /**
     * Returns an adapter for the provided {@link Oid}, wrapping the provided
     * domain object.
     * 
     * <p>
     * If an adapter exists in the {@link AdapterManager map} for either the
     * {@link Oid} or the domain object then that adapter is returned
     * immediately. Otherwise a new adapter is created using the specified
     * {@link Oid} and its resolved state set to either
     * {@link ResolveState#TRANSIENT} or {@link ResolveState#GHOST} based on
     * whether the {@link Oid} is {@link Oid#isTransient() transient} or not.
     */
    ObjectAdapter recreateAdapter(Oid oid, Object pojo);
    

}
