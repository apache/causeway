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
import static graphql.schema.GraphQLUnionType.newUnionType;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneFeature;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import lombok.RequiredArgsConstructor;

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
        if (clazz.isEnum())
			return contextProvider.get().graphQLTypeRegistry.addEnumTypeIfNotAlreadyPresent(clazz, SchemaType.RICH);
        if (ResourceValueTypes.isResourceType(clazz))
            return ResourceValueTypes.outputTypeFor(clazz, contextProvider.get());
        if (ResourceValueTypes.isLocalResourcePathType(clazz))
            return ResourceValueTypes.localResourcePathOutputType(contextProvider.get());
        return scalarMapper.scalarTypeFor(clazz);
    }

    @Override
    public GraphQLInputType inputTypeFor(final Class<?> clazz){
        if (clazz.isEnum())
			return contextProvider.get().graphQLTypeRegistry.addEnumTypeIfNotAlreadyPresent(clazz, SchemaType.RICH);
        if (ResourceValueTypes.isResourceType(clazz))
            return ResourceValueTypes.inputTypeFor(clazz, contextProvider.get());
        if (ResourceValueTypes.isLocalResourcePathType(clazz))
            return ResourceValueTypes.localResourcePathInputType(contextProvider.get());
        return scalarMapper.inputScalarTypeFor(clazz);
    }

    @Override
    public Object unmarshal(
            final Object gqlValue,
            final ObjectSpecification targetObjectSpec) {
        var correspondingClass = targetObjectSpec.getCorrespondingClass();
        if (correspondingClass.isEnum())
			return gqlValue;
        if (ResourceValueTypes.isResourceType(correspondingClass))
            return ResourceValueTypes.unmarshal(correspondingClass, gqlValue, contextProvider.get());
        if (ResourceValueTypes.isLocalResourcePathType(correspondingClass))
            return ResourceValueTypes.unmarshalLocalResourcePath(gqlValue);
        return scalarMapper.unmarshal(gqlValue, correspondingClass);
    }

    @Override
    public GraphQLOutputType outputTypeFor(
            final OneToOneFeature oneToOneFeature,
            final SchemaType schemaType) {
        ObjectSpecification otoaObjectSpec = oneToOneFeature.getElementType();

        return switch (otoaObjectSpec.beanSort()) {
            case ABSTRACT, VIEW_MODEL, ENTITY -> domainObjectTypePossiblyOptional(oneToOneFeature, schemaType, otoaObjectSpec);
            case VALUE-> scalarTypePossiblyOptional(oneToOneFeature, otoaObjectSpec);
            default -> null;
        };
    }

    private GraphQLOutputType domainObjectTypePossiblyOptional(
            final OneToOneFeature oneToOneFeature,
            final SchemaType schemaType,
            final ObjectSpecification objectSpecification) {
        var outputType = outputTypeFor(objectSpecification, schemaType);
        return oneToOneFeature.isOptional()
                ? outputType
                : nonNull(outputType);
    }

    private GraphQLOutputType scalarTypePossiblyOptional(final OneToOneFeature oneToOneFeature, final ObjectSpecification otoaObjectSpec) {
        GraphQLOutputType scalarType = outputTypeFor(otoaObjectSpec.getCorrespondingClass());
        return oneToOneFeature.isOptional()
                ? scalarType
                : nonNull(scalarType);
    }

    @Override
    @Nullable
    public GraphQLOutputType outputTypeFor(
            final ObjectSpecification objectSpecification,
            final SchemaType schemaType){

        return switch (objectSpecification.beanSort()){
            case ABSTRACT -> schemaType == SchemaType.RICH
                    ? polymorphicOutputTypeFor(objectSpecification, schemaType)
                    : typeRef(TypeNames.objectTypeNameFor(objectSpecification, schemaType));
            case VIEW_MODEL, ENTITY -> typeRef(TypeNames.objectTypeNameFor(objectSpecification, schemaType));
            case VALUE -> outputTypeFor(objectSpecification.getCorrespondingClass());
            case COLLECTION -> null; // should be noop
            default -> Scalars.GraphQLString; // for now
        };
    }

    @Override
    @Nullable public GraphQLList listTypeForElementTypeOf(
            final OneToManyAssociation oneToManyAssociation,
            final SchemaType schemaType) {
        var elementType = oneToManyAssociation.getElementType();
        return listTypeFor(elementType, schemaType);
    }

    @Override
    @Nullable public GraphQLList listTypeFor(
            final ObjectSpecification elementType,
            final SchemaType schemaType) {
        return switch (elementType.beanSort()) {
            case ABSTRACT, VIEW_MODEL, ENTITY ->
                GraphQLList.list(outputTypeFor(elementType, schemaType));
            case VALUE ->
                GraphQLList.list(outputTypeFor(elementType.getCorrespondingClass()));
            default -> null;
        };
    }

    private GraphQLOutputType polymorphicOutputTypeFor(
            final ObjectSpecification declaredType,
            final SchemaType schemaType) {
        var context = contextProvider.get();
        var possibleTypes = context.concreteSpecificationsAssignableTo(declaredType);
        if (possibleTypes.isEmpty()) {
            return typeRef(TypeNames.objectTypeNameFor(declaredType, schemaType));
        }

        var unionTypeName = TypeNames.polymorphicTypeNameFor(declaredType, schemaType);
        var existing = context.graphQLTypeRegistry.lookup(unionTypeName, GraphQLUnionType.class);
        if (existing.isPresent()) {
            return existing.get();
        }

        var unionBuilder = newUnionType()
                .name(unionTypeName)
                .description("Concrete rich object types assignable to " + declaredType.logicalTypeName());
        possibleTypes.stream()
                .map(specification -> TypeNames.objectTypeNameFor(specification, schemaType))
                .distinct()
                .map(GraphQLTypeReference::typeRef)
                .forEach(unionBuilder::possibleType);
        var unionType = context.graphQLTypeRegistry.addUnionTypeIfNotAlreadyPresent(unionBuilder.build());
        context.codeRegistryBuilder.typeResolver(unionType, environment -> {
            var pojo = environment.getObject();
            if (pojo instanceof BookmarkedPojo bookmarkedPojo) {
                pojo = bookmarkedPojo.getTargetPojo();
            }
            if (pojo instanceof ManagedObject managedObject) {
                pojo = managedObject.getPojo();
            }
            if (pojo == null || !declaredType.isAssignableFrom(pojo.getClass())) {
                return null;
            }
            var runtimeSpecification = context.specificationLoader.loadSpecification(pojo.getClass());
            if (runtimeSpecification == null) {
                return null;
            }
            return environment.getSchema().getObjectType(
                    TypeNames.objectTypeNameFor(runtimeSpecification, schemaType));
        });
        return unionType;
    }

    @Override
    public GraphQLInputType inputTypeFor(
            final OneToOneFeature oneToOneFeature,
            final InputContext inputContext,
            final SchemaType schemaType) {

        return oneToOneFeature.isOptional() || inputContext.isOptionalAlwaysAllowed()
                ? inputTypeFor_(oneToOneFeature, schemaType)
                : nonNull(inputTypeFor_(oneToOneFeature, schemaType));
    }

    private GraphQLInputType inputTypeFor_(
            final OneToOneFeature oneToOneFeature,
            final SchemaType schemaType){
        var elementObjectSpec = oneToOneFeature.getElementType();

        {   // guard introduced to intercept interfaces, which otherwise seem to break schema creation
            // due to missing type reference for given name
            var elementClass = elementObjectSpec.getCorrespondingClass();
            if(elementClass.isInterface()) return inputTypeFor(elementClass);
        }

        return switch (elementObjectSpec.beanSort()) {
            case ABSTRACT, VIEW_MODEL, ENTITY -> typeRef(TypeNames.inputTypeNameFor(elementObjectSpec, schemaType));
            case VALUE -> inputTypeFor(elementObjectSpec.getCorrespondingClass());
            case COLLECTION ->
                throw new IllegalArgumentException(String.format("OneToOneFeature '%s' is not expected to have a beanSort of COLLECTION", oneToOneFeature.getFeatureIdentifier().toString()));
            default -> GraphQLValueScalars.UNSUPPORTED_INPUT;
        };
    }

    @Override
    public GraphQLList inputTypeFor(
            final OneToManyActionParameter oneToManyActionParameter,
            final SchemaType schemaType){
        var elementObjectSpec = oneToManyActionParameter.getElementType();
        return GraphQLList.list(inputTypeFor(elementObjectSpec, schemaType));
    }

    @Override
    public GraphQLInputType inputTypeFor(
            final ObjectSpecification elementType,
            final SchemaType schemaType){
        return switch (elementType.beanSort()) {
            case ABSTRACT, VIEW_MODEL, ENTITY -> typeRef(TypeNames.inputTypeNameFor(elementType, schemaType));
            case VALUE -> inputTypeFor(elementType.getCorrespondingClass());
            case COLLECTION ->
                throw new IllegalArgumentException(String.format("ObjectSpec '%s' is not expected to have a beanSort of COLLECTION", elementType.getFullIdentifier()));
            default -> GraphQLValueScalars.UNSUPPORTED_INPUT;
        };
    }

}
