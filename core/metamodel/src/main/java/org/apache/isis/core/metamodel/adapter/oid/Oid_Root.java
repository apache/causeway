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

import java.util.Objects;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.internal.url.UrlDecoderUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.schema.common.v2.OidDto;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.val;

final class Oid_Root implements RootOid {

    private final static long serialVersionUID = 3L;

    private final ObjectSpecId objectSpecId;
    private final String identifier;
    private final int hashCode;

    public static Oid_Root of(
            ObjectSpecId objectSpecId, 
            String identifier) {

        return new Oid_Root(objectSpecId, identifier);
    }

    private Oid_Root(ObjectSpecId objectSpecId, String identifier) {

        requires(objectSpecId, "objectSpecId");
        requires(identifier, "identifier");

        this.objectSpecId = objectSpecId;
        this.identifier = identifier;
        this.hashCode = calculateHash();

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
    public Bookmark asBookmark() {
        val objectType = getObjectSpecId().asString();
        val identifier = getIdentifier();
        return new Bookmark(objectType, identifier);
    }

    @Override
    public OidDto asOidDto() {

        val oidDto = new OidDto();

        oidDto.setType(getObjectSpecId().asString());
        oidDto.setId(getIdentifier());

        return oidDto;
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
        return Objects.equals(objectSpecId, other.getObjectSpecId()) 
                && Objects.equals(identifier, other.getIdentifier());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return enString();
    }

    // -- HELPER

    private int calculateHash() {
        return Objects.hash(objectSpecId, identifier);
    }


}
