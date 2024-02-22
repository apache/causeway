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
package org.apache.causeway.viewer.graphql.model.domain.rich.mutation;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import org.apache.causeway.viewer.graphql.model.domain.common.query.GvqlActionUtils;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvAction;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvMetaSaveAs;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvMutationForAction extends GqlvAbstract {

    private static final SchemaType SCHEMA_TYPE = SchemaType.RICH;

    private final ObjectSpecification objectSpec;
    private final ObjectAction objectAction;
    private String argumentName;

    public GqlvMutationForAction(
            final ObjectSpecification objectSpec,
            final ObjectAction objectAction,
            final Context context) {
        super(context);
        this.objectSpec = objectSpec;
        this.objectAction = objectAction;

        this.argumentName = context.causewayConfiguration.getViewer().getGraphql().getMutation().getTargetArgName();

        GraphQLOutputType type = typeFor(objectAction);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name(fieldName(objectSpec, objectAction))
                    .type(type);
            addGqlArguments(fieldBuilder);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    private static String fieldName(
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction) {
        return TypeNames.objectTypeFieldNameFor(objectSpecification) + "__" + objectAction.getId();
    }

    @Nullable
    private GraphQLOutputType typeFor(final ObjectAction objectAction){
        ObjectSpecification objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                if (facet == null) {
                    log.warn("Unable to locate TypeOfFacet for {}", objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                val objectSpecificationOfCollectionElement = facet.elementSpec();
                GraphQLType wrappedType = context.typeMapper.outputTypeFor(objectSpecificationOfCollectionElement, SCHEMA_TYPE);
                if (wrappedType == null) {
                    log.warn("Unable to create wrapped type of for {} for action {}",
                            objectSpecificationOfCollectionElement.getFullIdentifier(),
                            objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                return GraphQLList.list(wrappedType);

            case VALUE:
            case ENTITY:
            case VIEW_MODEL:
            default:
                return context.typeMapper.outputTypeFor(objectSpecification, SchemaType.RICH);

        }
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        val isService = objectSpec.getBeanSort().isManagedBeanContributing();

        val environment = new Environment.For(dataFetchingEnvironment);
        Object sourcePojo;
        if (isService) {
            sourcePojo = context.serviceRegistry.lookupServiceElseFail(objectSpec.getCorrespondingClass());
        } else {
            Object target = dataFetchingEnvironment.getArgument(argumentName);
            Optional<Object> result;
            val argumentValue = (Map<String, String>) target;
            String idValue = argumentValue.get("id");
            if (idValue != null) {
                String logicalTypeName = argumentValue.get("logicalTypeName");
                Optional<Bookmark> bookmarkIfAny;
                if (logicalTypeName != null) {
                    bookmarkIfAny = Optional.of(Bookmark.forLogicalTypeNameAndIdentifier(logicalTypeName, idValue));
                } else {
                    Class<?> paramClass = objectSpec.getCorrespondingClass();
                    bookmarkIfAny = context.bookmarkService.bookmarkFor(paramClass, idValue);
                }
                result = bookmarkIfAny
                        .map(context.bookmarkService::lookup)
                        .filter(Optional::isPresent)
                        .map(Optional::get);
            } else {
                String refValue = argumentValue.get("ref");
                if (refValue != null) {
                    String key = GvqlActionUtils.keyFor(refValue);
                    BookmarkedPojo value = ((Environment) environment).getGraphQlContext().get(key);
                    result = Optional.of(value).map(BookmarkedPojo::getTargetPojo);
                } else {
                    throw new IllegalArgumentException("Either 'id' or 'ref' must be specified for a DomainObject input type");
                }
            }
            sourcePojo = result
                    .orElseThrow(); // TODO: better error handling if no such object found.
        }

        ManagedObject managedObject = ManagedObject.adaptSingular(objectSpec, sourcePojo);

        val visibleConsent = objectAction.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visibleConsent.isVetoed()) {
            throw new HiddenException(objectAction.getFeatureIdentifier());
        }

        val usableConsent = objectAction.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usableConsent.isVetoed()) {
            throw new DisabledException(objectAction.getFeatureIdentifier());
        }

        val head = objectAction.interactionHead(managedObject);
        val argumentManagedObjects = argumentManagedObjectsFor(environment, objectAction);

        val validityConsent = objectAction.isArgumentSetValid(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new IllegalArgumentException(validityConsent.getReasonAsString().orElse("Invalid"));
        }

        val resultManagedObject = objectAction.execute(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        return resultManagedObject.getPojo();
    }


    // TODO: adapted from GqlvAction - rationalize?
    private void addGqlArguments(final GraphQLFieldDefinition.Builder fieldBuilder) {

        val arguments = new ArrayList<GraphQLArgument>();
        val argName = context.causewayConfiguration.getViewer().getGraphql().getMutation().getTargetArgName();

        // add target (if not a service)
        if (! objectSpec.getBeanSort().isManagedBeanContributing()) {
            arguments.add(
                    GraphQLArgument.newArgument()
                            .name(argName)
                            .type(context.typeMapper.inputTypeFor(objectSpec, SchemaType.RICH))
                            .build()
            );
        }

        val parameters = objectAction.getParameters();
        parameters.stream()
                .map(this::gqlArgumentFor)
                .forEach(arguments::add);

        if (!arguments.isEmpty()) {
            fieldBuilder.arguments(arguments);
        }
    }

    // adapted from GqlvAction
    GraphQLArgument gqlArgumentFor(final ObjectActionParameter objectActionParameter) {
        return objectActionParameter.isPlural()
                ? gqlArgumentFor((OneToManyActionParameter) objectActionParameter)
                : gqlArgumentFor((OneToOneActionParameter) objectActionParameter);
    }

    // adapted from GqlvAction
    GraphQLArgument gqlArgumentFor(final OneToOneActionParameter oneToOneActionParameter) {
        return GraphQLArgument.newArgument()
                .name(oneToOneActionParameter.getId())
                .type(context.typeMapper.inputTypeFor(oneToOneActionParameter, TypeMapper.InputContext.INVOKE, SchemaType.RICH))
                .build();
    }

    // adapted from GqlvAction
    GraphQLArgument gqlArgumentFor(final OneToManyActionParameter oneToManyActionParameter) {
        return GraphQLArgument.newArgument()
                .name(oneToManyActionParameter.getId())
                .type(context.typeMapper.inputTypeFor(oneToManyActionParameter, SchemaType.RICH))
                .build();
    }

    private Can<ManagedObject> argumentManagedObjectsFor(
            final Environment dataFetchingEnvironment,
            final ObjectAction objectAction) {
        return GqlvAction.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context);
    }


}
