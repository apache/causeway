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

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;

/**
 * Adapters to domain objects, where the application is written in terms of domain objects and those objects are
 * represented within the NOF through these adapter, and not directly.
 * 
 * @see ObjectMetaModel
 */
public interface ObjectAdapter extends ObjectMetaModel {

    /**
     * Returns the name of an icon to use if this object is to be displayed graphically.
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
     * Checks the version of this adapter to make sure that it does not differ from the specified version.
     * 
     * @throws ConcurrencyException
     *             if the specified version differs from the version held this adapter.
     */
    void checkLock(Version version);

    /**
     * The objects unique id. This id allows the object to added to, stored by, and retrieved from the object store.
     */
    Oid getOid();

    /**
     * Determines what 'lazy loaded' state the domain object is in.
     * 
     * @see ResolveState
     */
    ResolveState getResolveState();

    /**
     * Returns the current version of the domain object.
     */
    Version getVersion();

    /**
     * Sets the versions of the domain object.
     */
    void setOptimisticLock(Version version);

    void fireChangedEvent();

    /**
     * Whether this instance belongs to another object (meaning its {@link #getOid()} will be <tt>AggregatedOid</tt>.
     */
    boolean isAggregated();

    /**
     * Either itself or its parent adapter (if aggregated).
     * @return
     */
    ObjectAdapter getAggregateRoot();

}
