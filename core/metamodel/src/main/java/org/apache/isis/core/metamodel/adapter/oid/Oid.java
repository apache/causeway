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
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;


/**
 * An immutable identifier for either a root object (subtype {@link RootOid}) or
 * a parented collection (subtype {@link ParentedOid}).
 *
 * <p>
 * Note that value objects (strings, ints, {@link Value}s etc) do not have an {@link Oid}.
 */
public interface Oid extends Serializable {

    /**
     * A string representation of this {@link Oid}.
     */
    String enString();

    default boolean isValue() {
        return false; // default, only overridden by Oid_Value
    }
    
    @Deprecated
    default boolean isTransient() {
        return false;
    }

    
    /**
     * {@link ObjectSpecId} of the domain object this instance is representing, or when parented,
     * the ObjectSpecId of the parent domain object. When representing a value returns {@code null}.   
     */
    ObjectSpecId getObjectSpecId();

    // -- MARSHALLING

    public static interface Marshaller {

        String marshal(ParentedOid parentedOid);

        String marshal(RootOid rootOid);

        String joinAsOid(String domainType, String instanceId);

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

        // -- LEGACY

        public static RootOid ofBookmark(Bookmark bookmark) {
            return Oid_Root.of(
                    ObjectSpecId.of(bookmark.getObjectType()), 
                    bookmark.getIdentifier());
        }

        public static RootOid of(ObjectSpecId objectSpecId, String mementoStr) {
            return Oid_Root.of(objectSpecId, mementoStr);
        }
        
        @Deprecated
        public static RootOid persistentOf(ObjectSpecId objectSpecId, String mementoStr) {
            return of(objectSpecId, mementoStr);
        }

        @Deprecated
        public static RootOid transientOf(ObjectSpecId objectSpecId, String identifier) {
            return of(objectSpecId, identifier);
        }
        
        // -- PARENTED COLLECTIONS

        public static ParentedOid parentedOfOneToMany(RootOid parentRootOid, OneToManyAssociation oneToMany) {
            return Oid_Parented.ofOneToManyId(parentRootOid, oneToMany.getId());
        }

        public static ParentedOid parentedOfOneToManyId(RootOid parentRootOid, String oneToManyId) {
            return Oid_Parented.ofOneToManyId(parentRootOid, oneToManyId);
        }
        
    }

}
