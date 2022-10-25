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
package org.apache.causeway.core.metamodel.facets.object.choices.enums;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;
import lombok.val;

public class ChoicesFacetFromEnum
extends ChoicesFacetAbstract {

    private final @NonNull Can<ManagedObject> choices;

    public ChoicesFacetFromEnum(final FacetHolder holder, final Class<?> enumClass) {
        super(holder);

        final Object[] choices = enumClass.getEnumConstants();

        val elementSpec = specForTypeElseFail(enumClass);
        this.choices = Can.ofArray(choices)
                .map(choice->ManagedObject.value(elementSpec, choice));
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ManagedObject adapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return choices;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("choices",
                choices.stream()
                .map(ManagedObject::getPojo)
                .map(Enum.class::cast)
                .map(Enum::name)
                .collect(Collectors.joining(", ")));

    }
}
