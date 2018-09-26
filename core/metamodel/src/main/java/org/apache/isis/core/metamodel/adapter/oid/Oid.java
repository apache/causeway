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

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;


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
        return false;
    }

    public static enum State {
        PERSISTENT,
        TRANSIENT,
        VIEWMODEL;

        public boolean isTransient() {
            return this == TRANSIENT;
        }
        public boolean isViewModel() {
            return this == VIEWMODEL;
        }
        public boolean isPersistent() {
            return this == PERSISTENT;
        }

        public static State from(final Bookmark bookmark) {
            final Bookmark.ObjectState objectState = bookmark.getObjectState();
            return from(objectState);
        }

        public static State from(final Bookmark.ObjectState objectState) {
            switch (objectState) {
            case VIEW_MODEL:
                return VIEWMODEL;
            case TRANSIENT:
                return TRANSIENT;
            case PERSISTENT:
                return PERSISTENT;
            default:
                throw _Exceptions.unmatchedCase(objectState);
            }
        }
        public Bookmark.ObjectState asBookmarkObjectState() {
            switch (this) {
            case VIEWMODEL:
                return Bookmark.ObjectState.VIEW_MODEL;
            case TRANSIENT:
                return Bookmark.ObjectState.TRANSIENT;
            case PERSISTENT:
                return Bookmark.ObjectState.PERSISTENT;
            default:
                throw _Exceptions.unmatchedCase(this);
            }
        }
    }

    // -- FACTORIES
    
    /** for convenience*/
    public static final class Factory {
        
        public static RootOid ofBookmark(final Bookmark bookmark) {
            return RootOid.of(ObjectSpecId.of(bookmark.getObjectType()), 
                    bookmark.getIdentifier(), State.from(bookmark), Version.empty());
        }

        public static RootOid viewmodelOf(ObjectSpecId objectSpecId, String mementoStr) {
            return RootOid.of(objectSpecId, mementoStr, State.VIEWMODEL, Version.empty());
        }
        
        public static RootOid transientOf(final ObjectSpecId objectSpecId, final String identifier) {
            return RootOid.of(objectSpecId, identifier, State.TRANSIENT, Version.empty());
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
            return RootOid.of(objectSpecId, identifier, State.PERSISTENT, 
                    Version.Factory.ifPresent(versionSequence, versionUser, versionUtcTimestamp));
        }

        // -- PARENTED COLLECTIONS
        
        public static ParentedOid collectionOfOneToMany(RootOid parentRootOid, OneToManyAssociation otma) {
            return ParentedOid.ofName(parentRootOid, otma.getId());
        }

        public static ParentedOid collectionOfName(RootOid parentRootOid, String name) {
            return ParentedOid.ofName(parentRootOid, name);
        }

        
    }


}
