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

import com.google.common.base.Objects;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Aggregated;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

/**
 * Used as the {@link Oid} for {@link Aggregated} {@link ObjectAdapter}s
 *
 * <p>
 * Aggregated adapters are created explicitly by the application using 
 * {@link DomainObjectContainer#newAggregatedInstance(Object, Class)}.
 */
public final class AggregatedOid extends ParentedOid implements TypedOid, Serializable {

    private static final long serialVersionUID = 1L;

    private final ObjectSpecId objectSpecId;
    private final String localId;

    private int cachedHashCode;

    // /////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////

    public AggregatedOid(ObjectSpecId objectSpecId, final TypedOid parentOid, final String localId) {
        super(parentOid);
        Assert.assertNotNull("objectSpecId required", objectSpecId);
        Assert.assertNotNull("LocalId required", localId);
        this.objectSpecId = objectSpecId;
        this.localId = localId;
        cacheState();
    }

    // /////////////////////////////////////////////////////////
    // enString
    // /////////////////////////////////////////////////////////

    public static AggregatedOid deString(String oidStr, OidMarshaller oidMarshaller) {
        return oidMarshaller.unmarshal(oidStr, AggregatedOid.class);
    }

    @Override
    public String enString(OidMarshaller oidMarshaller) {
        return oidMarshaller.marshal(this);
    }

    @Override
    public String enStringNoVersion(OidMarshaller oidMarshaller) {
        return oidMarshaller.marshalNoVersion(this);
    }


    // ////////////////////////////////////////////
    // Encodeable
    // ////////////////////////////////////////////


    public AggregatedOid(final DataInputExtended input) throws IOException {
        this(deString(input.readUTF(), getEncodingMarshaller()));
    }

    private AggregatedOid(final AggregatedOid oid) throws IOException {
        super(oid.getParentOid());
        this.objectSpecId = oid.objectSpecId;
        this.localId = oid.localId;
        cacheState();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(enString(getEncodingMarshaller()));
    }

    /**
     * Cannot be a reference because Oid gets serialized by wicket viewer
     */
    private static OidMarshaller getEncodingMarshaller() {
        return new OidMarshaller();
    }


    
    // /////////////////////////////////////////////////////////
    // Properties
    // /////////////////////////////////////////////////////////

    @Override
    public ObjectSpecId getObjectSpecId() {
        return objectSpecId;
    }
    
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
        return Objects.equal(other.getParentOid(), getParentOid()) && 
               Objects.equal(other.objectSpecId, objectSpecId) && 
               Objects.equal(other.localId, localId);
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    private void cacheState() {
        int hashCode = 17;
        hashCode = 37 * hashCode + getParentOid().hashCode();
        hashCode = 37 * hashCode + objectSpecId.hashCode();
        hashCode = 37 * hashCode + localId.hashCode();
        cachedHashCode = hashCode;
    }

    
    // /////////////////////////////////////////////////////////
    // asPersistent
    // /////////////////////////////////////////////////////////

    /**
     * When the parent Oid is persisted, all its &quot;children&quot;
     * need updating similarly.
     */
    public AggregatedOid asPersistent(TypedOid newParentOid) {
        return new AggregatedOid(objectSpecId, newParentOid, localId);
    }





}
