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
package org.apache.causeway.core.metamodel.interactions.managed;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.Cardinality;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * In the event of serialization memoizes all the current pending parameter values
 * form the {@link ParameterNegotiationModel} this instance was originally created from.
 * <p>
 * On de-serialization those snapshot-ed parameter values can be fed back into a new
 * {@link ParameterNegotiationModel} instance.
 *
 * @apiNote Introduced because {@link ParameterNegotiationModel} is not serializable
 *      and we need a way to serialize pending parameter values. (see also [CAUSEWAY-3663])
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PendingParamsSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    protected static PendingParamsSnapshot create(final ParameterNegotiationModel parameterNegotiationModel) {
        return new PendingParamsSnapshot(null, parameterNegotiationModel);
    }

    private static PendingParamsSnapshot empty() {
        return new PendingParamsSnapshot(null, null);
    }

    // -- CONSTRUCTION

    private Can<ManagedObject> parameterValues;
    private ParameterNegotiationModel parameterNegotiationModel;

    public ParameterNegotiationModel restoreParameterNegotiationModel(final ManagedAction managedAction) {
        return this.parameterNegotiationModel = ParameterNegotiationModel.of(managedAction, parameterValues);
    }

    // -- UTILITY

    public static boolean canRestore(final @Nullable PendingParamsSnapshot pendingParamsSnapshot) {
        return pendingParamsSnapshot!=null
                && pendingParamsSnapshot.parameterValues!=null;
    }

    // -- SERIALIZATION PROXY

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * For each parameter, only set if it is a plural.
         * @implNote {@link LogicalType} is needed to recover a {@link PackedManagedObject}.
         */
        private final LogicalType[] cardinalityConstraints;
        private final Can<Can<Bookmark>> argBookmarks;
        private final boolean isEmpty;

        private SerializationProxy(final PendingParamsSnapshot pendingParamsSnapshot) {

            // guard against ParameterNegotiationModel not available
            if(pendingParamsSnapshot.parameterNegotiationModel==null) {
                this.cardinalityConstraints = null;
                this.argBookmarks = null;
                this.isEmpty = true;
                return;
            }

            var objectManager = MetaModelContext.instanceElseFail().getObjectManager();
            this.cardinalityConstraints = new LogicalType[pendingParamsSnapshot.parameterNegotiationModel.getParamCount()];
            this.argBookmarks = pendingParamsSnapshot.parameterNegotiationModel
                .getParamModels()
                .map(paramModel->bookmark(objectManager, paramModel));
            this.isEmpty = false;
        }

        private Object readResolve() {
            if(isEmpty) return PendingParamsSnapshot.empty();
            var objectManager = MetaModelContext.instanceElseFail().getObjectManager();
            return new PendingParamsSnapshot(
                    argBookmarks
                        .map(IndexedFunction.zeroBased((final int paramIndex, final Can<Bookmark> bookmarks)->
                            debookmark(objectManager, paramIndex, bookmarks))),
                    null);
        }

        /**
         * For given {@code paramModel} create a {@link Can} of {@link Bookmark}s,
         * either containing zero to many entries if it is a plural parameter,
         * otherwise is a singleton (having cardinality ONE, regardless of representing a null param value or not).
         */
        private Can<Bookmark> bookmark(
                final @NonNull ObjectManager objectManager,
                final @NonNull ManagedParameter paramModel) {
            var paramValue = paramModel.getValue().getValue();
            var isPlural = paramModel.getMetaModel().isPlural();
            { // memoize cardinalityConstraints
                _Assert.assertEquals(ManagedObjects.isPacked(paramValue), isPlural,
                        ()->String.format("Framework Bug: cardinality constraint mismatch on parameter %s",
                                paramModel.getMetaModel().getFeatureIdentifier()));
                if(isPlural) {
                    cardinalityConstraints[paramModel.getParamNr()] =
                            Objects.requireNonNull(((PackedManagedObject)paramValue).getLogicalType());
                }
            }
            return isPlural
                ? ManagedObjects.unpack(paramValue)
                        .map(objectManager::bookmark)
                : Can.of(objectManager.bookmark(paramValue));
        }

        /**
         * Recovers a {@link ManagedObject} from a {@link Can} of {@link Bookmark}s,
         * as previously created via {@link #bookmark(ObjectManager, ManagedParameter)}.
         * <p>
         * Automatically packs multiple values, in case the underlying parameter is a plural.
         */
        private ManagedObject debookmark(
                final @NonNull ObjectManager objectManager,
                final int paramIndex,
                final @NonNull Can<Bookmark> bookmarks) {
            var cardinalityConstraint = cardinalityConstraints[paramIndex];
            var isPlural = cardinalityConstraint!=null;
            { // sanity checks
                if(!isPlural) {
                    _Assert.assertEquals(Cardinality.ONE, bookmarks.getCardinality(),
                            ()->String.format("Framework Bug: cardinality constraint mismatch on parameter with index %d",
                                    paramIndex));
                }
            }
            var debookmarked = bookmarks
                    .map(objectManager::debookmark);
            return isPlural
                    ? ManagedObject.packed(
                            objectManager.getSpecificationLoader().specForLogicalTypeElseFail(cardinalityConstraint),
                            debookmarked)
                    : debookmarked.getSingletonOrFail();
        }

    }

}
