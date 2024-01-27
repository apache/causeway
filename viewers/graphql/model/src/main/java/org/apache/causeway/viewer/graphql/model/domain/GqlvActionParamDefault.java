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

 import java.util.Collections;

 import org.apache.causeway.applib.annotation.Where;
 import org.apache.causeway.applib.services.bookmark.BookmarkService;
 import org.apache.causeway.commons.collections.Can;
 import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
 import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
 import org.apache.causeway.core.metamodel.object.ManagedObject;
 import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
 import org.apache.causeway.viewer.graphql.model.context.Context;
 import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
 import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionParameterProvider;
 import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
 import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
 import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

 import lombok.val;
 import lombok.extern.log4j.Log4j2;

 import graphql.schema.DataFetchingEnvironment;
 import graphql.schema.GraphQLFieldDefinition;

 import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

 @Log4j2
 public class GqlvActionParamDefault {

     private final Holder holder;
     private final Context context;

     /**
      * Populated iff there are choices for this parameter.
      */
     private final GraphQLFieldDefinition field;

     public GqlvActionParamDefault(
             final Holder holder,
             final Context context) {
         this.holder = holder;
         this.context = context;

         val objectActionParameter = holder.getObjectActionParameter();

         if (objectActionParameter.hasDefaults()) {
             val elementType = objectActionParameter.getElementType();
             val fieldBuilder = newFieldDefinition()
                     .name("default")
                     .type(context.typeMapper.outputTypeFor(elementType));
             holder.addGqlArguments(holder.getObjectAction(), fieldBuilder, TypeMapper.InputContext.DEFAULT, holder.getParamNum());
             this.field = holder.addField(fieldBuilder.build());
         } else {
             this.field = null;
         }
     }

     boolean hasDefault() {
         return field != null;
     }

     public void addDataFetcher() {
         context.codeRegistryBuilder.dataFetcher(
                 holder.coordinatesFor(field),
                 this::default_
         );
     }

     private Object default_(final DataFetchingEnvironment dataFetchingEnvironment) {

         val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);
         val objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
         if (objectSpecification == null) {
             return Collections.emptyList();
         }

         val objectAction = holder.getObjectAction();
         val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

         val objectActionParameter = objectAction.getParameterById(holder.getObjectActionParameter().getId());
         val argumentManagedObjects = holder.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context.bookmarkService);

         val managedAction = ManagedAction.of(managedObject, objectAction, Where.ANYWHERE);
         val pendingArgs = ParameterNegotiationModel.of(managedAction, argumentManagedObjects);
         val defaultManagedObject = objectActionParameter.getDefault(pendingArgs);

         return defaultManagedObject.getPojo();
     }

     public interface Holder
             extends GqlvHolder,
             ObjectSpecificationProvider,
             ObjectActionProvider,
             ObjectActionParameterProvider {
         GqlvActionParam.Holder getHolder();

         void addGqlArguments(
                 ObjectAction objectAction,
                 GraphQLFieldDefinition.Builder fieldBuilder,
                 TypeMapper.InputContext inputContext,
                 int paramNum);

         Can<ManagedObject> argumentManagedObjectsFor(
                 DataFetchingEnvironment dataFetchingEnvironment,
                 ObjectAction objectAction,
                 BookmarkService bookmarkService);
     }
 }
