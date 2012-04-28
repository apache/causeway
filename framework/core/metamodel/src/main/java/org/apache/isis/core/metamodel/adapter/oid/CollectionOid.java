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

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

/**
 * Used as the {@link Oid} for collections.
 */
public final class CollectionOid extends ParentedOid implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private int cachedHashCode;

    // /////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////

    public CollectionOid(Oid parentOid, OneToManyAssociation otma) {
        this(parentOid, otma.getId());
        cacheState();
    }

    public CollectionOid(Oid parentOid, String name) {
        super(parentOid);
        this.name = name;
        cacheState();
    }
    
    // ////////////////////////////////////////////
    // Encodeable
    // ////////////////////////////////////////////

    public CollectionOid(final DataInputExtended input) throws IOException {
        super(input);
        this.name = input.readUTF();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeUTF(this.name);
    }


    
    // /////////////////////////////////////////////////////////
    // Properties
    // /////////////////////////////////////////////////////////

    public String getName() {
        return name;
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
        return equals((CollectionOid) other);
    }

    public boolean equals(final CollectionOid other) {
        return Objects.equal(other.getParentOid(), getParentOid()) && Objects.equal(other.name, name);
    }

    
    @Override
    public int hashCode() {
        cacheState();
        return cachedHashCode;
    }

    private void cacheState() {
        int hashCode = 17;
        hashCode = 37 * hashCode + getParentOid().hashCode();
        hashCode = 37 * hashCode + name.hashCode();
        cachedHashCode = hashCode;
    }


    
    // /////////////////////////////////////////////////////////
    // asPersistent
    // /////////////////////////////////////////////////////////

    /**
     * When the RootOid is persisted, all its &quot;children&quot;
     * need updating similarly.
     */
    public CollectionOid asPersistent(Oid newParentRootOid) {
        return new CollectionOid(newParentRootOid, name);
    }

    // /////////////////////////////////////////////////////////
    // enString
    // /////////////////////////////////////////////////////////

    @Override
    public String enString() {
        return getParentOid().enString() + "@" + name;
    }

    // /////////////////////////////////////////////////////////
    // toString
    // /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "COID[" + getParentOid() + "," + name + "]";
    }

}
