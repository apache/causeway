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

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionParamInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

final class RichActionParamsParamAutoCompleteWindow extends RichAutoCompleteWindow {

    private final ActionParamInteractor actionParamInteractor;

    RichActionParamsParamAutoCompleteWindow(
            final ActionParamInteractor actionParamInteractor,
            final Context context) {
        super(
                context,
                TypeNames.actionParamAutocompleteWindowTypeNameFor(
                        actionParamInteractor.getObjectSpecification(),
                        actionParamInteractor.getObjectActionParameter(),
                        actionParamInteractor.getSchemaType()),
                context.typeMapper.outputTypeFor(
                        actionParamInteractor.getObjectActionParameter().getElementType(),
                        actionParamInteractor.getSchemaType()),
                builder -> actionParamInteractor.addGqlArguments(
                        actionParamInteractor.getObjectMember(),
                        builder,
                        TypeMapper.InputContext.AUTOCOMPLETE,
                        actionParamInteractor.getParamNum()));
        this.actionParamInteractor = actionParamInteractor;
    }

    @Override
    protected List<Object> autocompleteItems(final DataFetchingEnvironment environment) {
        var sourcePojo = BookmarkedPojo.sourceFrom(environment);
        var objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return List.of();
        }

        var objectAction = actionParamInteractor.getObjectMember();
        var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        final ObjectFeature objectFeature = actionParamInteractor.getObjectActionParameter();
        var objectActionParameter = objectAction.getParameterById(objectFeature.asciiId());
        var argumentManagedObjects = actionParamInteractor.argumentManagedObjectsFor(
                new Environment.For(environment), objectAction, context.bookmarkService);
        var managedAction = ManagedAction.of(managedObject, objectAction, Where.ANYWHERE);
        var pendingArgs = ParameterNegotiationModel.of(managedAction, argumentManagedObjects);
        var search = environment.<String>getArgument(SEARCH_ARGUMENT);
        return objectActionParameter.getAutoComplete(pendingArgs, search, InteractionInitiatedBy.USER).stream()
                .map(ManagedObject::getPojo)
                .toList();
    }
}
