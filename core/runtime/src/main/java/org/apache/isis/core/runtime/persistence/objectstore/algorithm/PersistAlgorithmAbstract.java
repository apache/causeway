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

package org.apache.isis.core.runtime.persistence.objectstore.algorithm;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.NotPersistableException;

public abstract class PersistAlgorithmAbstract implements PersistAlgorithm {

    // ////////////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////////////

    public void init() {
    }

    public void shutdown() {
    }

    // ////////////////////////////////////////////////////////////////
    // helpers
    // ////////////////////////////////////////////////////////////////

    /**
     * Whether the persist algorithm should skip over this object.
     * 
     * <p>
     * There are various reasons why an object should not be persisted:
     * <ul>
     * <li>it is already persisted
     * <li>its {@link ObjectSpecification specification} indicates instances of
     * its type should not be persisted.
     * <li>it is {@link ResolveState#VALUE standalone}
     * <li>it is a {@link ObjectSpecification#isService() service}.
     * </ul>
     * 
     * <p>
     * Implementation note: the only reason that this method has not been
     * combined with the weaker check in
     * {@link #alreadyPersistedOrNotPersistable(ObjectAdapter)} is because of
     * existing code that throws an exception if the latter is not fulfilled.
     * <b><i>REVIEW: should try to combine and remove the other method</i></b>.
     */
    protected static boolean alreadyPersistedOrNotPersistableOrServiceOrStandalone(final ObjectAdapter adapter) {
        return adapter.isValue() || objectSpecIsService(adapter) || alreadyPersistedOrNotPersistable(adapter);
    }

    /**
     * If has a {@link ResolveState} that is already persisted or has a
     * {@link ObjectSpecification specification} that indicates instances of its
     * type should not be persisted.
     * 
     * @see #alreadyPersistedOrNotPersistableOrServiceOrStandalone(ObjectAdapter)
     */
    protected static boolean alreadyPersistedOrNotPersistable(final ObjectAdapter adapter) {
        return adapter.representsPersistent() || objectSpecNotPersistable(adapter);
    }

    /**
     * As per {@link #alreadyPersistedOrNotPersistable(ObjectAdapter)}, ensures
     * object can be persisted else throws {@link NotPersistableException}.
     */
    protected static void assertObjectNotPersistentAndPersistable(final ObjectAdapter object) {
        if (alreadyPersistedOrNotPersistable(object)) {
            throw new NotPersistableException("can't make object persistent - either already persistent, " + "or transient only: " + object);
        }
    }

    private static boolean objectSpecNotPersistable(final ObjectAdapter adapter) {
        return !adapter.getSpecification().persistability().isPersistable() || adapter.isParented();
    }

    private static boolean objectSpecIsService(final ObjectAdapter adapter) {
        return adapter.getSpecification().isService();
    }

}
