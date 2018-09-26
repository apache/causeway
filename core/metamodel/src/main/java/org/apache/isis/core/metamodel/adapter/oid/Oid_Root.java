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
import java.util.Objects;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.url.UrlDecoderUtil;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.schema.common.v1.OidDto;

final class Oid_Root implements RootOid {

    // -- fields
    private final static long serialVersionUID = 1L;

    private final ObjectSpecId objectSpecId;
    private final String identifier;
    private final Oid_State state;
    private final int hashCode;

    // not part of equality check
    private Version version;


    public static Oid_Root of(final ObjectSpecId objectSpecId, final String identifier, final Oid_State state) {
        return of(objectSpecId, identifier, state, Version.empty());
    }
    
    public static Oid_Root of(final ObjectSpecId objectSpecId, final String identifier, final Oid_State state, final Version version) {
        return new Oid_Root(objectSpecId, identifier, state, version);
    }
    
    private Oid_Root(final ObjectSpecId objectSpecId, final String identifier, final Oid_State state, final Version version) {

        requires(objectSpecId, "objectSpecId");
        requires(identifier, "identifier");
        requires(state, "state");

        // too slow...
        // Ensure.ensureThatArg(identifier, is(not(IsisMatchers.contains("#"))), "identifier '" + identifier + "' contains a '#' symbol");
        // Ensure.ensureThatArg(identifier, is(not(IsisMatchers.contains("@"))), "identifier '" + identifier + "' contains an '@' symbol");

        this.objectSpecId = objectSpecId;
        this.identifier = identifier;
        this.state = state;
        this.version = version;
        this.hashCode = calculateHash();
    }

    // -- Encodeable
    public Oid_Root(final DataInputExtended input) throws IOException {
        final String oidStr = input.readUTF();
        final Oid_Root oid = Oid.unmarshaller().unmarshal(oidStr, Oid_Root.class);
        this.objectSpecId = oid.objectSpecId;
        this.identifier = oid.identifier;
        this.state = oid.state;
        
        requires(objectSpecId, "objectSpecId");
        requires(identifier, "identifier");
        requires(state, "state");
        
        this.version = oid.version;
        this.hashCode = calculateHash();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(enString());
    }

    // -- deString'able, enString
    public static Oid_Root deStringEncoded(final String urlEncodedOidStr) {
        final String oidStr = UrlDecoderUtil.urlDecode(urlEncodedOidStr);
        return deString(oidStr);
    }

    public static Oid_Root deString(final String oidStr) {
        return Oid.unmarshaller().unmarshal(oidStr, Oid_Root.class);
    }

    @Override
    public String enString() {
        return Oid.marshaller().marshal(this);
    }

    @Override
    public String enStringNoVersion() {
        return Oid.marshaller().marshalNoVersion(this);
    }


    // -- Properties
    @Override
    public ObjectSpecId getObjectSpecId() {
        return objectSpecId;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isTransient() {
        return state.isTransient();
    }

    @Override
    public boolean isViewModel() {
        return state.isViewModel();
    }

    @Override
    public boolean isPersistent() {
        return state.isPersistent();
    }

    // -- Version

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public void setVersion(final Version version) {
        this.version = version;
    }

    @Override
    public Bookmark asBookmark() {
        return state.bookmarkOf(this);
    }
    
    @Override
    public OidDto asOidDto() {
        return state.toOidDto(this);
    }
    
    // -- equals, hashCode

    private int calculateHash() {
        return Objects.hash(objectSpecId, identifier, state);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return equals((Oid_Root) other);
    }

    public boolean equals(final Oid_Root other) {
        return Objects.equals(objectSpecId, other.getObjectSpecId()) && 
                Objects.equals(identifier, other.getIdentifier()) && 
                Objects.equals(state, other.state);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    // -- toString
    @Override
    public String toString() {
        return enString();
    }

    @Override
    public Oid copy() {
        // these are all immutable ...
        return of(objectSpecId, identifier, state, version);
    }

}
