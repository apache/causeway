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
package org.apache.causeway.viewer.wicket.model.models.interaction.act;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * In the event of page serialization memoizes all the current pending parameter values
 * form the {@link ParameterNegotiationModel} this instance was originally created from.
 * <p>
 * On de-serialization those snapshot-ed parameter values can be fed back into a new
 * {@link ParameterNegotiationModel} instance.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class PendingParamsSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    public static PendingParamsSnapshot create(final ParameterNegotiationModel parameterNegotiationModel) {
        return new PendingParamsSnapshot(null, parameterNegotiationModel);
    }

    private Can<ManagedObject> parameterValues;
    private ParameterNegotiationModel parameterNegotiationModel;

    public ParameterNegotiationModel restoreParameterNegotiationModel(final ManagedAction managedAction) {
        return this.parameterNegotiationModel = ParameterNegotiationModel.of(managedAction, parameterValues);
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
        private final Can<Bookmark> argBookmarks;

        //TODO[CAUSEWAY-3663] also handle PackedManagedObject
        private SerializationProxy(final PendingParamsSnapshot pvm) {
            this.argBookmarks = pvm.parameterNegotiationModel.getParamValues()
                    .map(managedObj->ManagedObjects.bookmark(managedObj)
                            .orElseGet(()->Bookmark.empty(managedObj.getLogicalType())));
        }

        //TODO[CAUSEWAY-3663] also handle PackedManagedObject
        private Object readResolve() {
            var objectManager = MetaModelContext.instanceElseFail().getObjectManager();
            return new PendingParamsSnapshot(
                    argBookmarks.map(bookmark->
                        bookmark.isEmpty()
                        ? ManagedObject.empty(
                                objectManager.getSpecificationLoader().specForBookmarkElseFail(bookmark))
                        : objectManager
                            .loadObject(bookmark)
                            .orElseThrow()),
                    null);
        }

    }

}
