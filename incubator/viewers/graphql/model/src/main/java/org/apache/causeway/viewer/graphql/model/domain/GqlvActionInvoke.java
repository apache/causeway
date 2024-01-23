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
package org.apache.causeway.viewer.graphql.model.domain;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;

import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.types.ScalarMapper;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import org.springframework.lang.Nullable;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;

@Log4j2
public class GqlvActionInvoke {

    private final GqlvActionInvokeHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final GraphQLFieldDefinition field;

    public GqlvActionInvoke(
            final GqlvActionInvokeHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
    }

    private static GraphQLFieldDefinition fieldDefinition(final GqlvActionInvokeHolder holder) {

        val objectAction = holder.getObjectAction();

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = typeFor(objectAction);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name("invoke")
                    .type(type);
            addGqlArguments(objectAction, fieldBuilder);
            fieldDefinition = fieldBuilder.build();

            holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    @Nullable
    private static GraphQLOutputType typeFor(final ObjectAction objectAction){
        ObjectSpecification objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                if (facet == null) {
                    log.warn("Unable to locate TypeOfFacet for {}", objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                ObjectSpecification objectSpecificationForElementWhenCollection = facet.elementSpec();
                GraphQLType wrappedType = outputTypeFor(objectSpecificationForElementWhenCollection);
                if (wrappedType == null) {
                    log.warn("Unable to create wrapped type of for {} for action {}",
                            objectSpecificationForElementWhenCollection.getFullIdentifier(),
                            objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                return GraphQLList.list(wrappedType);

            case VALUE:
            case ENTITY:
            case VIEW_MODEL:
            default:
                // TODO: this cast is suspicious
                return (GraphQLOutputType) outputTypeFor(objectSpecification);

        }
    }

    @Nullable
    private static GraphQLType outputTypeFor(final ObjectSpecification objectSpecification){

        switch (objectSpecification.getBeanSort()){
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return GraphQLTypeReference.typeRef(TypeNames.objectTypeNameFor(objectSpecification));

            case VALUE:
                return ScalarMapper.typeFor(objectSpecification.getCorrespondingClass());

            case COLLECTION:
                // should be noop
                return null;

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

    static void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder builder) {

        Can<ObjectActionParameter> parameters = objectAction.getParameters();

        if (parameters.isNotEmpty()) {
            builder.arguments(parameters.stream()
                    .map(GqlvActionInvoke::gqlArgumentFor)
                    .collect(Collectors.toList()));
        }
    }

    private static GraphQLArgument gqlArgumentFor(final ObjectActionParameter objectActionParameter) {
        return GraphQLArgument.newArgument()
                .name(objectActionParameter.getId())
                .type(objectActionParameter.isOptional()
                        ? GqlvActionParameter.inputTypeFor(objectActionParameter)
                        : nonNull(GqlvActionParameter.inputTypeFor(objectActionParameter)))
                .build();
    }

    public void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                this::invoke
        );
    }

    private Object invoke(
            final DataFetchingEnvironment dataFetchingEnvironment) {
        final ObjectAction objectAction = holder.getObjectAction();

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val specificationLoader = holder.getObjectAction().getSpecificationLoader();
        val objectSpecification = specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val actionInteractionHead = objectAction.interactionHead(managedObject);

        Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
        Can<ObjectActionParameter> parameters = objectAction.getParameters();
        Can<ManagedObject> canOfParams = parameters
                .map(oap -> {
                    Object argumentValue = arguments.get(oap.getId());
                    return ManagedObject.adaptParameter(oap, argumentValue);
                });

        val resultManagedObject = objectAction
                .execute(actionInteractionHead, canOfParams, InteractionInitiatedBy.USER);

        return resultManagedObject.getPojo();
    }

}
