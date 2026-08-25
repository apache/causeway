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

import java.util.stream.Collectors;

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
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    private boolean isInvokeAllowed(final ObjectAction objectAction) {
        var apiVariant = context.causewayConfiguration.viewer().graphql().apiVariant();
        return switch (apiVariant) {
		case QUERY_ONLY, QUERY_AND_MUTATIONS -> objectAction.getSemantics().isSafeInNature();
		case QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT -> true;
		default -> // shouldn't happen
		throw new IllegalArgumentException("Unknown API variant: " + apiVariant);
		};
    }

    @Override
    public Can<ManagedObject> argumentManagedObjectsFor(
            final Environment dataFetchingEnvironment,
            final ObjectAction objectAction,
            final BookmarkService bookmarkService) {

        return argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context);
    }

    public static Can<ManagedObject> argumentManagedObjectsFor(
            final Environment environment,
            final ObjectAction objectAction,
            final Context context) {
        return ObjectFeatureUtils.argumentManagedObjectsFor(environment, objectAction, context);
    }

    @Override
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
        addMemberMetadataDataFetchers();
        if(hidden == null)
			return;
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
