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

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.graphql.model.types.ScalarMapper;

import lombok.extern.log4j.Log4j2;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Log4j2
public class GqlvMemberHidden {

    private final GqlvMemberHiddenHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final GraphQLFieldDefinition field;

    public GqlvMemberHidden(
            final GqlvMemberHiddenHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
    }

    private static GraphQLFieldDefinition fieldDefinition(final GqlvMemberHiddenHolder holder) {

        GraphQLFieldDefinition fieldDefinition =
                newFieldDefinition()
                    .name("hidden")
                    .type(ScalarMapper.typeFor(boolean.class))
                    .build();

        holder.addField(fieldDefinition);
        return fieldDefinition;
    }

    public void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                this::hidden
        );
    }

    private boolean hidden(
            final DataFetchingEnvironment dataFetchingEnvironment) {

        val objectMember = holder.getObjectMember();

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val specificationLoader = holder.getObjectMember().getSpecificationLoader();
        val objectSpecification = specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return true;
        }

        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        val visibleConsent = objectMember.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        return visibleConsent.isVetoed();
    }

}
