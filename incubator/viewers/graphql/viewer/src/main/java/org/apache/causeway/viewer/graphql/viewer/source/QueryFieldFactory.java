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
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class QueryFieldFactory {

    private static ObjectAction objectAction;
    private final ServiceRegistry serviceRegistry;
    private final SpecificationLoader specificationLoader;

    public void queryFieldFromObjectSpecification(
            final ObjectSpecification objectSpec,
            final GqlvTopLevelQueryStructure topLevelQueryStructure,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
        .ifPresent(service -> {
            addService(objectSpec, service, topLevelQueryStructure, codeRegistryBuilder);
        });
    }

    private void addService(
            final ObjectSpecification serviceSpec,
            final Object service,
            final GqlvTopLevelQueryStructure topLevelQueryStructure,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        val gqlvServiceStructure = new GqlvServiceStructure(serviceSpec, topLevelQueryStructure);

        List<ObjectAction> objectActionList = serviceSpec.streamRuntimeActions(MixedIn.INCLUDED)
                .map(ObjectAction.class::cast)
                .filter((final ObjectAction x) -> x.containsFacet(ActionSemanticsFacet.class))
                .collect(Collectors.toList());

        if (!objectActionList.isEmpty()) {

            val serviceAsGraphQlType = gqlvServiceStructure.getGraphQlTypeBuilder();

            objectActionList.forEach(gqlvServiceStructure::addAction);

            gqlvServiceStructure.buildObjectGqlType();

            objectActionList
            .forEach(objectAction -> {
                addBehaviour(gqlvServiceStructure, objectAction, codeRegistryBuilder);
            });

            gqlvServiceStructure.addTopLevelQueryField();

            String fieldName = newFieldDefinition()
                    .name(_LTN.sanitized(serviceSpec))
                    .type(serviceAsGraphQlType)
                    .build().getName();

            codeRegistryBuilder
            .dataFetcher(
                    FieldCoordinates.coordinates("Query", fieldName),
                    (DataFetcher<Object>) environment -> service);
        }
    }

    private void addBehaviour(
            final GqlvServiceStructure gqlvServiceStructure,
            final ObjectAction objectAction,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        final GraphQLObjectType graphQLObjectType = gqlvServiceStructure.getGqlObjectType();

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
    }

}
