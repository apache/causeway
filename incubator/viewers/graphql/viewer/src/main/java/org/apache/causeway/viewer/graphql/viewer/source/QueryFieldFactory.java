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
package org.apache.causeway.viewer.graphql.viewer.source;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import lombok.RequiredArgsConstructor;
import lombok.val;

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

                                ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);

                                ActionInteractionHead actionInteractionHead = objectAction.interactionHead(owner);

                                Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
                                Can<ObjectActionParameter> parameters = objectAction.getParameters();
                                Can<ManagedObject> canOfParams = parameters
                                        .map(oap -> {
                                            Object argumentValue = arguments.get(oap.getId());
                                            return ManagedObject.adaptParameter(oap, argumentValue);
                                        });

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
