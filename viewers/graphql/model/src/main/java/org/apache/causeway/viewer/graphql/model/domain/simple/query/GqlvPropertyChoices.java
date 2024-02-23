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
package org.apache.causeway.viewer.graphql.model.domain.simple.query;

import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.PropertyInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

public class GqlvPropertyChoices extends GqlvAbstract {

    final PropertyInteractor propertyInteractor;

    public GqlvPropertyChoices(
            final PropertyInteractor propertyInteractor,
            final Context context) {
        super(context);
        this.propertyInteractor = propertyInteractor;

        val otoa = propertyInteractor.getObjectMember();
        if (otoa.hasChoices()) {
            val elementType = otoa.getElementType();
            val fieldBuilder = newFieldDefinition()
                    .name("choices")
                    .type(GraphQLList.list(context.typeMapper.outputTypeFor(elementType, SchemaType.RICH)));
            propertyInteractor.addGqlArgument(otoa, fieldBuilder, TypeMapper.InputContext.CHOICES);
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

        val association = propertyInteractor.getObjectMember();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        val choicesManagedObject = association.getChoices(managedObject, InteractionInitiatedBy.USER);
        return choicesManagedObject.stream()
                    .map(ManagedObject::getPojo)
                    .collect(Collectors.toList());
    }

}
