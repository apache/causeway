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
package org.apache.isis.viewer.graphql.viewer.source;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class QueryFieldFactory {

    private final ServiceRegistry serviceRegistry;
    private final SpecificationLoader specificationLoader;

    public void queryFieldFromObjectSpecification(
            final GraphQLObjectType.Builder queryBuilder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final ObjectSpecification objectSpecification) {

        val logicalTypeName = objectSpecification.getLogicalTypeName();
        String logicalTypeNameSanitized = _Utils.logicalTypeNameSanitized(logicalTypeName);

        serviceRegistry.lookupBeanById(logicalTypeName)
        .ifPresent(service -> {

            List<ObjectAction> objectActionList = objectSpecification.streamRuntimeActions(MixedIn.INCLUDED)
                    .map(ObjectAction.class::cast)
                    .filter((final ObjectAction x) -> x.containsFacet(ActionSemanticsFacet.class))
//                            .filter(x -> x.getFacet(ActionSemanticsFacet.class).value() == SemanticsOf.SAFE)
                    .collect(Collectors.toList());

            // for now filters when no safe actions
            if (!objectActionList.isEmpty()) {

                val serviceAsGraphQlType = newObject().name(logicalTypeNameSanitized);

                objectActionList
                .forEach(objectAction -> {
                    String fieldName = objectAction.getId();

                    GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                            .name(fieldName)
                            .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
                    if (objectAction.getParameters().isNotEmpty()) {
                        builder.arguments(objectAction.getParameters().stream()
                                .map(objectActionParameter -> GraphQLArgument.newArgument()
                                        .name(objectActionParameter.getId())
                                        .type(TypeMapper.inputTypeFor(objectActionParameter))
                                        .build())
                                .collect(Collectors.toList()));
                    }
                    serviceAsGraphQlType
                            .field(builder
                                    .build());

                });

                GraphQLObjectType graphQLObjectType = serviceAsGraphQlType.build();

                objectActionList
                .forEach(objectAction -> {

                    String fieldName = objectAction.getId();
                    codeRegistryBuilder
                    .dataFetcher(
                        FieldCoordinates.coordinates(graphQLObjectType, fieldName),
                        new DataFetcher<Object>() {

                            @Override
                            public Object get(final DataFetchingEnvironment dataFetchingEnvironment) throws Exception {

                                Object domainObjectInstance = dataFetchingEnvironment.getSource();

                                Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
                                ObjectSpecification specification = specificationLoader
                                        .loadSpecification(domainObjectInstanceClass);

                                ManagedObject owner = ManagedObject.of(specification, domainObjectInstance);

                                ActionInteractionHead actionInteractionHead = objectAction.interactionHead(owner);

                                Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
                                Can<ObjectActionParameter> parameters = objectAction.getParameters();
                                Can<ManagedObject> canOfParams = parameters.stream().map(oap -> {
                                    Object argumentValue = arguments.get(oap.getId());
                                    ObjectSpecification elementType = oap.getElementType();

                                    if (argumentValue == null)
                                        return ManagedObject.empty(elementType);
                                    return ManagedObject.of(elementType, argumentValue);


                                }).collect(Can.toCan());

                                    ManagedObject managedObject = objectAction
                                            .execute(actionInteractionHead, canOfParams, InteractionInitiatedBy.USER);

                                return managedObject.getPojo();
                            }

                        });

                });

                queryBuilder.field(newFieldDefinition()
                        .name(logicalTypeNameSanitized)
                        .type(serviceAsGraphQlType)
                        .build());
                codeRegistryBuilder
                .dataFetcher(
                        FieldCoordinates.coordinates("Query", newFieldDefinition()
                            .name(logicalTypeNameSanitized)
                            .type(serviceAsGraphQlType)
                            .build().getName()),
                        (DataFetcher<Object>) environment -> service);
            }
        });
    }
}
