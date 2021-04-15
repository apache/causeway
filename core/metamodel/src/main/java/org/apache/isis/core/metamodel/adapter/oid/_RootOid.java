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
import org.apache.isis.commons.internal.codec._UrlDecoderUtil;

import lombok.Getter;
import lombok.NonNull;

final class _RootOid implements Oid {

    private static final long serialVersionUID = 3L;

    @Getter(onMethod_ = {@Override}) private final String logicalTypeName;
    @Getter(onMethod_ = {@Override}) private final String identifier;
    
    private final int hashCode;

    public static _RootOid of(
            final @NonNull String logicalTypeName, 
            final @NonNull String identifier) {
        return new _RootOid(logicalTypeName, identifier);
    }

    private _RootOid(
            final String logicalTypeName, 
            final String identifier) {

        this.logicalTypeName = logicalTypeName;
        this.identifier = identifier;
        this.hashCode = calculateHash();

    }

    // -- ENCODING 
    
    public static _RootOid deStringEncoded(final String urlEncodedOidStr) {
        final String oidStr = _UrlDecoderUtil.urlDecode(urlEncodedOidStr);
        return deString(oidStr);
    }

    public static _RootOid deString(final String oidStr) {
        return Oid.unmarshaller().unmarshal(oidStr, _RootOid.class);
    }

    @Override
    public String enString() {
        return Oid.marshaller().marshal(this);
    }

    @Override
    public Bookmark asBookmark() {
        return Bookmark.of(logicalTypeName, getIdentifier());
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
        return equals((_RootOid) other);
    }

    public boolean equals(final _RootOid other) {
        return Objects.equals(logicalTypeName, other.getLogicalTypeName()) 
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
        return Objects.hash(logicalTypeName, identifier);
    }

}
