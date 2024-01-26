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
package org.apache.causeway.viewer.graphql.model.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.NotSupportedException;

import org.apache.causeway.core.config.CausewayConfiguration;

import org.joda.time.DateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneFeature;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import lombok.RequiredArgsConstructor;
import lombok.val;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;

import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLTypeReference.typeRef;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TypeMapperDefault implements TypeMapper {

    @SuppressWarnings("CdiInjectInspection")
    @Inject private final CausewayConfiguration causewayConfiguration;

    private static <K,V> Map.Entry<K,V> pair(K key, V value) {
        return new Map.Entry<K, V>() {
            @Override public K getKey() {return key;}
            @Override public V getValue() {return value;}
            @Override public V setValue(V value) { throw new NotSupportedException("Immutable"); }
        };
    }

    private static final Map<Class<?>, GraphQLScalarType> SCALAR_BY_CLASS = _Maps.unmodifiableEntries(

            pair(int.class, Scalars.GraphQLInt),
            pair(Integer.class, Scalars.GraphQLInt),
            pair(Short.class, Scalars.GraphQLInt),
            pair(short.class, Scalars.GraphQLInt),
            pair(BigInteger.class, Scalars.GraphQLInt),

            pair(float.class, Scalars.GraphQLFloat),
            pair(Float.class, Scalars.GraphQLFloat),
            pair(double.class, Scalars.GraphQLFloat),
            pair(Double.class, Scalars.GraphQLFloat),
            pair(long.class, Scalars.GraphQLFloat),
            pair(Long.class, Scalars.GraphQLFloat),
            pair(BigDecimal.class, Scalars.GraphQLFloat),

            pair(boolean.class, Scalars.GraphQLBoolean),
            pair(Boolean.class, Scalars.GraphQLBoolean)
    );

    public GraphQLScalarType scalarTypeFor(final Class<?> c){
        return SCALAR_BY_CLASS.getOrDefault(c, Scalars.GraphQLString);
    }

    public GraphQLOutputType outputTypeFor(final OneToOneFeature oneToOneFeature) {
        ObjectSpecification otoaObjectSpec = oneToOneFeature.getElementType();
        switch (otoaObjectSpec.getBeanSort()) {

            case VIEW_MODEL:
            case ENTITY:

                GraphQLTypeReference fieldTypeRef = typeRef(TypeNames.objectTypeNameFor(otoaObjectSpec));
                return oneToOneFeature.isOptional()
                        ? fieldTypeRef
                        : nonNull(fieldTypeRef);

            case VALUE:

                GraphQLScalarType scalarType = scalarTypeFor(otoaObjectSpec.getCorrespondingClass());

                return oneToOneFeature.isOptional()
                        ? scalarType
                        : nonNull(scalarType);
        }
        return null;
    }

    @Nullable
    public GraphQLOutputType outputTypeFor(final ObjectSpecification objectSpecification){

        switch (objectSpecification.getBeanSort()){
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return typeRef(TypeNames.objectTypeNameFor(objectSpecification));

            case VALUE:
                return scalarTypeFor(objectSpecification.getCorrespondingClass());

            case COLLECTION:
                // should be noop
                return null;

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

    @Nullable public GraphQLList listTypeForElementTypeOf(OneToManyAssociation oneToManyAssociation) {
        ObjectSpecification elementType = oneToManyAssociation.getElementType();
        return listTypeFor(elementType);
    }

    @Nullable public GraphQLList listTypeFor(ObjectSpecification elementType) {
        switch (elementType.getBeanSort()) {
            case VIEW_MODEL:
            case ENTITY:
                return GraphQLList.list(typeRef(TypeNames.objectTypeNameFor(elementType)));
            case VALUE:
                return GraphQLList.list(scalarTypeFor(elementType.getCorrespondingClass()));
        }
        return null;
    }

    public GraphQLInputType inputTypeFor(
            final OneToOneFeature oneToOneFeature,
            final InputContext inputContext) {
        return oneToOneFeature.isOptional() || inputContext.isOptionalAlwaysAllowed()
                ? inputTypeFor_(oneToOneFeature)
                : nonNull(inputTypeFor_(oneToOneFeature));
    }

    private GraphQLInputType inputTypeFor_(final OneToOneFeature oneToOneFeature){
        ObjectSpecification elementType = oneToOneFeature.getElementType();
        switch (elementType.getBeanSort()) {
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return typeRef(TypeNames.inputTypeNameFor(elementType));

            case VALUE:
                return scalarTypeFor(elementType.getCorrespondingClass());

            case COLLECTION:
                throw new IllegalArgumentException(String.format("OneToOneFeature '%s' is not expected to have a beanSort of COLLECTION", oneToOneFeature.getFeatureIdentifier().toString()));
            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

    public GraphQLList inputTypeFor(final OneToManyActionParameter oneToManyActionParameter, final InputContext inputContextUnused){
        ObjectSpecification elementType = oneToManyActionParameter.getElementType();
        return GraphQLList.list(inputTypeFor_(elementType));
    }

    private GraphQLInputType inputTypeFor_(final ObjectSpecification elementType){
        switch (elementType.getBeanSort()) {
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return typeRef(TypeNames.inputTypeNameFor(elementType));

            case VALUE:
                return scalarTypeFor(elementType.getCorrespondingClass());

            case COLLECTION:
                throw new IllegalArgumentException(String.format("ObjectSpec '%s' is not expected to have a beanSort of COLLECTION", elementType.getFullIdentifier()));

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

    public Object adaptPojo(
            final Object argumentValue,
            final ObjectSpecification elementType) {
        val elementClazz = elementType.getCorrespondingClass();

        if (elementClazz.isEnum()) {
            return Enum.valueOf((Class<Enum>) elementClazz, argumentValue.toString());
        }

        if (elementClazz == BigInteger.class) {
            return BigInteger.valueOf((Integer) argumentValue);
        }

        if (elementClazz == BigDecimal.class) {
            return BigDecimal.valueOf((Double) argumentValue);
        }

        val typeMapperConfig = causewayConfiguration.getViewer().getGqlv().getTypeMapper();
        if (elementClazz == LocalDate.class) {
            String argumentStr = (String) argumentValue;
            return LocalDate.parse(argumentStr, DateTimeFormatter.ofPattern(typeMapperConfig.getLocalDateFormat()));
        }

        if (elementClazz == org.joda.time.LocalDate.class) {
            String argumentStr = (String) argumentValue;
            return org.joda.time.LocalDate.parse(argumentStr, org.joda.time.format.DateTimeFormat.forPattern(typeMapperConfig.getLocalDateFormat()));
        }

        if (elementClazz == ZonedDateTime.class) {
            String argumentStr = (String) argumentValue;
            return ZonedDateTime.parse(argumentStr, DateTimeFormatter.ofPattern(typeMapperConfig.getZonedDateTimeFormat()));
        }

        if (elementClazz == DateTime.class) {
            String argumentStr = (String) argumentValue;
            return DateTime.parse(argumentStr, org.joda.time.format.DateTimeFormat.forPattern(typeMapperConfig.getLocalDateFormat()));
        }

        if (elementClazz == float.class || elementClazz == Float.class) {
            return ((Double) argumentValue).floatValue();
        }

        return argumentValue;
    }

}
