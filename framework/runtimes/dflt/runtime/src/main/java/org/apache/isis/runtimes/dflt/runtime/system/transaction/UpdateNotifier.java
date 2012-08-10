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

package org.apache.isis.runtimes.dflt.runtime.system.transaction;

import java.util.List;

import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.components.TransactionScopedComponent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * UpdateNotifier provides updates to client making available lists of the
 * latest changed and disposed objects.
 */
public interface UpdateNotifier extends TransactionScopedComponent {

    // //////////////////////////////////////////////////
    // Changed Objects
    // //////////////////////////////////////////////////

    /**
     * Used by the framework to add objects that have just changed.
     */
    void addChangedObject(ObjectAdapter object);

    /**
     * Returns an immutable {@link List} of changed objects.
     * 
     * <p>
     * Each changed object that was added is only ever provided during one call
     * to this method so the list must be processed fully to avoid missing
     * updates.
     */
    List<ObjectAdapter> getChangedObjects();

    // //////////////////////////////////////////////////
    // Disposed Objects
    // //////////////////////////////////////////////////

    /**
     * Used by the framework to add objects that have just been disposed of.
     */
    void addDisposedObject(ObjectAdapter adapter);

    /**
     * Returns an immutable {@link List} of disposed objects.
     * 
     * <p>
     * Each object that was disposed of is only ever provided during one call to
     * this method so the list must be processed fully to avoid missing
     * deletions.
     */
    public List<ObjectAdapter> getDisposedObjects();

    // //////////////////////////////////////////////////
    // Empty, Clear
    // //////////////////////////////////////////////////

    void ensureEmpty();

    void clear();

}
