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
import org.apache.isis.core.metamodel.adapter.ObjectAdapterLookup;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

/**
 * API used solely by the {@link PersistenceSession}, for remoting support.
 * 
 * <p>
 * *** THIS INTERFACE IS NO LONGER IMPLEMENTED, SINCE REMOTING SUPPORT HAS BEEN REMOVED. ***
 */
public interface AdapterManagerProxy extends ObjectAdapterLookup {

    /**
     * Remaps an {@link Oid} that has been {@link Oid#getPrevious() updated} so
     * that its {@link ObjectAdapter adapter} (if any) is mapped to that
     * {@link Oid}.
     * 
     * <p>
     * Part of public API so that the proxy persistor can maintain its maps when
     * it processes a newly persisted object.
     * 
     * @see AdapterManagerSpi#remapAsPersistent(ObjectAdapter, RootOid)
     */
    public void remapUpdated(Oid rootOid);

}
