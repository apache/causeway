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
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;

public interface AdapterManagerTestSupport {

    /**
     * For testing purposes, creates an {@link ObjectAdapter adapter} for the
     * supplied domain object with the specified {@link RootOid}
     * 
     * <p>
     * The usual way of creating {@link ObjectAdapter adapter}s is using
     * {@link #adapterFor(Object)}, using the <tt>OidGenerator</tt> to obtain an
     * {@link RootOid}. This test-support method differs because it allows the
     * {@link RootOid} to be specified explicitly.
     * 
     * <p>
     * Note that the {@link RootOid} must represent a {@link Oid#isTransient()
     * transient} object. If an {@link ObjectAdapter adapter} is required for a
     * persistent {@link RootOid}, just use
     * {@link #recreateAdapter(RootOid, Object)}.
     * 
     * @see #adapterFor(Object)
     * @see #recreateAdapter(RootOid, Object)
     */
    ObjectAdapter testCreateTransient(Object pojo, RootOid oid);

}
