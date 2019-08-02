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

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.encoding.Encodable;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;


/**
 * An immutable identifier for either a root object (subtype {@link RootOid}) or
 * a parented collection (subtype {@link ParentedOid}).
 *
 * <p>
 * Note that value objects (strings, ints, {@link Value}s etc) do not have an {@link Oid}.
 */
public interface Oid extends Encodable {

    /**
     * A string representation of this {@link Oid}.
     */
    String enString();

    String enStringNoVersion();

    Version getVersion();

    void setVersion(Version version);

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

    // -- MARSHALLING

    public static interface Marshaller {

        String marshal(Version version);

        String marshalNoVersion(ParentedOid parentedOid);

        String marshal(ParentedOid parentedOid);

        String marshalNoVersion(RootOid rootOid);

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

        public static RootOid ofBookmark(final Bookmark bookmark) {
            return Oid_Root.of(ObjectSpecId.of(bookmark.getObjectType()), 
                    bookmark.getIdentifier(), Oid_State.from(bookmark), Version.empty());
        }

        public static RootOid viewmodelOf(ObjectSpecId objectSpecId, String mementoStr) {
            return Oid_Root.of(objectSpecId, mementoStr, Oid_State.VIEWMODEL, Version.empty());
        }

        public static RootOid transientOf(final ObjectSpecId objectSpecId, final String identifier) {
            return Oid_Root.of(objectSpecId, identifier, Oid_State.TRANSIENT, Version.empty());
        }

        public static RootOid persistentOf(final ObjectSpecId objectSpecId, final String identifier) {
            return Factory.persistentOf(objectSpecId, identifier, null);
        }

        public static RootOid persistentOf(final ObjectSpecId objectSpecId, final String identifier, final Long versionSequence) {
            return Factory.persistentOf(objectSpecId, identifier, versionSequence, null, null);
        }

        public static RootOid persistentOf(final ObjectSpecId objectSpecId, final String identifier, final Long versionSequence, final String versionUser) {
            return Factory.persistentOf(objectSpecId, identifier, versionSequence, versionUser, null);
        }

        public static RootOid persistentOf(final ObjectSpecId objectSpecId, final String identifier, final Long versionSequence, final Long versionUtcTimestamp) {
            return Factory.persistentOf(objectSpecId, identifier, versionSequence, null, versionUtcTimestamp);
        }

        public static RootOid persistentOf(final ObjectSpecId objectSpecId, final String identifier, final Long versionSequence, final String versionUser, final Long versionUtcTimestamp) {
            return Oid_Root.of(objectSpecId, identifier, Oid_State.PERSISTENT, 
                    Version.Factory.ifPresent(versionSequence, versionUser, versionUtcTimestamp));
        }

        // -- PARENTED COLLECTIONS

        public static ParentedOid parentedOfOneToMany(RootOid parentRootOid, OneToManyAssociation otma) {
            return Oid_Parented.ofName(parentRootOid, otma.getId());
        }

        public static ParentedOid parentedOfName(RootOid parentRootOid, String name) {
            return Oid_Parented.ofName(parentRootOid, name);
        }


    }

    Oid copy();


}
