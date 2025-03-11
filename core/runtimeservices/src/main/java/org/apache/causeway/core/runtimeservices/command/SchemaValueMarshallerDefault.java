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
package org.apache.causeway.core.runtimeservices.command;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshallerAbstract;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.schema.common.v2.CollectionDto;
import org.apache.causeway.schema.common.v2.TypedTupleDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation of {@link SchemaValueMarshaller}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".SchemaValueMarshallerDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor
public class SchemaValueMarshallerDefault
extends SchemaValueMarshallerAbstract {

    @Inject @Getter(onMethod_ = {@Override}) private ValueSemanticsResolver valueSemanticsResolver;
    @Inject @Getter(onMethod_ = {@Override}) private MetaModelContext metaModelContext;

    // -- RECORD VALUES INTO DTO

    @Override
    protected <T> ValueWithTypeDto recordValue(
            final Context<T> context,
            final ValueWithTypeDto valueDto,
            final ManagedObject value) {

        valueDto.setType(context.schemaValueType());
        if (value.specialization().isEmpty()) {
            valueDto.setNull(true);
        }

        switch (context.schemaValueType()) {
        case COMPOSITE:
            valueDto.setComposite(toTypedTuple(context, _Casts.<T>uncheckedCast(value.getPojo())));
            return valueDto;
        case COLLECTION:
            recordValues(context, valueDto, ((PackedManagedObject)value).unpack());
            return valueDto;
        case REFERENCE:
            value.getBookmark()
                .map(Bookmark::toOidDto)
                .ifPresent(valueDto::setReference); // otherwise: null reference
            return valueDto;

        default:
            break;
        }

        var decomposedValueDto = context.semantics()
                .decompose(_Casts.uncheckedCast(value.getPojo()))
                .fundamental();

        // copy the decomposedValueDto into valueDto
        CommonDtoUtils.copy(decomposedValueDto, valueDto);

        return valueDto;
    }

    @Override
    protected <T> ValueWithTypeDto recordValues(
            final Context<T> context,
            final ValueWithTypeDto valueWithTypeDto,
            final Can<ManagedObject> values) {

        valueWithTypeDto.setType(ValueType.COLLECTION);
        valueWithTypeDto.setCollection(asCollectionDto(context, values));
        valueWithTypeDto.setNull(false);
        return valueWithTypeDto;
    }

    private <T> CollectionDto asCollectionDto(
            final Context<T> context,
            final Can<ManagedObject> values) {

        var elementValueType = context.schemaValueType();
        var collectionDto = new CollectionDto();
        collectionDto.setType(elementValueType);

        values.stream()
        .forEach(element->{
            var valueDto = new ValueWithTypeDto();
            valueDto.setType(elementValueType);
            collectionDto.getValue().add(valueDto);
            recordValue(context, valueDto, element);
        });

        return collectionDto;
    }

    private <T> TypedTupleDto toTypedTuple(final Context<T> context, final T valuePojo) {
        return valuePojo!=null
                ? context.semantics()
                        .decompose(valuePojo)
                        .composite()
                : null;
    }

    // -- RECOVER VALUES FROM DTO

    @Override
    protected ManagedObject recoverScalarValue(
            final @NonNull Context<?> context,
            final @NonNull ValueWithTypeDto valueDto) {

        var elementSpec = context.getElementType();

        var recoveredValueAsPojo = valueDto.getType()==ValueType.COMPOSITE
                ? fromTypedTuple(context, valueDto.getComposite())
                : context.semantics().compose(ValueDecomposition.ofFundamental(valueDto));

        var recoveredValue = ManagedObject.value(elementSpec, recoveredValueAsPojo);
        return recoveredValue;
    }

    private <T> T fromTypedTuple(final Context<T> context, final TypedTupleDto typedTupleDto) {
        if(typedTupleDto==null) return null;

        return context.semantics()
                .compose(ValueDecomposition.ofComposite(typedTupleDto));
    }

}
