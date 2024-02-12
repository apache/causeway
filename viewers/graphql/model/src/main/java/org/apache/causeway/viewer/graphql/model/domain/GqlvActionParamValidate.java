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

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionParameterProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionParamValidate extends GqlvAbstract {

    private final Holder holder;

    public GqlvActionParamValidate(
            final Holder holder,
            final Context context) {
        super(context);
        this.holder = holder;

        val fieldBuilder = newFieldDefinition()
                .name("validity")
                .type(context.typeMapper.scalarTypeFor(String.class));
        holder.addGqlArgument(holder.getObjectAction(), fieldBuilder, TypeMapper.InputContext.DISABLE, holder.getParamNum());
        setField(fieldBuilder.build());
    }


    public void addDataFetcher(Parent parent) {
        context.codeRegistryBuilder.dataFetcher(
                parent.coordinatesFor(getField()),
                this::fetchData
        );
    }

    private String fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            return "Invalid";
        }

        val objectAction = holder.getObjectAction();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val actionInteractionHead = objectAction.interactionHead(managedObject);

        val objectActionParameter = objectAction.getParameterById(holder.getObjectActionParameter().getId());

        val argumentManagedObjects = holder.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context.bookmarkService);

        val usable = objectActionParameter.isUsable(actionInteractionHead, argumentManagedObjects, InteractionInitiatedBy.USER);
        return usable.isVetoed() ? usable.getReasonAsString().orElse("Invalid") : null;
    }

    public interface Holder
            extends ObjectSpecificationProvider,
                    ObjectActionProvider,
                    ObjectActionParameterProvider {
        void addGqlArgument(ObjectAction objectAction, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext, int paramNum);

        Can<ManagedObject> argumentManagedObjectsFor(
                DataFetchingEnvironment dataFetchingEnvironment,
                ObjectAction objectAction,
                BookmarkService bookmarkService);
    }
}
