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
package org.apache.causeway.core.metamodel.services.schema;

import java.util.ArrayList;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.Identifier.Type;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.Converter;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.Cardinality;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ProtoObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.CollectionDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.ValueDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

import lombok.SneakyThrows;

public abstract class SchemaValueMarshallerAbstract
implements SchemaValueMarshaller, HasMetaModelContext {

    public record Context<T>(
            @NonNull Class<T> correspondingClass,
            @NonNull ObjectFeature feature,
            @NonNull ValueType schemaValueType,
            @Nullable ValueSemanticsProvider<T> semantics,
            @NonNull Optional<Converter<T, ?>> converter) {

        public static <T> Context<T> forNonValue(
                final Class<T> correspondingClass,
                final ObjectFeature feature) {
            return new Context<T>(correspondingClass, feature,
                    feature.getFeatureType().isCollection()
                        ? ValueType.COLLECTION
                        : ValueType.REFERENCE,
                    /*semantics*/null, Optional.empty());
        }

        public static <T> Context<T> forValue(
                final Class<T> correspondingClass,
                final ObjectFeature feature,
                final @NonNull ValueSemanticsProvider<T> semantics) {

            return new Context<>(correspondingClass, feature,
                    semantics.getSchemaValueType(),
                    semantics,
                    Optional.ofNullable(semantics.getConverter()));
        }

        public ObjectSpecification getElementType() {
            return feature.getElementType();
        }

    }

    // -- RECORD DTOS

    @Override
    public final ActionInvocationDto recordActionResultScalar(
            final @NonNull ActionInvocationDto invocationDto,
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject value) {

        var feature = objectAction;
        var elementTypeAsClass = feature.getElementType().getCorrespondingClass();
        var context = newContext(elementTypeAsClass, feature);
        invocationDto.setReturned(
                recordValue(context, new ValueWithTypeDto(), value));
        return invocationDto;
    }

    @Override
    public final ActionInvocationDto recordActionResultNonScalar(
            final @NonNull ActionInvocationDto invocationDto,
            final @NonNull ObjectAction objectAction,
            final @NonNull Can<ManagedObject> value) {

        var feature = objectAction;
        var elementTypeAsClass = feature.getElementType().getCorrespondingClass();
        var context = newContext(elementTypeAsClass, feature);
        invocationDto.setReturned(
                recordValues(context, new ValueWithTypeDto(), value));
        return invocationDto;
    }

    @Override
    public final PropertyDto recordPropertyValue(
            final @NonNull PropertyDto propertyDto,
            final @NonNull OneToOneAssociation property,
            final @NonNull ManagedObject value) {

        var feature = property;
        var elementTypeAsClass = feature.getElementType().getCorrespondingClass();

        // guard against property not being a scalar
        _Assert.assertEquals(elementTypeAsClass, property.getElementType().getCorrespondingClass());

        var context = newContext(elementTypeAsClass, feature);
        propertyDto.setNewValue(
                recordValue(context, new ValueWithTypeDto(), value));
        return propertyDto;
    }

    @Override
    public final ParamDto recordParamScalar(
            final @NonNull ParamDto paramDto,
            final @NonNull ObjectActionParameter actionParameter,
            final @NonNull ManagedObject value) {

        _Assert.assertTrue(actionParameter.getFeatureType() == FeatureType.ACTION_PARAMETER_SINGULAR);

        var feature = actionParameter;
        var elementTypeAsClass = feature.getElementType().getCorrespondingClass();
        var context = newContext(elementTypeAsClass, feature);

        //          ValueType valueType = valueWrapper.getValueType();
        //
        //          // this hack preserves previous behavior before we were able to serialize blobs and clobs into XML
        //          // however, we also don't want this new behavior for parameter arguments
        //          // (else these large objects could end up being persisted).
        //          if(valueType == ValueType.BLOB) valueType = ValueType.REFERENCE;
        //          if(valueType == ValueType.CLOB) valueType = ValueType.REFERENCE;
        recordValue(context, paramDto, value);
        return paramDto;
    }

    @Override
    public ParamDto recordParamNonScalar(
            final @NonNull ParamDto paramDto,
            final @NonNull ObjectActionParameter actionParameter,
            final @NonNull Can<ManagedObject> values) {

        _Assert.assertTrue(actionParameter.getFeatureType() == FeatureType.ACTION_PARAMETER_PLURAL);

        var feature = actionParameter;
        var valueCls = feature.getElementType().getCorrespondingClass();
        var context = newContext(valueCls, feature);

        recordValues(context, paramDto, values);
        return paramDto;
    }

    // -- RECOVER IDENTIFIERS

    @Override
    public final Identifier actionIdentifier(final @NonNull ActionDto actionDto) {
        return memberIdentifierFor(getSpecificationLoader(),
                Type.ACTION,
                actionDto.getLogicalMemberIdentifier());
    }

    @Override
    public final Identifier actionIdentifier(final @NonNull ActionInvocationDto actionInvocationDto) {
        return memberIdentifierFor(getSpecificationLoader(),
                Type.ACTION,
                actionInvocationDto.getLogicalMemberIdentifier());
    }

    @Override
    public final Identifier propertyIdentifier(final @NonNull PropertyDto propertyDto) {
        return memberIdentifierFor(getSpecificationLoader(),
                Type.PROPERTY,
                propertyDto.getLogicalMemberIdentifier());
    }

    // -- RECOVER VALUES FROM DTO

    @Override
    public final ManagedObject recoverReferenceFrom(
            final @NonNull OidDto oidDto) {
        var bookmark = Bookmark.forOidDto(oidDto);
        return getObjectManager()
                .loadObject(ProtoObject.resolveElseFail(getSpecificationLoader(), bookmark));
    }

    @Override
    public final ManagedObject recoverPropertyFrom(
            final @NonNull PropertyDto propertyDto) {
        final Identifier propertyIdentifier = propertyIdentifier(propertyDto);
        var valueWithTypeDto = propertyDto.getNewValue();
        return recoverValueOrReference(propertyIdentifier, valueWithTypeDto, Cardinality.ONE);
    }

    @Override
    public final ManagedObject recoverParameterFrom(
            final @NonNull Identifier paramIdentifier,
            final @NonNull ParamDto paramDto) {

        var cardinalityConstraint = paramDto.getType().equals(ValueType.COLLECTION)
                ? Cardinality.MULTIPLE
                : Cardinality.ONE;
        return recoverValueOrReference(paramIdentifier, paramDto, cardinalityConstraint);
    }

    // -- HELPER
    
    /**
     * Recovers an {@link Identifier} for given {@code logicalMemberIdentifier}.
     */
    @SneakyThrows
    private static Identifier memberIdentifierFor(
            final @NonNull SpecificationLoader specLoader,
            final Identifier.@NonNull Type identifierType,
            final @NonNull String logicalMemberIdentifier) {

        var stringCutter = TextUtils.cutter(logicalMemberIdentifier);
        var logicalTypeName = stringCutter
                .keepBefore("#")
                .getValue();
        var memberId = stringCutter
                .keepAfter("#")
                .getValue();
        var typeSpec = specLoader.specForLogicalTypeNameElseFail(logicalTypeName);
        var logicalType = LogicalType.eager(typeSpec.getCorrespondingClass(), logicalTypeName);

        if(identifierType.isAction()) {
            return Identifier.actionIdentifier(logicalType, memberId);
        }

        if(identifierType.isProperty()) {
            return Identifier.propertyIdentifier(logicalType, memberId);
        }

        if(identifierType.isCollection()) {
            return Identifier.collectionIdentifier(logicalType, memberId);
        }

        throw _Exceptions.illegalArgument("unsupported identifier type %s (logicalMemberIdentifier=%s)",
                identifierType, logicalMemberIdentifier);
    }

    private <T> Context<T> newContext(
            final Class<T> valueCls,
            final ObjectFeature feature){
        return getValueSemanticsResolver()
                .selectValueSemantics(feature.getFeatureIdentifier(), valueCls)
                .getFirst()
                .map(valueSemantics->Context.forValue(valueCls, feature, valueSemantics))
                .orElseGet(()->Context.forNonValue(valueCls, feature));
    }

    // -- LOW LEVEL IMPLEMENTATION - RECORDING

    protected abstract <T> ValueWithTypeDto
        recordValue(Context<T> context, ValueWithTypeDto valueDto, ManagedObject value);

    protected abstract <T> ValueWithTypeDto
        recordValues(Context<T> context, ValueWithTypeDto valueDto, Can<ManagedObject> values);

    // -- LOW LEVEL IMPLEMENTATION - RECOVERY

    /**
     * References and collections are already dealt with.
     * Implementations only need to consider a scalar value-type.
     */
    protected abstract ManagedObject recoverScalarValue(
            final @NonNull Context<?> valueTypeHelper,
            final @NonNull ValueWithTypeDto valueDto);

    protected ManagedObject recoverValue(
            final @NonNull Context<?> valueTypeHelper,
            final @NonNull ValueWithTypeDto valueDto,
            final @NonNull Cardinality cardinalityConstraint) {

        return cardinalityConstraint.isMultiple()
                ? ManagedObject.packed(
                        valueTypeHelper.getElementType(),
                        recoverCollectionOfValues(valueTypeHelper, valueDto.getCollection()))
                : recoverScalarValue(valueTypeHelper, valueDto);
    }

    protected ManagedObject recoverReference(
            final @NonNull ObjectFeature feature,
            final @NonNull ValueDto valueDto,
            final @NonNull Cardinality cardinalityConstraint) {

        return cardinalityConstraint.isMultiple()
                ? ManagedObject.packed(
                        feature.getElementType(),
                        recoverCollectionOfReferences(valueDto.getCollection()))
                : recoverReferenceFrom(valueDto.getReference());
    }

    protected Can<ManagedObject> recoverCollectionOfValues(
            final Context<?> valueTypeHelper,
            final CollectionDto collectionDto) {

        _Assert.assertEquals(valueTypeHelper.schemaValueType(), collectionDto.getType());

        if(_NullSafe.isEmpty(collectionDto.getValue())) {
            return Can.empty();
        }

        var elementDtos = collectionDto.getValue();
        var list = new ArrayList<ManagedObject>(elementDtos.size());

        for(var _elementDto : elementDtos) {

            var elementDto = CommonDtoUtils
                    .toValueWithTypeDto(valueTypeHelper.schemaValueType(), _elementDto);
            var cardinalityConstraint = elementDto.getCollection()!=null
                    ? Cardinality.MULTIPLE
                    : Cardinality.ONE;
            list.add(recoverValue(valueTypeHelper, elementDto, cardinalityConstraint));
        }
        return Can.ofCollection(list);
    }

    protected Can<ManagedObject> recoverCollectionOfReferences(
            final CollectionDto collectionDto) {

        if(_NullSafe.isEmpty(collectionDto.getValue())) {
            return Can.empty();
        }

        var elementDtos = collectionDto.getValue();
        var list = new ArrayList<ManagedObject>(elementDtos.size());

        for(var elementDto : elementDtos) {
            list.add(recoverReferenceFrom(elementDto.getReference()));
        }
        return Can.ofCollection(list);
    }

    private ManagedObject recoverValueOrReference(
            final Identifier featureIdentifier,
            final ValueWithTypeDto valueWithTypeDto,
            final Cardinality cardinalityConstraint) {

        var feature = getSpecificationLoader().loadFeatureElseFail(featureIdentifier);

        if(valueWithTypeDto==null
                || (valueWithTypeDto.isNull()!=null
                    && valueWithTypeDto.isNull())) {
            return cardinalityConstraint.isMultiple()
                    ? ManagedObject.packed(feature.getElementType(), Can.empty())
                    : ManagedObject.empty(feature.getElementType());
        }

        var valueCls = feature.getElementType().getCorrespondingClass();

        var recoveredValueOrReference = feature.getElementType().isValue()
                ? recoverValue(newContext(valueCls, feature), valueWithTypeDto, cardinalityConstraint)
                // assume reference otherwise
                : recoverReference(feature, valueWithTypeDto, cardinalityConstraint);

        return recoveredValueOrReference;
    }

    // -- DEPENDENCIES

    protected abstract ValueSemanticsResolver getValueSemanticsResolver();

}
