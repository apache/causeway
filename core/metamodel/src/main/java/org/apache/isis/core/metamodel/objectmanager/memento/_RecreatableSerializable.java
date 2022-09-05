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
package org.apache.isis.core.metamodel.objectmanager.memento;

import java.util.Objects;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

class _RecreatableSerializable implements _Recreatable{

    @Override
    public ManagedObject recreateObject(
            final ObjectMementoForScalar memento,
            final MetaModelContext mmc) {
        ObjectSpecification spec = mmc.getSpecificationLoader()
                .specForLogicalTypeElseFail(memento.logicalType);
        return mmc.getObjectManager().getObjectSerializer()
                .deserialize(spec, memento.serializedPayload);
    }

    @Override
    public boolean equals(
            final ObjectMementoForScalar memento,
            final ObjectMementoForScalar otherMemento) {
        return otherMemento.recreateStrategy == RecreateStrategy.SERIALIZABLE
                && Objects.equals(memento.logicalType, otherMemento.logicalType)
                && Objects.equals(memento.serializedPayload, otherMemento.serializedPayload);
    }

    @Override
    public int hashCode(final ObjectMementoForScalar memento) {
        // thats where we stored the hash of the originating pojo
        return memento.bookmark.getIdentifier().hashCode();
    }

}
