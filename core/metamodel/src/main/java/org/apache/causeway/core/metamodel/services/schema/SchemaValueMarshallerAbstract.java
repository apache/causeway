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

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.Identifier.Type;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.Converter;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.Cardinality;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ProtoObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.CollectionDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.ValueDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public abstract class SchemaValueMarshallerAbstract
implements SchemaValueMarshaller, HasMetaModelContext {

    @Value(staticConstructor = "of")
    public static class Context<T> {

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

            return of(correspondingClass, feature,
                    semantics.getSchemaValueType(),
                    semantics,
                    Optional.ofNullable(semantics.getConverter()));
        }

        private final @NonNull Class<T> correspondingClass;
        private final @NonNull ObjectFeature feature;
        @Getter private final @NonNull ValueType schemaValueType;
        private final @Nullable ValueSemanticsProvider<T> semantics;
        private final @NonNull Optional<Converter<T, ?>> converter;

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

        val feature = objectAction;
        val elementTypeAsClass = feature.getElementType().getCorrespondingClass();
        val context = newContext(elementTypeAsClass, feature);
        invocationDto.setReturned(
                recordValue(context, new ValueWithTypeDto(), value));
        return invocationDto;
    }

    @Override
    public final ActionInvocationDto recordActionResultNonScalar(
            final @NonNull ActionInvocationDto invocationDto,
            final @NonNull ObjectAction objectAction,
            final @NonNull Can<ManagedObject> value) {

        val feature = objectAction;
        val elementTypeAsClass = feature.getElementType().getCorrespondingClass();
        val context = newContext(elementTypeAsClass, feature);
        invocationDto.setReturned(
                recordValues(context, new ValueWithTypeDto(), value));
        return invocationDto;
    }

    @Override
    public final PropertyDto recordPropertyValue(
            final @NonNull PropertyDto propertyDto,
            final @NonNull OneToOneAssociation property,
            final @NonNull ManagedObject value) {

        val feature = property;
        val elementTypeAsClass = feature.getElementType().getCorrespondingClass();

        // guard against property not being a scalar
        _Assert.assertEquals(elementTypeAsClass, property.getElementType().getCorrespondingClass());

        val context = newContext(elementTypeAsClass, feature);
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

        val feature = actionParameter;
        val elementTypeAsClass = feature.getElementType().getCorrespondingClass();
        val context = newContext(elementTypeAsClass, feature);

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

        val feature = actionParameter;
        val valueCls = feature.getElementType().getCorrespondingClass();
        val context = newContext(valueCls, feature);

        recordValues(context, paramDto, values);
        return paramDto;
    }

    // -- RECOVER IDENTIFIERS

    @Override
    public final Identifier actionIdentifier(final @NonNull ActionDto actionDto) {
        return IdentifierUtil.memberIdentifierFor(getSpecificationLoader(),
                Type.ACTION,
                actionDto.getLogicalMemberIdentifier());
    }

    @Override
    public final Identifier actionIdentifier(final @NonNull ActionInvocationDto actionInvocationDto) {
        return IdentifierUtil.memberIdentifierFor(getSpecificationLoader(),
                Type.ACTION,
                actionInvocationDto.getLogicalMemberIdentifier());
    }

    @Override
    public final Identifier propertyIdentifier(final @NonNull PropertyDto propertyDto) {
        return IdentifierUtil.memberIdentifierFor(getSpecificationLoader(),
                Type.PROPERTY,
                propertyDto.getLogicalMemberIdentifier());
    }

    // -- RECOVER VALUES FROM DTO

    @Override
    public final ManagedObject recoverReferenceFrom(
            final @NonNull OidDto oidDto) {
        val bookmark = Bookmark.forOidDto(oidDto);
        return getObjectManager()
                .loadObject(ProtoObject.resolveElseFail(getSpecificationLoader(), bookmark));
    }

    @Override
    public final ManagedObject recoverPropertyFrom(
            final @NonNull PropertyDto propertyDto) {
        final Identifier propertyIdentifier = propertyIdentifier(propertyDto);
        val valueWithTypeDto = propertyDto.getNewValue();
        return recoverValueOrReference(propertyIdentifier, valueWithTypeDto, Cardinality.ONE);
    }

    @Override
    public final ManagedObject recoverParameterFrom(
            final @NonNull Identifier paramIdentifier,
            final @NonNull ParamDto paramDto) {

        val cardinalityConstraint = paramDto.getType().equals(ValueType.COLLECTION)
                ? Cardinality.MULTIPLE
                : Cardinality.ONE;
        return recoverValueOrReference(paramIdentifier, paramDto, cardinalityConstraint);
    }

    // -- HELPER

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

        _Assert.assertEquals(valueTypeHelper.getSchemaValueType(), collectionDto.getType());

        if(_NullSafe.isEmpty(collectionDto.getValue())) {
            return Can.empty();
        }

        val elementDtos = collectionDto.getValue();
        val list = new ArrayList<ManagedObject>(elementDtos.size());

        for(val _elementDto : elementDtos) {

            val elementDto = CommonDtoUtils
                    .toValueWithTypeDto(valueTypeHelper.getSchemaValueType(), _elementDto);
            val cardinalityConstraint = elementDto.getCollection()!=null
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

        val elementDtos = collectionDto.getValue();
        val list = new ArrayList<ManagedObject>(elementDtos.size());

        for(val elementDto : elementDtos) {
            list.add(recoverReferenceFrom(elementDto.getReference()));
        }
        return Can.ofCollection(list);
    }

    private ManagedObject recoverValueOrReference(
            final Identifier featureIdentifier,
            final ValueWithTypeDto valueWithTypeDto,
            final Cardinality cardinalityConstraint) {

        val feature = getSpecificationLoader().loadFeatureElseFail(featureIdentifier);

        if(valueWithTypeDto==null
                || (valueWithTypeDto.isNull()!=null
                    && valueWithTypeDto.isNull())) {
            return cardinalityConstraint.isMultiple()
                    ? ManagedObject.packed(feature.getElementType(), Can.empty())
                    : ManagedObject.empty(feature.getElementType());
        }

        val valueCls = feature.getElementType().getCorrespondingClass();

        val recoveredValueOrReference = feature.getElementType().isValue()
                ? recoverValue(newContext(valueCls, feature), valueWithTypeDto, cardinalityConstraint)
                // assume reference otherwise
                : recoverReference(feature, valueWithTypeDto, cardinalityConstraint);

        return recoveredValueOrReference;
    }

    // -- DEPENDENCIES

    protected abstract ValueSemanticsResolver getValueSemanticsResolver();

}
