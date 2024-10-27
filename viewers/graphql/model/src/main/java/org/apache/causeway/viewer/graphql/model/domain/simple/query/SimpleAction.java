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
package org.apache.causeway.viewer.graphql.model.domain.simple.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.*;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.query.ObjectFeatureUtils;
import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.Getter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SimpleAction
        extends Element {

    @Getter final ObjectInteractor objectInteractor;
    @Getter private final ObjectAction objectMember;

    public SimpleAction(
            final ObjectInteractor objectInteractor,
            final ObjectAction objectAction,
            final Context context) {
        super(context);

        this.objectInteractor = objectInteractor;
        this.objectMember = objectAction;

        var graphQLOutputType = typeFor(objectAction);

        var fieldBuilder = newFieldDefinition()
                .name(getId())
                .description(objectAction.getCanonicalDescription().orElse(objectAction.getCanonicalFriendlyName()))
                .type(graphQLOutputType);
        addGqlArguments(objectAction, fieldBuilder, TypeMapper.InputContext.INVOKE, objectAction.getParameterCount());

        setField(fieldBuilder.build());
    }

    public String getId() {
        return objectMember.asciiId();
    }

    private GraphQLOutputType typeFor(final ObjectAction objectAction){

        var objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                if (facet == null) {
                    log.warn("Unable to locate TypeOfFacet for {}", objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                var objectSpecificationOfCollectionElement = facet.elementSpec();
                GraphQLType wrappedType = context.typeMapper.outputTypeFor(objectSpecificationOfCollectionElement, objectInteractor.getSchemaType());
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
                return context.typeMapper.outputTypeFor(objectSpecification, objectInteractor.getSchemaType());
        }
    }

    public Can<ManagedObject> argumentManagedObjectsFor(
            final Environment dataFetchingEnvironment,
            final ObjectAction objectAction,
            final BookmarkService bookmarkService) {

        return argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context);
    }

    /**
     *
     * @param environment
     * @param objectAction
     * @param context
     * @return
     */
    public static Can<ManagedObject> argumentManagedObjectsFor(
            final Environment environment,
            final ObjectAction objectAction,
            final Context context) {
        Map<String, Object> argumentPojos = environment.getArguments();
        Can<ObjectActionParameter> parameters = objectAction.getParameters();
        return parameters
                .map(oap -> {
                    final ObjectSpecification elementType = oap.getElementType();
                    Object argumentValue = argumentPojos.get(oap.asciiId());
                    Object pojoOrPojoList;

                    switch (elementType.getBeanSort()) {

                        case VALUE:
                            return adaptValue(oap, argumentValue, context);

                        case ENTITY:
                        case VIEW_MODEL:
                            if (argumentValue == null) {
                                return ManagedObject.empty(elementType);
                            }
                            // fall through

                        case ABSTRACT:
                            // if the parameter is abstract, we still attempt to figure out the arguments.
                            // the arguments will need to either use 'ref' or else both 'id' AND 'logicalTypeName'
                            if (argumentValue instanceof List) {
                                var argumentValueList = (List<Object>) argumentValue;
                                pojoOrPojoList = argumentValueList.stream()
                                        .map(value -> asPojo(oap.getElementType(), value, environment, context))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList());
                            } else {
                                pojoOrPojoList = asPojo(oap.getElementType(), argumentValue, environment, context).orElse(null);
                            }
                            return ManagedObject.adaptParameter(oap, pojoOrPojoList);

                        case COLLECTION:
                        case MANAGED_BEAN_CONTRIBUTING:
                        case VETOED:
                        case MANAGED_BEAN_NOT_CONTRIBUTING:
                        case MIXIN:
                        case UNKNOWN:
                        default:
                            throw new IllegalArgumentException(String.format(
                                    "Cannot handle an input type for %s; beanSort is %s", elementType.getFullIdentifier(), elementType.getBeanSort()));
                    }
                });
    }

    private static ManagedObject adaptValue(
            final ObjectActionParameter oap,
            final Object argumentValue,
            final Context context) {

        var elementType = oap.getElementType();
        if (argumentValue == null) {
            return ManagedObject.empty(elementType);
        }

        var argPojo = context.typeMapper.unmarshal(argumentValue, elementType);
        return ManagedObject.adaptParameter(oap, argPojo);
    }

    public static Optional<Object> asPojo(
            final ObjectSpecification elementType,
            final Object argumentValueObj,
            final Environment environment,
            final Context context
    ) {
        var argumentValue = (Map<String, ?>) argumentValueObj;

        var refValue = (String)argumentValue.get("ref");
        if (refValue != null) {
            String key = ObjectFeatureUtils.keyFor(refValue);
            BookmarkedPojo bookmarkedPojo = environment.getGraphQlContext().get(key);
            if (bookmarkedPojo == null) {
                throw new IllegalArgumentException(String.format(
                    "Could not find object referenced '%s' in the execution context; was it saved previously using \"saveAs\" ?", refValue));
            }
            var targetPojoClass = bookmarkedPojo.getTargetPojo().getClass();
            var targetPojoSpec = context.specificationLoader.loadSpecification(targetPojoClass);
            if (targetPojoSpec == null) {
                throw new IllegalArgumentException(String.format(
                    "The object referenced '%s' is not part of the metamodel (has class '%s')",
                    refValue, targetPojoClass.getCanonicalName()));
            }
            if (!elementType.isPojoCompatible(bookmarkedPojo.getTargetPojo())) {
                throw new IllegalArgumentException(String.format(
                    "The object referenced '%s' has a type '%s' that is not assignable to the required type '%s'",
                    refValue, targetPojoSpec.getLogicalTypeName(), elementType.getLogicalTypeName()));
            }
            return Optional.of(bookmarkedPojo).map(BookmarkedPojo::getTargetPojo);
        }

        var idValue = (String)argumentValue.get("id");
        if (idValue != null) {
            Class<?> paramClass = elementType.getCorrespondingClass();
            Optional<Bookmark> bookmarkIfAny;
            if(elementType.isAbstract()) {
                var objectSpecArg = (ObjectSpecification)argumentValue.get("logicalTypeName");
                if (objectSpecArg == null) {
                    throw new IllegalArgumentException(String.format(
                            "The 'logicalTypeName' is required along with the 'id', because the input type '%s' is abstract",
                            elementType.getLogicalTypeName()));
                }
                 bookmarkIfAny = Optional.of(Bookmark.forLogicalTypeNameAndIdentifier(objectSpecArg.getLogicalTypeName(), idValue));
            } else {
                bookmarkIfAny = context.bookmarkService.bookmarkFor(paramClass, idValue);
            }
            return bookmarkIfAny
                    .map(context.bookmarkService::lookup)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        }
        throw new IllegalArgumentException("Either 'id' or 'ref' must be specified for a DomainObject input type");
    }

    public void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder builder,
            final TypeMapper.InputContext inputContext,
            final int upTo) {

        var parameters = objectAction.getParameters();
        var arguments = parameters.stream()
                .limit(upTo)
                .map(objectActionParameter -> gqlArgumentFor(objectActionParameter, inputContext))
                .collect(Collectors.toList());
        if (!arguments.isEmpty()) {
            builder.arguments(arguments);
        }
    }

    GraphQLArgument gqlArgumentFor(
            final ObjectActionParameter objectActionParameter,
            final TypeMapper.InputContext inputContext) {
        return objectActionParameter.isPlural()
                ? gqlArgumentFor((OneToManyActionParameter) objectActionParameter)
                : gqlArgumentFor((OneToOneActionParameter) objectActionParameter, inputContext);
    }

    GraphQLArgument gqlArgumentFor(
            final OneToOneActionParameter otoap,
            final TypeMapper.InputContext inputContext) {
        return GraphQLArgument.newArgument()
                .name(otoap.asciiId())
                .type(context.typeMapper.inputTypeFor(otoap, inputContext, objectInteractor.getSchemaType()))
                .build();
    }

    GraphQLArgument gqlArgumentFor(final OneToManyActionParameter otmap) {
        return GraphQLArgument.newArgument()
                .name(otmap.asciiId())
                .type(context.typeMapper.inputTypeFor(otmap, objectInteractor.getSchemaType()))
                .build();
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        var sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        var environment = new Environment.For(dataFetchingEnvironment);

        var objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return null;
        }

        var objectAction = getObjectMember();
        var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        var visibleConsent = objectAction.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visibleConsent.isVetoed()) {
            throw new HiddenException(objectAction.getFeatureIdentifier());
        }

        var usableConsent = objectAction.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usableConsent.isVetoed()) {
            throw new DisabledException(objectAction.getFeatureIdentifier());
        }

        var head = objectAction.interactionHead(managedObject);
        var argumentManagedObjects = argumentManagedObjectsFor(environment, objectAction, context.bookmarkService);

        var validityConsent = objectAction.isArgumentSetValid(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new IllegalArgumentException(validityConsent.getReasonAsString().orElse("Invalid"));
        }

        var resultManagedObject = objectAction.execute(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        return resultManagedObject.getPojo();
    }

}
