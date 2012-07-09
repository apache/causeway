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

package org.apache.isis.core.metamodel.adapter;

import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.CollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectMetaModel;

/**
 * Adapters to domain objects, where the application is written in terms of
 * domain objects and those objects are represented within the NOF through these
 * adapter, and not directly.
 * 
 * @see ObjectMetaModel
 */
public interface ObjectAdapter extends ObjectMetaModel {

    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     * 
     * <p>
     * May return <code>null</code> if no icon is specified.
     */
    String getIconName();

    /**
     * Changes the 'lazy loaded' state of the domain object.
     * 
     * @see ResolveState
     */
    void changeState(ResolveState newState);

    /**
     * Checks the version of this adapter to make sure that it does not differ
     * from the specified version.
     * 
     * @throws ConcurrencyException
     *             if the specified version differs from the version held this
     *             adapter.
     */
    void checkLock(Version version);

    /**
     * The object's unique {@link Oid}. 
     * 
     * <p>
     * This id allows the object to added to, stored by,
     * and retrieved from the object store.  Objects can be looked up by their
     * {@link Oid} from the {@link AdapterMap}.
     * 
     * <p>
     * Note that standalone value objects ("foobar", or 5, or a date),
     * are not mapped and have a <tt>null</tt> oid.
     */
    Oid getOid();

    /**
     * Since {@link Oid}s are now immutable, it is the reference from the 
     * {@link ObjectAdapter} to its {@link Oid} that must now be updated. 
     */
    void replaceOid(Oid persistedOid);

    /**
     * Determines what 'lazy loaded' state the domain object is in.
     * 
     * @see ResolveState
     */
    ResolveState getResolveState();

    Version getVersion();

    void setVersion(Version version);

    void fireChangedEvent();

    /**
     * Whether this instance belongs to another object (meaning its
     * {@link #getOid()} will be <tt>ParentedOid</tt>, either an 
     * {@link AggregatedOid} or a {@link CollectionOid}).
     */
    boolean isParented();

    /**
     * Whether this is an aggregated Oid.
     */
    boolean isAggregated();

    /**
     * Whether this is a value (standalone, has no oid).
     */
    boolean isValue();


    /**
     * Either the aggregate root (either itself or, if parented, then its parent adapter).
     * 
     * TODO: should this be recursive, to support root->aggregate->aggregate etc.
     */
    ObjectAdapter getAggregateRoot();

    boolean isResolved();

    boolean isGhost();

    boolean isTitleAvailable();

    void markAsResolvedIfPossible();

    boolean isDestroyed();

    boolean isTransient();

    boolean isResolving();

    boolean isUpdating();

    boolean isNew();



}
