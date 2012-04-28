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

package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.internal;

import org.apache.isis.core.commons.components.Resettable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;

/**
 * A map of the objects' identities and the adapters' of the objects.
 */
public interface OidAdapterMap extends DebuggableWithTitle, Iterable<Oid>, SessionScopedComponent, Resettable {

    /**
     * Add an adapter for a given oid
     */
    public void add(final Oid oid, final ObjectAdapter adapter);

    /**
     * Remove the adapter for the given oid
     * 
     * @return <tt>true</tt> if an adapter was removed.
     */
    public boolean remove(final Oid oid);


    /**
     * Get the adapter identified by the specified OID.
     */
    public ObjectAdapter getAdapter(final Oid oid);


}
