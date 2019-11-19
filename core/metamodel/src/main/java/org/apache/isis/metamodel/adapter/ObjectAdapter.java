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

package org.apache.isis.metamodel.adapter;

import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * Adapters to domain objects, where the application is written in terms of
 * domain objects and those objects are represented within the NOF through these
 * adapter, and not directly.
 */
public interface ObjectAdapter extends ManagedObject {

    /**
     * The object's unique {@link Oid}.
     *
     * <p>
     * This id allows the object to added to, stored by,
     * and retrieved from the object store.  Objects can be looked up by their
     * {@link Oid}.
     *
     * <p>
     * Note that standalone value objects ("foobar", or 5, or a date),
     * are not mapped and have a <tt>null</tt> oid.
     */
    Oid getOid(); //XXX[2033] referenced by 'metamodel' only to create a bookmark (CommandUtil)

    /**
     * Returns either itself (if this is a root) or for parented collections, the
     * adapter corresponding to their {@link ParentedOid#getParentOid() root oid}.
     */
    ObjectAdapter getAggregateRoot(); //XXX[2033] not referenced by 'metamodel'

    /**
     * Whether this instance belongs to another object (meaning its
     * {@link #getOid()} will be a {@link ParentedOid}).
     */
    default boolean isParentedCollection() {
        return getOid() instanceof ParentedOid;
    }

    @Deprecated boolean isRepresentingPersistent();

}
