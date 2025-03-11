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
package org.apache.causeway.core.metamodel.object;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * 'Collection' of {@link ManagedObject}s.
 * @see ManagedObject.Specialization#PACKED
 */
public record PackedManagedObject(
    /** element spec */
    @NonNull ObjectSpecification objSpec,
    @NonNull Can<ManagedObject> nonScalar)
implements
    ManagedObject, Bookmarkable.NoBookmark {

    public PackedManagedObject(
            final ObjectSpecification objSpec,
            final @Nullable Can<ManagedObject> nonScalar) {
        this.objSpec = objSpec;
        this.nonScalar = nonScalar!=null
                ? nonScalar
                : Can.empty();
        _Assert.assertTrue(objSpec().isSingular(), "a PackedManagedObject cannot containt non-scalars");
    }

    @Override
    public String getTitle() {
        return nonScalar.stream()
                    .map(ManagedObject::getTitle)
                    .collect(Collectors.joining(","));
    }

    @Override
    public Object getPojo() {
        // this algorithm preserves null pojos ...
        return nonScalar.stream()
                .map(ManagedObject::getPojo)
                .toList();
    }

    @Override
    public Optional<ObjectMemento> getMemento() {
        var listOfMementos = nonScalar.stream()
            .map(scalar->scalar.getMementoElseFail())
            .collect(Collectors.toCollection(ArrayList::new)); // ArrayList is serializable
        var memento = ObjectMemento.packed(
            logicalType(),
            listOfMementos);
        return Optional.of(memento);
    }

    public Can<ManagedObject> unpack(){
        return nonScalar;
    }

    @Override
    public Specialization specialization() {
        return Specialization.PACKED;
    }

}
