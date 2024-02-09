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

import java.util.List;
import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.OneToOneAssociationProvider;

import lombok.val;

public class GqlvPropertyChoices {

    final Holder holder;
    private final Context context;
    /**
     * Populated iff there are choices for this property
     */
    final GraphQLFieldDefinition field;

    public GqlvPropertyChoices(
            final Holder holder,
            final Context context) {
        this.holder = holder;
        this.context = context;

        val otoa = holder.getOneToOneAssociation();
        if (otoa.hasChoices()) {
            val elementType = otoa.getElementType();
            val fieldBuilder = newFieldDefinition()
                    .name("choices")
                    .type(GraphQLList.list(context.typeMapper.outputTypeFor(elementType)));
            holder.addGqlArgument(otoa, fieldBuilder, TypeMapper.InputContext.CHOICES);
            this.field = holder.addField(fieldBuilder.build());
        } else {
            this.field = null;
        }
    }

    boolean hasChoices() {
        return this.field != null;
    }

    void addDataFetcher() {

        val association = holder.getOneToOneAssociation();
        val fieldObjectSpecification = association.getElementType();
        val beanSort = fieldObjectSpecification.getBeanSort();

        switch (beanSort) {
            case VALUE:
            case VIEW_MODEL:
            case ENTITY:
                context.codeRegistryBuilder.dataFetcher(
                        holder.coordinatesFor(field),
                        this::choices);

                break;
        }
    }

    List<Object> choices(final DataFetchingEnvironment dataFetchingEnvironment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return null;
        }

        val association = holder.getOneToOneAssociation();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        val choicesManagedObject = association.getChoices(managedObject, InteractionInitiatedBy.USER);
        return choicesManagedObject.stream()
                    .map(ManagedObject::getPojo)
                    .collect(Collectors.toList());
    }

    public interface Holder
            extends GqlvHolder,
            ObjectSpecificationProvider,
            OneToOneAssociationProvider {

        void addGqlArgument(OneToOneAssociation otoa, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext);
    }
}
