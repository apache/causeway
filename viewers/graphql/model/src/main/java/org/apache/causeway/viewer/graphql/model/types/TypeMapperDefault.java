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

import javax.inject.Inject;
import javax.inject.Provider;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLTypeReference.typeRef;

import org.apache.causeway.viewer.graphql.model.domain.SchemaType;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneFeature;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TypeMapperDefault implements TypeMapper {

    @Configuration
    public static class AutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(TypeMapper.class)
        public TypeMapper defaultTypeMapper(final ScalarMapper scalarMapper, final Provider<Context> contextProvider) {
            return new TypeMapperDefault(scalarMapper, contextProvider);
        }
    }

    private final ScalarMapper scalarMapper;
    private final Provider<Context> contextProvider;


    @Override
    public GraphQLOutputType outputTypeFor(final Class<?> clazz){
        if (clazz.isEnum()) {
            return contextProvider.get().graphQLTypeRegistry.addEnumTypeIfNotAlreadyPresent(clazz);
        }
        return scalarMapper.scalarTypeFor(clazz);
    }

    @Override
    public GraphQLInputType inputTypeFor(final Class<?> clazz){
        if (clazz.isEnum()) {
            return contextProvider.get().graphQLTypeRegistry.addEnumTypeIfNotAlreadyPresent(clazz);
        }
        return scalarMapper.scalarTypeFor(clazz);
    }

    @Override
    public Object unmarshal(
            final Object gqlValue,
            final ObjectSpecification targetObjectSpec) {
        val correspondingClass = targetObjectSpec.getCorrespondingClass();
        if (correspondingClass.isEnum()) {
            return gqlValue;
        }
        return scalarMapper.unmarshal(gqlValue, correspondingClass);
    }

    @Override
    public GraphQLOutputType outputTypeFor(final OneToOneFeature oneToOneFeature) {
        ObjectSpecification otoaObjectSpec = oneToOneFeature.getElementType();
        switch (otoaObjectSpec.getBeanSort()) {

            case VIEW_MODEL:
            case ENTITY:

                GraphQLTypeReference fieldTypeRef = typeRef(TypeNames.objectTypeNameFor(otoaObjectSpec, SchemaType.RICH));
                return oneToOneFeature.isOptional()
                        ? fieldTypeRef
                        : nonNull(fieldTypeRef);

            case VALUE:

                GraphQLOutputType scalarType = outputTypeFor(otoaObjectSpec.getCorrespondingClass());

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
                return typeRef(TypeNames.objectTypeNameFor(objectSpecification, SchemaType.RICH));

            case VALUE:
                return outputTypeFor(objectSpecification.getCorrespondingClass());

            case COLLECTION:
                // should be noop
                return null;

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

    @Override
    @Nullable public GraphQLList listTypeForElementTypeOf(OneToManyAssociation oneToManyAssociation) {
        val elementType = oneToManyAssociation.getElementType();
        return listTypeFor(elementType);
    }

    @Override
    @Nullable public GraphQLList listTypeFor(ObjectSpecification elementType) {
        switch (elementType.getBeanSort()) {
            case VIEW_MODEL:
            case ENTITY:
                return GraphQLList.list(typeRef(TypeNames.objectTypeNameFor(elementType, SchemaType.RICH)));
            case VALUE:
                return GraphQLList.list(outputTypeFor(elementType.getCorrespondingClass()));
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
                return typeRef(TypeNames.inputTypeNameFor(elementObjectSpec, SchemaType.RICH));

            case VALUE:
                return inputTypeFor(elementObjectSpec.getCorrespondingClass());

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
                return typeRef(TypeNames.inputTypeNameFor(elementType, SchemaType.RICH));

            case VALUE:
                return inputTypeFor(elementType.getCorrespondingClass());

            case COLLECTION:
                throw new IllegalArgumentException(String.format("ObjectSpec '%s' is not expected to have a beanSort of COLLECTION", elementType.getFullIdentifier()));

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

}
