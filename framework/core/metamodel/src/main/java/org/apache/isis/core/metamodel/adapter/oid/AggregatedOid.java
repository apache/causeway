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

package org.apache.isis.core.metamodel.adapter.oid;

import java.io.Serializable;

import com.google.common.base.Objects;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Aggregated;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * Used as the {@link Oid} for {@link Aggregated} {@link ObjectAdapter}s
 *
 * <p>
 * Aggregated adapters are created explicitly by the application using 
 * {@link DomainObjectContainer#newAggregatedInstance(Object, Class)}.
 * 
 * @see CollectionOid
 */
public final class AggregatedOid extends ParentedOid implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String localId;

    private int cachedHashCode;

    // /////////////////////////////////////////////////////////
    // Constructor, Encodeable
    // /////////////////////////////////////////////////////////

    public AggregatedOid(final Oid parentOid, final String localId) {
        super(parentOid);
        Assert.assertNotNull("LocalId required", localId);
        this.localId = localId;
        cacheState();
    }

    
    // /////////////////////////////////////////////////////////
    // Properties
    // /////////////////////////////////////////////////////////

    public String getLocalId() {
        return localId;
    }


    // /////////////////////////////////////////////////////////
    // Value semantics
    // /////////////////////////////////////////////////////////

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return equals((AggregatedOid) other);
    }

    public boolean equals(final AggregatedOid other) {
        return Objects.equal(other.getParentOid(), getParentOid()) && Objects.equal(other.localId, localId);
    }

    @Override
    public int hashCode() {
        cacheState();
        return cachedHashCode;
    }

    private void cacheState() {
        int hashCode = 17;
        hashCode = 37 * hashCode + getParentOid().hashCode();
        hashCode = 37 * hashCode + localId.hashCode();
        cachedHashCode = hashCode;
    }

    
    // /////////////////////////////////////////////////////////
    // asPersistent
    // /////////////////////////////////////////////////////////

    /**
     * When the RootOid is persisted, all its &quot;children&quot;
     * need updating similarly.
     */
    public AggregatedOid asPersistent(Oid newParentOid) {
        return new AggregatedOid(newParentOid, localId);
    }

    // /////////////////////////////////////////////////////////
    // enString
    // /////////////////////////////////////////////////////////

    @Override
    public String enString() {
        return getParentOid().enString() + "@" + localId;
    }

    // /////////////////////////////////////////////////////////
    // toString
    // /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "AOID[" + getParentOid() + "," + localId + "]";
    }

}
