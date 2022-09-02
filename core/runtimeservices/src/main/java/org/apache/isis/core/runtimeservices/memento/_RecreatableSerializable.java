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
package org.apache.isis.core.runtimeservices.memento;

import java.util.Arrays;
import java.util.Objects;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

class _RecreatableSerializable implements _Recreatable{

    @Override
    public ManagedObject recreateObject(
            final _ObjectMemento memento,
            final MetaModelContext mmc) {
        ObjectSpecification spec = mmc.getSpecificationLoader()
                .specForLogicalTypeElseFail(memento.logicalType);
        return mmc.getObjectManager().getObjectSerializer()
                .deserialize(spec, memento.serializedObject);
    }

    @Override
    public boolean equals(
            final _ObjectMemento memento,
            final _ObjectMemento otherMemento) {
        return otherMemento.recreateStrategy == RecreateStrategy.SERIALIZABLE
                && Objects.equals(memento.logicalType, otherMemento.logicalType)
                && Objects.equals(memento.serializedObject, otherMemento.serializedObject);
    }

    @Override
    public int hashCode(final _ObjectMemento memento) {
        return Arrays.hashCode(memento.serializedObject); // potentially expensive, unfortunately cannot be cached in enum
    }

    @Override
    public Bookmark asPseudoBookmark(final _ObjectMemento memento) {
        return Bookmark.forLogicalTypeNameAndIdentifier(
                memento.getLogicalTypeName(),
                "SERIALIZABLE");
    }

    @Override
    public void resetVersion(
            final _ObjectMemento memento,
            final MetaModelContext mmc) {
        // nope
    }
}
