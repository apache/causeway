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

import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLTypeReference.typeRef;

import jakarta.inject.Inject;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneFeature;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TypeMapperDefault implements TypeMapper {

    @Configuration
    public static class AutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(TypeMapper.class)
        public TypeMapper defaultTypeMapper(final ScalarMapper scalarMapper) {
            return new TypeMapperDefault(scalarMapper);
        }
    }

    private final ScalarMapper scalarMapper;


    @Override
    public GraphQLScalarType scalarTypeFor(final Class<?> clazz){
        GraphQLScalarType scalarType = scalarMapper.scalarTypeFor(clazz);
        return scalarType;
    }

    @Override
    public Object unmarshal(
            final Object gqlValue,
            final ObjectSpecification targetObjectSpec) {
        return scalarMapper.unmarshal(gqlValue, targetObjectSpec.getCorrespondingClass());
    }

    @Override
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

    @Override
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

    @Override
    @Nullable public GraphQLList listTypeForElementTypeOf(final OneToManyAssociation oneToManyAssociation) {
        val elementType = oneToManyAssociation.getElementType();
        return listTypeFor(elementType);
    }

    @Override
    @Nullable public GraphQLList listTypeFor(final ObjectSpecification elementType) {
        switch (elementType.getBeanSort()) {
            case VIEW_MODEL:
            case ENTITY:
                return GraphQLList.list(typeRef(TypeNames.objectTypeNameFor(elementType)));
            case VALUE:
                return GraphQLList.list(scalarTypeFor(elementType.getCorrespondingClass()));
        }
        return null;
    }

    @Override
    public GraphQLInputType inputTypeFor(
            final OneToOneFeature oneToOneFeature,
            final InputContext inputContext) {
        return oneToOneFeature.isOptional() || inputContext.isOptionalAlwaysAllowed()
                ? inputTypeFor_(oneToOneFeature)
                : nonNull(inputTypeFor_(oneToOneFeature));
    }

    private GraphQLInputType inputTypeFor_(final OneToOneFeature oneToOneFeature){
        val elementObjectSpec = oneToOneFeature.getElementType();
        switch (elementObjectSpec.getBeanSort()) {
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return typeRef(TypeNames.inputTypeNameFor(elementObjectSpec));

            case VALUE:
                return scalarTypeFor(elementObjectSpec.getCorrespondingClass());

            case COLLECTION:
                throw new IllegalArgumentException(String.format("OneToOneFeature '%s' is not expected to have a beanSort of COLLECTION", oneToOneFeature.getFeatureIdentifier().toString()));
            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

    @Override
    public GraphQLList inputTypeFor(final OneToManyActionParameter oneToManyActionParameter){
        val elementObjectSpec = oneToManyActionParameter.getElementType();
        return GraphQLList.list(inputTypeFor(elementObjectSpec));
    }

    @Override
    public GraphQLInputType inputTypeFor(final ObjectSpecification elementType){
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

}
