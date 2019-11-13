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

package org.apache.isis.metamodel.adapter.oid;

import java.io.IOException;
import java.util.Objects;

import org.apache.isis.commons.internal.encoding.DataInputExtended;
import org.apache.isis.commons.internal.encoding.DataOutputExtended;

import static org.apache.isis.commons.internal.base._With.requires;

final class Oid_Parented implements ParentedOid {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final int hashCode;

    private final RootOid parentRootOid;

    // package private to support testing
    static Oid_Parented ofName(RootOid parentRootOid, String name) {
        return new Oid_Parented(parentRootOid, name);
    }

    private Oid_Parented(RootOid parentRootOid, String name) {
        requires(parentRootOid, "parentRootOid");
        this.parentRootOid = parentRootOid;
        this.name = name;
        this.hashCode = calculateHash();
    }

    @Override
    public RootOid getParentOid() {
        return parentRootOid;
    }

    @Override
    public boolean isTransient() {
        return getParentOid().isTransient();
    }

    @Override
    public boolean isViewModel() {
        return getParentOid().isViewModel();
    }

    @Override
    public boolean isPersistent() {
        return getParentOid().isPersistent();
    }


    // /////////////////////////////////////////////////////////
    // enstring
    // /////////////////////////////////////////////////////////

    public static Oid_Parented deString(String oidStr) {
        return Oid.unmarshaller().unmarshal(oidStr, Oid_Parented.class);
    }


    @Override
    public String enString() {
        return Oid.marshaller().marshal(this);
    }

    @Override
    public String enStringNoVersion() {
        return Oid.marshaller().marshalNoVersion(this);
    }


    // /////////////////////////////////////////////////////////
    // encodeable
    // /////////////////////////////////////////////////////////


    public Oid_Parented(DataInputExtended inputStream) throws IOException {
        this(Oid_Parented.deString(inputStream.readUTF()));
    }

    private Oid_Parented(Oid_Parented oid) throws IOException {
        this.parentRootOid = oid.getParentOid();
        this.name = oid.name;
        this.hashCode = calculateHash();
    }


    @Override
    public void encode(DataOutputExtended outputStream) throws IOException {
        outputStream.writeUTF(enString());
    }

    // /////////////////////////////////////////////////////////
    // Properties
    // /////////////////////////////////////////////////////////

    @Override
    public String getName() {
        return name;
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
        return Objects.equals(other.getParentOid(), getParentOid()) && Objects.equals(other.name, name);
    }


    @Override
    public int hashCode() {
        return hashCode;
    }

    private int calculateHash() {
        return Objects.hash(getParentOid(), name);
    }


    // /////////////////////////////////////////////////////////
    // asPersistent
    // /////////////////////////////////////////////////////////

    /**
     * When the RootOid is persisted, all its &quot;children&quot;
     * need updating similarly.
     */
    public Oid_Parented asPersistent(RootOid newParentRootOid) {
        return new Oid_Parented(newParentRootOid, name);
    }

    @Override
    public Oid copy() {
        return ofName((RootOid) parentRootOid.copy(), name);
    }



}
