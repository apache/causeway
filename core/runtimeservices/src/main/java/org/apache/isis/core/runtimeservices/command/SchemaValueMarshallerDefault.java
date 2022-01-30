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
package org.apache.isis.core.runtimeservices.command;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.services.schema.SchemaValueMarshallerAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.common.v2.CollectionDto;
import org.apache.isis.schema.common.v2.TypedTupleDto;
import org.apache.isis.schema.common.v2.ValueDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.runtimeservices.SchemaValueMarshallerDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor
public class SchemaValueMarshallerDefault
extends SchemaValueMarshallerAbstract {

    @Inject private ValueSemanticsResolver valueSemanticsResolver;
    @Inject private SpecificationLoader specLoader;

    // -- RECOVER VALUES FROM DTO

    @Override
    protected ManagedObject recoverScalarValue(
            @NonNull final Context<?> context,
            @NonNull final ValueWithTypeDto valueDto) {

        val valueAsObject = CommonDtoUtils.getValueAsObject(valueDto);

        if(valueAsObject==null) {
            return ManagedObject.empty(context.getElementType());
        }

        val elementSpec = context.getElementType();

        val recoveredValueAsPojo = valueDto.getComposite()!=null
                ? fromTypedTuple(context, valueDto.getComposite())
                : fromFundamentalValue(context, CommonDtoUtils.getValueAsObject(valueDto));

        val recoveredValue = ManagedObject.of(elementSpec, recoveredValueAsPojo);
        return recoveredValue;
    }

    // -- RECORD VALUES INTO DTO

    @Override
    protected <D extends ValueDto, T> D recordValue(
            final Context<T> context,
            final D valueDto,
            final ManagedObject value) {

        value.getBookmark()
        .ifPresentOrElse(
                bookmark->valueDto.setReference(bookmark.toOidDto()),
                ()->CommonDtoUtils.recordFundamentalValue(
                        context.getSchemaValueType(),
                        valueDto,
                        toFundamentalValue(context, (T)value.getPojo())));

        return valueDto;
    }

    @Override
    protected <D extends ValueWithTypeDto, T> D recordValues(
            final Context<T> context,
            final D valueWithTypeDto,
            final Can<ManagedObject> values) {

        valueWithTypeDto.setType(ValueType.COLLECTION);
        valueWithTypeDto.setCollection(asCollectionDto(context, values));
        valueWithTypeDto.setNull(false);
        return valueWithTypeDto;
    }

    // -- HELPER - RECORDING

    private <T> CollectionDto asCollectionDto(
            final Context<T> context,
            final Can<ManagedObject> values) {

        val elementValueType = context.getSchemaValueType();
        val collectionDto = new CollectionDto();
        collectionDto.setType(elementValueType);

        values.stream()
        .forEach(element->{
            val valueDto = new ValueDto();
            collectionDto.getValue().add(valueDto);
            recordValue(context, valueDto, element);
        });

        return collectionDto;
    }

    public <T> Object toFundamentalValue(final Context<T> context, final T valuePojo) {
        return context.getEncoderDecoder().isPresent()
                ? context.getEncoderDecoder().get().toEncodedString(valuePojo)
                : context.getConverter()
                    .<Object>map(converter->converter.toDelegateValue(valuePojo))
                    .orElse(valuePojo);
    }


    // -- HELPER - RECOVERY

    private <T> T fromTypedTuple(final Context<T> context, final TypedTupleDto typedTupleDto) {
        // FIXME[ISIS-2877] implement
        return null;
    }

    public <T> T fromFundamentalValue(final Context<T> context, final Object fundamentalValue) {
        val valuePojo = context.getEncoderDecoder().isPresent()
                ? context.getEncoderDecoder().get().fromEncodedString((String)fundamentalValue)
                : context.getConverter()
                    .<T>map(converter->converter.fromDelegateValue(_Casts.uncheckedCast(fundamentalValue)))
                    .orElse(_Casts.uncheckedCast(fundamentalValue));
        return valuePojo;
    }

    // -- DEPENDENCIES

    @Override
    protected final SpecificationLoader getSpecificationLoader() {
        return specLoader;
    }

    @Override
    protected ValueSemanticsResolver getValueSemanticsResolver() {
        return valueSemanticsResolver;
    }

}
