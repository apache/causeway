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

import static org.apache.isis.commons.internal.base._With.requires;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.adapter.version.Version;

/**
 * Used as the {@link Oid} for collections.
 */
public final class ParentedOid implements Serializable, Oid {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final int hashCode;

    private final RootOid parentRootOid;

    // package private to support testing
    static ParentedOid ofName(RootOid parentRootOid, String name) {
        return new ParentedOid(parentRootOid, name);
    }
    
    private ParentedOid(RootOid parentRootOid, String name) {
        requires(parentRootOid, "parentRootOid");
        this.parentRootOid = parentRootOid;
        this.name = name;
        this.hashCode = calculateHash();
    }

    public RootOid getParentOid() {
        return parentRootOid;
    }

    @Override
    public Version getVersion() {
        return parentRootOid.getVersion();
    }

    @Override
    public void setVersion(Version version) {
        parentRootOid.setVersion(version);
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

    public static ParentedOid deString(String oidStr) {
        return Oid.unmarshaller().unmarshal(oidStr, ParentedOid.class);
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


    public ParentedOid(DataInputExtended inputStream) throws IOException {
        this(ParentedOid.deString(inputStream.readUTF()));
    }

    private ParentedOid(ParentedOid oid) throws IOException {
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
        return equals((ParentedOid) other);
    }

    public boolean equals(final ParentedOid other) {
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
    public ParentedOid asPersistent(RootOid newParentRootOid) {
        return new ParentedOid(newParentRootOid, name);
    }



}
