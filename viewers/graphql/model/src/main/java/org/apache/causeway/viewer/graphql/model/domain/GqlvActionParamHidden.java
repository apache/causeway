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

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionParameterProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.applib.types.TypeMapper;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionParamHidden {

    private final Holder holder;
    private final Context context;

    private final GraphQLFieldDefinition field;

    public GqlvActionParamHidden(
            final Holder holder,
            final Context context) {
        this.holder = holder;
        this.context = context;

        GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                .name("hidden")
                .type(context.typeMapper.scalarTypeFor(boolean.class));
        holder.addGqlArguments(holder.getObjectAction(), fieldBuilder, TypeMapper.InputContext.DISABLE, holder.getParamNum());
        this.field = holder.addField(fieldBuilder.build());
    }


    public void addDataFetcher() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                this::hidden
        );
    }

    private boolean hidden(final DataFetchingEnvironment dataFetchingEnvironment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            return true;
        }

        val objectAction = holder.getObjectAction();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val actionInteractionHead = objectAction.interactionHead(managedObject);

        val objectActionParameter = objectAction.getParameterById(holder.getObjectActionParameter().getId());

        val argumentManagedObjects = holder.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context.bookmarkService);

        val visible = objectActionParameter.isVisible(actionInteractionHead, argumentManagedObjects, InteractionInitiatedBy.USER);
        return visible.isVetoed();
    }

    public interface Holder
            extends GqlvHolder,
                    ObjectSpecificationProvider,
                    ObjectActionProvider,
                    ObjectActionParameterProvider {
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
