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
import java.util.Objects;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

/**
 * Used as the {@link Oid} for collections.
 */
public final class ParentedCollectionOid implements Serializable, Oid {

    private static final long serialVersionUID = 1L;

    private final static OidMarshaller OID_MARSHALLER = OidMarshaller.INSTANCE;

    private final String name;
    private final int hashCode;

    private final RootOid parentOid;

    // /////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////

    public ParentedCollectionOid(RootOid rootOid, OneToManyAssociation otma) {
        this(rootOid, otma.getId());
    }

    public ParentedCollectionOid(RootOid rootOid, String name) {
        Assert.assertNotNull("rootOid required", rootOid);
        this.parentOid = rootOid;
        this.name = name;
        this.hashCode = calculateHash();
    }


    public RootOid getRootOid() {
        return parentOid;
    }

    @Override
    public Version getVersion() {
        return parentOid.getVersion();
    }

    @Override
    public void setVersion(Version version) {
        parentOid.setVersion(version);
    }

    @Override
    public boolean isTransient() {
        return getRootOid().isTransient();
    }

    @Override
    public boolean isViewModel() {
        return getRootOid().isViewModel();
    }

    @Override
    public boolean isPersistent() {
        return getRootOid().isPersistent();
    }


    // /////////////////////////////////////////////////////////
    // enstring
    // /////////////////////////////////////////////////////////

    public static ParentedCollectionOid deString(String oidStr) {
        return OID_MARSHALLER.unmarshal(oidStr, ParentedCollectionOid.class);
    }


    @Override
    public String enString() {
        return OID_MARSHALLER.marshal(this);
    }

    @Override
    public String enStringNoVersion() {
        return OID_MARSHALLER.marshalNoVersion(this);
    }


    // /////////////////////////////////////////////////////////
    // encodeable
    // /////////////////////////////////////////////////////////


    public ParentedCollectionOid(DataInputExtended inputStream) throws IOException {
        this(ParentedCollectionOid.deString(inputStream.readUTF()));
    }

    private ParentedCollectionOid(ParentedCollectionOid oid) throws IOException {
        this.parentOid = oid.getRootOid();
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
        return equals((ParentedCollectionOid) other);
    }

    public boolean equals(final ParentedCollectionOid other) {
        return Objects.equals(other.getRootOid(), getRootOid()) && Objects.equals(other.name, name);
    }


    @Override
    public int hashCode() {
        return hashCode;
    }

    private int calculateHash() {
        return Objects.hash(getRootOid(), name);
    }


    // /////////////////////////////////////////////////////////
    // asPersistent
    // /////////////////////////////////////////////////////////

    /**
     * When the RootOid is persisted, all its &quot;children&quot;
     * need updating similarly.
     */
    public ParentedCollectionOid asPersistent(RootOid newParentRootOid) {
        return new ParentedCollectionOid(newParentRootOid, name);
    }



}
