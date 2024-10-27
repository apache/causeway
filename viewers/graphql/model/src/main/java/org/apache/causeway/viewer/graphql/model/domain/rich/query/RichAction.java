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
package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.Parent;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.query.ObjectFeatureUtils;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RichAction
        extends RichMember<ObjectAction, ObjectInteractor>
        implements ActionInteractor,
                   Parent {

    private final RichMemberHidden<ObjectAction> hidden;
    private final RichMemberDisabled<ObjectAction> disabled;
    private final RichActionValidity validate;
    /**
     * Populated iff the API variant allows for it.
     */
    private final RichActionInvoke invoke;
    /**
     * Populated iif there are params for this action.
     */
    private final RichActionParams params;

    public RichAction(
            final ObjectInteractor objectInteractor,
            final ObjectAction objectAction,
            final Context context) {
        super(objectInteractor, objectAction, TypeNames.actionTypeNameFor(objectInteractor.getObjectSpecification(), objectAction, objectInteractor.getSchemaType()), context);

        if(isBuilt()) {
            this.hidden = null;
            this.disabled = null;
            this.validate = null;
            this.invoke = null;
            this.params = null;
            return;
        }
        addChildFieldFor(this.hidden = new RichMemberHidden<>(this, context));
        addChildFieldFor(this.disabled = new RichMemberDisabled<>(this, context));
        addChildFieldFor(this.validate = new RichActionValidity(this, context));

        addChildFieldFor(
                this.invoke = isInvokeAllowed(objectAction)
                    ? new RichActionInvoke(this, context)
                    : null);
        addChildFieldFor(this.params = new RichActionParams(this, context));

        buildObjectTypeAndField(objectAction.asciiId(), objectAction.getCanonicalDescription().orElse(objectAction.getCanonicalFriendlyName()));
    }

    private boolean isInvokeAllowed(ObjectAction objectAction) {
        var apiVariant = context.causewayConfiguration.getViewer().getGraphql().getApiVariant();
        switch (apiVariant) {
            case QUERY_ONLY:
            case QUERY_AND_MUTATIONS:
                return objectAction.getSemantics().isSafeInNature();
            case QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT:
                return true;
            default:
                // shouldn't happen
                throw new IllegalArgumentException("Unknown API variant: " + apiVariant);
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
            final OneToOneActionParameter oap,
            final TypeMapper.InputContext inputContext) {
        return GraphQLArgument.newArgument()
                .name(oap.asciiId())
                .type(context.typeMapper.inputTypeFor(oap, inputContext, getSchemaType()))
                .build();
    }

    GraphQLArgument gqlArgumentFor(final OneToManyActionParameter otmp) {
        return GraphQLArgument.newArgument()
                .name(otmp.asciiId())
                .type(context.typeMapper.inputTypeFor(otmp, getSchemaType()))
                .build();
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return interactor.getObjectSpecification();
    }

    @Override
    protected void addDataFetchersForChildren() {
        if(hidden == null) {
            return;
        }
        hidden.addDataFetcher(this);
        disabled.addDataFetcher(this);
        validate.addDataFetcher(this);
        if (invoke != null) {
            invoke.addDataFetcher(this);
        }
        if (params != null) {
            params.addDataFetcher(this);
        }
    }

    @Override
    public SchemaType getSchemaType() {
        return interactor.getSchemaType();
    }

}
