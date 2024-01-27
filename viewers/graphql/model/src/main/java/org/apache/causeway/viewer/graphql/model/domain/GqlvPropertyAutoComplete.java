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
import static graphql.schema.GraphQLNonNull.nonNull;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.OneToOneAssociationProvider;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import lombok.val;

public class GqlvPropertyAutoComplete {

    private static final String SEARCH_PARAM_NAME = "search";

    private final Holder holder;
    private final Context context;
    /**
     * Populated iff there are choices for this property
     */
    final GraphQLFieldDefinition field;

    public GqlvPropertyAutoComplete(
            final Holder holder,
            final Context context) {
        this.holder = holder;
        this.context = context;

        val otoa = holder.getOneToOneAssociation();
        if (otoa.hasAutoComplete()) {
            val elementType = otoa.getElementType();
            val fieldBuilder = newFieldDefinition()
                    .name("autoComplete")
                    .type(GraphQLList.list(context.typeMapper.outputTypeFor(elementType)));
            fieldBuilder.argument(GraphQLArgument.newArgument()
                            .name(SEARCH_PARAM_NAME)
                            .type(nonNull(context.typeMapper.scalarTypeFor(String.class))))
                    .build();
            this.field = holder.addField(fieldBuilder.build());
        } else {
            this.field = null;
        }
    }

    boolean hasAutoComplete() {
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
                        this::autoComplete);

                break;
        }
    }

    List<Object> autoComplete(final DataFetchingEnvironment dataFetchingEnvironment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return null;
        }

        val association = holder.getOneToOneAssociation();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        val searchArg = dataFetchingEnvironment.<String>getArgument(SEARCH_PARAM_NAME);
        val autoCompleteManagedObjects = association.getAutoComplete(managedObject, searchArg, InteractionInitiatedBy.USER);

        return autoCompleteManagedObjects.stream()
                .map(ManagedObject::getPojo)
                .collect(Collectors.toList());
    }

    public interface Holder
            extends GqlvHolder,
            ObjectSpecificationProvider,
            OneToOneAssociationProvider {

    }
}
