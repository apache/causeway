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
package org.apache.causeway.viewer.graphql.model.domain.simple.mutation;

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

import org.apache.causeway.core.metamodel.spec.feature.*;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.query.ObjectFeatureUtils;
import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SimpleMutationForAction extends Element {

    private static final SchemaType SCHEMA_TYPE = SchemaType.SIMPLE;

    private final ObjectSpecification objectSpec;
    private final ObjectAction objectAction;
    private String argumentName;

    public SimpleMutationForAction(
            final ObjectSpecification objectSpec,
            final ObjectAction objectAction,
            final Context context) {
        super(context);
        this.objectSpec = objectSpec;
        this.objectAction = objectAction;

        this.argumentName = context.causewayConfiguration.getViewer().getGraphql().getMutation().getTargetArgName();

        GraphQLOutputType type = typeFor(objectAction);
        if (type != null) {
            var fieldBuilder = newFieldDefinition()
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
            final ObjectAction oa) {
        return TypeNames.objectTypeFieldNameFor(objectSpecification) + "__" + oa.asciiId();
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
                var objectSpecificationOfCollectionElement = facet.elementSpec();
                GraphQLType wrappedType = context.typeMapper.outputTypeFor(objectSpecificationOfCollectionElement, SchemaType.RICH);
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

        var isService = objectSpec.getBeanSort().isManagedBeanContributing();

        var environment = new Environment.For(dataFetchingEnvironment);
        Object sourcePojo;
        if (isService) {
            sourcePojo = context.serviceRegistry.lookupServiceElseFail(objectSpec.getCorrespondingClass());
        } else {
            Object target = dataFetchingEnvironment.getArgument(argumentName);
            Optional<Object> result;
            var argumentValue = (Map<String, ?>) target;
            var idValue = (String)argumentValue.get("id");
            if (idValue != null) {
                var objectSpecArg = (ObjectSpecification) argumentValue.get("logicalTypeName");
                Optional<Bookmark> bookmarkIfAny;
                if (objectSpecArg != null) {
                    bookmarkIfAny = Optional.of(Bookmark.forLogicalTypeNameAndIdentifier(objectSpecArg.getLogicalTypeName(), idValue));
                } else {
                    Class<?> paramClass = objectSpec.getCorrespondingClass();
                    bookmarkIfAny = context.bookmarkService.bookmarkFor(paramClass, idValue);
                }
                result = bookmarkIfAny
                        .map(context.bookmarkService::lookup)
                        .filter(Optional::isPresent)
                        .map(Optional::get);
            } else {
                var refValue = (String)argumentValue.get("ref");
                if (refValue != null) {
                    var key = ObjectFeatureUtils.keyFor(refValue);
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

        var visibleConsent = objectAction.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visibleConsent.isVetoed()) {
            throw new HiddenException(objectAction.getFeatureIdentifier());
        }

        var usableConsent = objectAction.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usableConsent.isVetoed()) {
            throw new DisabledException(objectAction.getFeatureIdentifier());
        }

        var head = objectAction.interactionHead(managedObject);
        var argumentManagedObjects = argumentManagedObjectsFor(environment, objectAction);

        var validityConsent = objectAction.isArgumentSetValid(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new IllegalArgumentException(validityConsent.getReasonAsString().orElse("Invalid"));
        }

        var resultManagedObject = objectAction.execute(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        return resultManagedObject.getPojo();
    }

    // TODO: adapted from SimpleAction - rationalize?
    private void addGqlArguments(final GraphQLFieldDefinition.Builder fieldBuilder) {

        var arguments = new ArrayList<GraphQLArgument>();
        var argName = context.causewayConfiguration.getViewer().getGraphql().getMutation().getTargetArgName();

        // add target (if not a service)
        if (! objectSpec.getBeanSort().isManagedBeanContributing()) {
            arguments.add(
                    GraphQLArgument.newArgument()
                            .name(argName)
                            .type(context.typeMapper.inputTypeFor(objectSpec, SchemaType.RICH))
                            .build()
            );
        }

        var parameters = objectAction.getParameters();
        parameters.stream()
                .map(this::gqlArgumentFor)
                .forEach(arguments::add);

        if (!arguments.isEmpty()) {
            fieldBuilder.arguments(arguments);
        }
    }

    // adapted from SimpleAction
    GraphQLArgument gqlArgumentFor(final ObjectActionParameter objectActionParameter) {
        return objectActionParameter.isPlural()
                ? gqlArgumentFor((OneToManyActionParameter) objectActionParameter)
                : gqlArgumentFor((OneToOneActionParameter) objectActionParameter);
    }

    // adapted from SimpleAction
    GraphQLArgument gqlArgumentFor(final OneToOneActionParameter otoap) {
        return GraphQLArgument.newArgument()
                .name(otoap.asciiId())
                .type(context.typeMapper.inputTypeFor(otoap, TypeMapper.InputContext.INVOKE, SchemaType.RICH))
                .build();
    }

    // adapted from SimpleAction
    GraphQLArgument gqlArgumentFor(final OneToManyActionParameter otmap) {
        return GraphQLArgument.newArgument()
                .name(otmap.asciiId())
                .type(context.typeMapper.inputTypeFor(otmap, SchemaType.RICH))
                .build();
    }

    private Can<ManagedObject> argumentManagedObjectsFor(
            final Environment dataFetchingEnvironment,
            final ObjectAction objectAction) {
        return ObjectFeatureUtils.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context);
    }

}
