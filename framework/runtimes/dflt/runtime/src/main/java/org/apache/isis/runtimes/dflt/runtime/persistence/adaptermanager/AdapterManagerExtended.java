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

import org.apache.isis.core.commons.components.Resettable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterLookup;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

/**
 * Extension of the {@link AdapterManager} as viewed by the
 * {@link PersistenceSession}.
 * 
 * <p>
 * Extends the {@link AdapterManager} interface in various ways, providing
 * additional support:
 * <ul>
 * <li>for the {@link PersistenceSession} itself (by extending the
 * {@link AdapterManagerPersist} interface),
 * <li>for tests (by extending {@link AdapterManagerTestSupport}) and,
 * <li>for slightly dodgy implementations (such as the
 * <tt>MemoryObjectStore</tt> that manipulate the identity maps directly (by
 * extending {@link AdapterManagerBackdoor}).
 * </ul>
 */
public interface AdapterManagerExtended extends Iterable<ObjectAdapter>, Resettable, AdapterManager, AdapterManagerPersist, ObjectAdapterLookup, AdapterManagerTestSupport, AdapterManagerBackdoor, DebuggableWithTitle, SessionScopedComponent {

}
