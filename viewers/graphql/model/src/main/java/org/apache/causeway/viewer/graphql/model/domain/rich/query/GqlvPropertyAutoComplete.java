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

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import lombok.val;

public class GqlvPropertyAutoComplete extends GqlvAbstract {

    private static final String SEARCH_PARAM_NAME = "search";

    private final HolderPropertyAutoComplete holder;

    public GqlvPropertyAutoComplete(
            final HolderPropertyAutoComplete holder,
            final Context context) {
        super(context);
        this.holder = holder;

        val otoa = holder.getOneToOneAssociation();
        if (otoa.hasAutoComplete()) {
            val elementType = otoa.getElementType();
            val fieldBuilder = newFieldDefinition()
                    .name("autoComplete")
                    .type(GraphQLList.list(context.typeMapper.outputTypeFor(elementType, SchemaType.RICH)));
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
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

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

}
