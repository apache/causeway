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

import java.io.IOException;
import java.io.Serializable;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;

/**
 * Used as the {@link Oid} for collections, values and <tt>@Aggregated</tt>
 * types.
 */
public final class AggregatedOid implements Oid, Serializable {

    private static final long serialVersionUID = 1L;

    private final Oid parentOid;
    private final String id;

    private AggregatedOid previous;

    private int cachedHashCode;

    // /////////////////////////////////////////////////////////
    // Constructor, Encodeable
    // /////////////////////////////////////////////////////////
    public AggregatedOid(final Oid oid, final String id) {
        Assert.assertNotNull("ID required", id);
        this.parentOid = oid;
        this.id = id;
        initialized();
    }

    public AggregatedOid(final DataInputExtended input) throws IOException {
        this.parentOid = input.readEncodable(Oid.class);
        this.id = input.readUTF();
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeEncodable(parentOid);
        output.writeUTF(id);
    }

    private void initialized() {
        cacheState();
    }

    // /////////////////////////////////////////////////////////
    // Properties
    // /////////////////////////////////////////////////////////

    public Oid getParentOid() {
        return parentOid;
    }

    public String getId() {
        return id;
    }

    // /////////////////////////////////////////////////////////
    // makePersistent
    // /////////////////////////////////////////////////////////

    @Override
    public void makePersistent() {
        this.previous = new AggregatedOid(this.parentOid, this.id);
        cacheState();
    }

    // /////////////////////////////////////////////////////////
    // Previous
    // /////////////////////////////////////////////////////////

    @Override
    public Oid getPrevious() {
        return previous;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void clearPrevious() {
    }

    // /////////////////////////////////////////////////////////
    // Other OID stuff
    // /////////////////////////////////////////////////////////

    @Override
    public void copyFrom(final Oid oid) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isTransient() {
        return parentOid.isTransient();
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
        return other.parentOid.equals(parentOid) && other.id.equals(id);
    }

    @Override
    public int hashCode() {
        cacheState();
        return cachedHashCode;
    }

    private void cacheState() {
        int hashCode = 17;
        hashCode = 37 * hashCode + parentOid.hashCode();
        hashCode = 37 * hashCode + id.hashCode();
        cachedHashCode = hashCode;
    }

    // /////////////////////////////////////////////////////////
    // toString
    // /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "AOID[" + parentOid + "," + id + "]";
    }

}
