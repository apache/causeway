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
import java.util.Objects;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import static org.apache.isis.commons.internal.base._With.requires;
import static org.apache.isis.core.metamodel.adapter.oid.Oid.marshaller;
import static org.apache.isis.core.metamodel.adapter.oid.Oid.unmarshaller;

final class Oid_Parented implements ParentedOid {

    private static final long serialVersionUID = 2L;

    private final String oneToManyId;
    private final int hashCode;

    private final RootOid parentRootOid;

    static Oid_Parented ofOneToManyId(RootOid parentRootOid, String oneToManyId) {
        return new Oid_Parented(parentRootOid, oneToManyId);
    }

    private Oid_Parented(RootOid parentRootOid, String oneToManyId) {
        requires(parentRootOid, "parentRootOid");
        this.parentRootOid = parentRootOid;
        this.oneToManyId = oneToManyId;
        this.hashCode = calculateHash();
    }

    @Override
    public RootOid getParentOid() {
        return parentRootOid;
    }
    
    @Override
    public ObjectSpecId getObjectSpecId() {
        return getParentOid().getObjectSpecId();
    }

    public static Oid_Parented deString(String oidStr) {
        return unmarshaller().unmarshal(oidStr, Oid_Parented.class);
    }

    @Override
    public String enString() {
        return marshaller().marshal(this);
    }

    private Oid_Parented(Oid_Parented oid) throws IOException {
        this.parentRootOid = oid.getParentOid();
        this.oneToManyId = oid.oneToManyId;
        this.hashCode = calculateHash();
    }

    @Override
    public String getName() {
        return oneToManyId;
    }


    // /////////////////////////////////////////////////////////
    // toString
    // /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return enString();
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
        return equals((Oid_Parented) other);
    }

    public boolean equals(final Oid_Parented other) {
        return Objects.equals(other.getParentOid(), getParentOid()) 
                && Objects.equals(other.oneToManyId, oneToManyId);
    }


    @Override
    public int hashCode() {
        return hashCode;
    }

    private int calculateHash() {
        return Objects.hash(getParentOid(), oneToManyId);
    }

    /**
     * When the RootOid is persisted, all its &quot;children&quot;
     * need updating similarly.
     */
    public Oid_Parented asPersistent(RootOid newParentRootOid) {
        return new Oid_Parented(newParentRootOid, oneToManyId);
    }


}
