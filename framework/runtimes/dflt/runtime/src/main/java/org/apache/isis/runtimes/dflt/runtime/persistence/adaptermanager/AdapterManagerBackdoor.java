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
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionHydrator;

/**
 * Not part of the standard API, but used by the <tt>MemoryObjectStore</tt> to
 * add pre-existing {@link ObjectAdapter adapter}s straight into the identity
 * maps.
 * 
 * <p>
 * Don't think this is used anymore; see instead
 * {@link PersistenceSessionHydrator}.
 */
public interface AdapterManagerBackdoor extends AdapterManager {

    /**
     * Add a pre-existing {@link ObjectAdapter adapter} straight into the maps.
     */
    ObjectAdapter addExistingAdapter(ObjectAdapter object);

}
