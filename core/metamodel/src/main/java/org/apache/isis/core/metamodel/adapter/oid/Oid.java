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

import java.io.Serializable;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.schema.common.v2.OidDto;

/**
 * An immutable identifier for a root object (subtype {@link RootOid}).
 *
 * <p>
 * @apiNote value objects (strings, ints, {@link Value}s etc) do not have a 
 * semantically meaningful {@link Oid}, but as an implementation detail 
 * might have a placeholder {@link Oid}. 
 */
public interface Oid extends Serializable {

    /**
     * A string representation of this {@link Oid}.
     */
    String enString();

    default boolean isValue() {
        return false; // default, only overridden by Oid_Value
    }
    
    /**
     * The logical-type-name of the domain object this instance is representing.
     * When representing a value returns {@code null}.
     */
    String getLogicalTypeName();

    // -- MARSHALLING

    public static interface Marshaller {
        String marshal(RootOid rootOid);
        String joinAsOid(String logicalTypeName, String instanceId);
    }

    public static Marshaller marshaller() {
        return Oid_Marshaller.INSTANCE;
    }

    // -- UN-MARSHALLING

    public static interface Unmarshaller {
        <T extends Oid> T unmarshal(String oidStr, Class<T> requestedType);
        String splitInstanceId(String oidStr);
    }

    public static Unmarshaller unmarshaller() {
        return Oid_Marshaller.INSTANCE;
    }

    // -- FACTORIES

    /** for convenience*/
    public static final class Factory {

        public static RootOid value() {
            return Oid_Value.INSTANCE;
        }

        public static RootOid ofBookmark(final Bookmark bookmark) {
            return Oid_Root.of(
                    bookmark.getLogicalTypeName(), 
                    bookmark.getIdentifier());
        }
        
        public static RootOid ofDto(final OidDto oid) {
            return Oid_Root.of(
                    oid.getType(), 
                    oid.getId());
        }

        public static RootOid root(final LogicalType logicalType, final String identifier) {
            return Oid_Root.of(
                    logicalType.getLogicalTypeName(), 
                    identifier);
        }
        
        @Deprecated
        public static RootOid root(final String logicalTypeName, final String identifier) {
            return Oid_Root.of(
                    logicalTypeName, 
                    identifier);
        }
        
    }

}
