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

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.*;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Log4j2
public class GqlvAction
        extends GqlvMember<ObjectAction, GqlvAction.Holder>
        implements GqlvMemberHidden.Holder,
                   GqlvMemberDisabled.Holder,
                   GqlvActionInvoke.Holder,
                   GqlvActionValidate.Holder,
                   GqlvActionParams.Holder {

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;

    private final GqlvMemberHidden hidden;
    private final GqlvMemberDisabled disabled;
    private final GqlvActionValidate validate;
    private final GqlvActionInvoke invoke;
    /**
     * Populated iif there are params for this action.
     */
    private final GqlvActionParams params;

    public GqlvAction(
            final Holder holder,
            final ObjectAction objectAction,
            final Context context
            ) {
        super(holder, objectAction, context);

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.actionTypeNameFor(holder.getObjectSpecification(), objectAction));

        this.hidden = new GqlvMemberHidden(this, context);
        this.disabled = new GqlvMemberDisabled(this, context);
        this.validate = new GqlvActionValidate(this, context);
        this.invoke = new GqlvActionInvoke(this, context);
        val params = new GqlvActionParams(this, context);
        this.params = params.hasParams() ? params : null;

        this.gqlObjectType = gqlObjectTypeBuilder.build();

        this.field = holder.addField(newFieldDefinition()
                .name(objectAction.getId())
                .type(gqlObjectTypeBuilder)
                .build());
    }

    static Can<ManagedObject> argumentManagedObjectsFor(
            final DataFetchingEnvironment dataFetchingEnvironment,
            final ObjectAction objectAction,
            final BookmarkService bookmarkService) {

        Map<String, Object> argumentPojos = dataFetchingEnvironment.getArguments();
        Can<ObjectActionParameter> parameters = objectAction.getParameters();
        return parameters
                .map(oap -> {
                    final ObjectSpecification elementType = oap.getElementType();
                    Object argumentValue = argumentPojos.get(oap.getId());
                    switch (elementType.getBeanSort()) {

                        case VALUE:
                            return ManagedObject.adaptParameter(oap, argumentValue);

                        case ENTITY:
                        case VIEW_MODEL:
                            //noinspection unchecked
                            if (argumentValue == null) {
                                return ManagedObject.empty(elementType);
                            }
                            String idValue = ((Map<String, String>) argumentValue).get("id");
                            Class<?> paramClass = elementType.getCorrespondingClass();
                            Optional<Bookmark> bookmarkIfAny = bookmarkService.bookmarkFor(paramClass, idValue);
                            return bookmarkIfAny
                                    .map(bookmarkService::lookup)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .map(pojo -> ManagedObject.adaptParameter(oap, pojo))
                                    .orElse(ManagedObject.empty(elementType));

                        case ABSTRACT:
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

    static void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder builder,
            final TypeMapper.InputContext inputContext) {

        Can<ObjectActionParameter> parameters = objectAction.getParameters();

        if (parameters.isNotEmpty()) {
            builder.arguments(parameters.stream()
                    .map(OneToOneActionParameter.class::cast)   // we previously filter to ignore any actions that have collection parameters
                    .map(oneToOneActionParameter -> gqlArgumentFor(oneToOneActionParameter, inputContext))
                    .collect(Collectors.toList()));
        }
    }

    static GraphQLArgument gqlArgumentFor(
            final OneToOneActionParameter oneToOneActionParameter,
            final TypeMapper.InputContext inputContext) {
        return GraphQLArgument.newArgument()
                .name(oneToOneActionParameter.getId())
                .type(TypeMapper.inputTypeFor(oneToOneActionParameter, inputContext))
                .build();
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    @Override
    public ObjectAction getObjectAction() {
        return getObjectMember();
    }

    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }

    public void addDataFetcher() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                new BookmarkedPojoFetcher(context.bookmarkService));

        hidden.addDataFetcher();
        disabled.addDataFetcher();
        validate.addDataFetcher();
        invoke.addDataFetcher();
        if (params != null) {
            params.addDataFetcher();
        }
    }


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }

    public interface Holder extends GqlvMember.Holder {
    }
}
