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

 import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
 import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
 import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionParamInteractor;
 import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.extern.log4j.Log4j2;

 @Log4j2
 public class RichActionParamsParamAutoComplete extends Element {

     private static final String SEARCH_PARAM_NAME = "search";

     private final ActionParamInteractor actionParamInteractor;

     public RichActionParamsParamAutoComplete(
             final ActionParamInteractor actionParamInteractor,
             final Context context) {
         super(context);
         this.actionParamInteractor = actionParamInteractor;

         var objectActionParameter = actionParamInteractor.getObjectActionParameter();
         if (objectActionParameter.hasAutoComplete()) {
             var elementType = objectActionParameter.getElementType();
             var fieldBuilder = newFieldDefinition()
                     .name("autoComplete")
                     .type(GraphQLList.list(context.typeMapper.outputTypeFor(elementType, actionParamInteractor.getSchemaType())));
             actionParamInteractor.addGqlArguments(actionParamInteractor.getObjectMember(), fieldBuilder, TypeMapper.InputContext.AUTOCOMPLETE, actionParamInteractor.getParamNum());
             fieldBuilder.argument(GraphQLArgument.newArgument()
                     .name(SEARCH_PARAM_NAME)
                     .type(nonNull(context.typeMapper.outputTypeFor(String.class))))
                     .build();
             setField(fieldBuilder.build());
         } else {
             setField(null);
         }
     }

     @Override
     protected List<Object> fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

         var sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);
         var objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
         if (objectSpecification == null) {
             return Collections.emptyList();
         }

         var objectAction = actionParamInteractor.getObjectMember();
         var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

         final ObjectFeature objectFeature = actionParamInteractor.getObjectActionParameter();
         var objectActionParameter = objectAction.getParameterById(objectFeature.asciiId());
         var argumentManagedObjects = actionParamInteractor.argumentManagedObjectsFor(new Environment.For(dataFetchingEnvironment), objectAction, context.bookmarkService);

         var managedAction = ManagedAction.of(managedObject, objectAction, Where.ANYWHERE);
         var pendingArgs = ParameterNegotiationModel.of(managedAction, argumentManagedObjects);
         var searchArg = dataFetchingEnvironment.<String>getArgument(SEARCH_PARAM_NAME);
         var autoCompleteManagedObjects = objectActionParameter.getAutoComplete(pendingArgs, searchArg, InteractionInitiatedBy.USER);

         return autoCompleteManagedObjects.stream()
                    .map(ManagedObject::getPojo)
                    .collect(Collectors.toList());
     }

 }
