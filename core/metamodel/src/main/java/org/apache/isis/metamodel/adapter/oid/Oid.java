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

import java.io.Serializable;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;


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

    /**
     * Flags whether this OID is for a transient (not-yet-persisted) object,
     * or a view model object, or for a persistent object.
     *
     * <p>
     * In the case of an {@link ParentedOid}, is determined by the state
     * of its {@link ParentedOid#getParentOid() root}'s {@link RootOid#isTransient() state}.
     */
    boolean isTransient();

    boolean isViewModel();

    boolean isPersistent();

    default boolean isValue() {
        return false; // default, only overridden by Oid_Value
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
                    bookmark.getIdentifier(), 
                    bookmark.getObjectState());
        }

        public static RootOid viewmodelOf(ObjectSpecId objectSpecId, String mementoStr) {
            return Oid_Root.of(objectSpecId, mementoStr, Bookmark.ObjectState.VIEW_MODEL);
        }

        public static RootOid transientOf(ObjectSpecId objectSpecId, String identifier) {
            return Oid_Root.of(objectSpecId, identifier, Bookmark.ObjectState.TRANSIENT);
        }

        public static RootOid persistentOf(ObjectSpecId objectSpecId, String identifier) {
            return Oid_Root.of(objectSpecId, identifier, Bookmark.ObjectState.PERSISTENT);
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
