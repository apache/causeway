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
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public class RichPropertyAutoComplete extends Element {

    private static final String SEARCH_PARAM_NAME = "search";

    private final MemberInteractor<OneToOneAssociation> memberInteractor;

    public RichPropertyAutoComplete(
            final MemberInteractor<OneToOneAssociation> memberInteractor,
            final Context context) {
        super(context);
        this.memberInteractor = memberInteractor;

        var otoa = memberInteractor.getObjectMember();
        if (otoa.hasAutoComplete()) {
            var elementType = otoa.getElementType();
            var fieldBuilder = newFieldDefinition()
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

        var sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        var objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return null;
        }

        var association = memberInteractor.getObjectMember();
        var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        var searchArg = dataFetchingEnvironment.<String>getArgument(SEARCH_PARAM_NAME);
        var autoCompleteManagedObjects = association.getAutoComplete(managedObject, searchArg, InteractionInitiatedBy.USER);

        return autoCompleteManagedObjects.stream()
                .map(ManagedObject::getPojo)
                .collect(Collectors.toList());
    }

}
