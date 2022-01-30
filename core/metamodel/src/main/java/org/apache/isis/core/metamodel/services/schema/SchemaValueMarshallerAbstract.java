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
package org.apache.isis.core.metamodel.services.schema;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.Identifier.Type;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.Cardinality;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.CollectionDto;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.ValueDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

public abstract class SchemaValueMarshallerAbstract
implements SchemaValueMarshaller {

    @Value(staticConstructor = "of")
    public static class ValueTypeHelper {

        private final @NonNull ObjectFeature feature;
        private final @Nullable ValueSemanticsProvider<?> semantics;

        public ObjectSpecification getElementType() {
            return feature.getElementType();
        }
        public ValueType getSchemaValueType() {
            return semantics.getSchemaValueType();
        }
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
                Type.PROPERTY_OR_COLLECTION,
                propertyDto.getLogicalMemberIdentifier());
    }

    // -- RECOVER VALUES FROM DTO

    @Override
    public final ManagedObject recoverReferenceFrom(
            final @NonNull OidDto oidDto) {
        val bookmark = Bookmark.forOidDto(oidDto);
        val spec = getSpecificationLoader().specForLogicalTypeNameElseFail(bookmark.getLogicalTypeName());
        val loadRequest = ObjectLoader.Request.of(spec, bookmark);
        return spec.getMetaModelContext().getObjectManager().loadObject(loadRequest);
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

    private ManagedObject recoverValueOrReference(
            final Identifier featureIdentifier,
            final ValueWithTypeDto valueWithTypeDto,
            final Cardinality cardinalityConstraint) {

        val feature = getSpecificationLoader().loadFeatureElseFail(featureIdentifier);

        if(valueWithTypeDto==null
                || (valueWithTypeDto.isSetNull()
                    && valueWithTypeDto.isNull())) {
            return cardinalityConstraint.isMultiple()
                    ? PackedManagedObject.pack(feature.getElementType(), Can.empty())
                    : ManagedObject.empty(feature.getElementType());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Optional<ValueSemanticsProvider<?>> preferredValueSemantics = getValueSemanticsResolver()
                .selectValueSemantics(
                        featureIdentifier,
                        (Class)feature.getElementType().getCorrespondingClass())
                .getFirst();

        val recoveredValueOrReference = preferredValueSemantics
            .map(valueSemantics->ValueTypeHelper.of(feature, valueSemantics))
            .map(valueTypeHelper->recoverValue(valueTypeHelper, valueWithTypeDto, cardinalityConstraint))
            // assume reference otherwise
            .orElseGet(()->recoverReference(feature, valueWithTypeDto, cardinalityConstraint));

        return recoveredValueOrReference;
    }

    // -- LOW LEVEL IMPLEMENTATION

    /**
     * References, collections and {@code null} are already dealt with.
     * Implementations only need to consider a non-empty scalar value-type.
     */
    protected abstract ManagedObject recoverScalarValue(
            final @NonNull ValueTypeHelper valueTypeHelper,
            final @NonNull ValueWithTypeDto valueDto);

    protected ManagedObject recoverValue(
            final @NonNull ValueTypeHelper valueTypeHelper,
            final @NonNull ValueWithTypeDto valueDto,
            final @NonNull Cardinality cardinalityConstraint) {

        return cardinalityConstraint.isMultiple()
                ? PackedManagedObject.pack(
                        valueTypeHelper.getElementType(),
                        recoverCollectionOfValues(valueTypeHelper, valueDto.getCollection()))
                : recoverScalarValue(valueTypeHelper, valueDto);
//                    valueDto.getReference()!=null
//                        ? recoverScalarValue(valueTypeHelper, valueDto)
//                        : ManagedObject.empty(valueTypeHelper.getElementType());
    }

    protected ManagedObject recoverReference(
            final @NonNull ObjectFeature feature,
            final @NonNull ValueDto valueDto,
            final @NonNull Cardinality cardinalityConstraint) {

        return cardinalityConstraint.isMultiple()
                ? PackedManagedObject.pack(
                        feature.getElementType(),
                        recoverCollectionOfReferences(valueDto.getCollection()))
                : recoverReferenceFrom(valueDto.getReference());
    }

    protected Can<ManagedObject> recoverCollectionOfValues(
            final ValueTypeHelper valueTypeHelper,
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


    // -- DEPENDENCIES

    protected abstract SpecificationLoader getSpecificationLoader();
    protected abstract ValueSemanticsResolver getValueSemanticsResolver();

}
