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
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.types.ScalarMapper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Log4j2
public class GqlvActionDisabled {

    private final GqlvActionHiddenHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final GraphQLFieldDefinition field;

    public GqlvActionDisabled(
            final GqlvActionHiddenHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
    }

    private static GraphQLFieldDefinition fieldDefinition(final GqlvActionHiddenHolder holder) {

        GraphQLFieldDefinition fieldDefinition =
                newFieldDefinition()
                    .name("disabled")
                    .type(ScalarMapper.typeFor(String.class))
                    .build();

        holder.addField(fieldDefinition);
        return fieldDefinition;
    }

    public void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                this::disabled
        );
    }

    private String disabled(
            final DataFetchingEnvironment dataFetchingEnvironment) {

        final ObjectAction objectAction = holder.getObjectAction();

        Object source = dataFetchingEnvironment.getSource();
        Object domainObjectInstance;
        if (source instanceof BookmarkedPojo) {
            BookmarkedPojo fetched = (BookmarkedPojo) source;
            domainObjectInstance = fetched.getTargetPojo();
        } else {
            domainObjectInstance = source;
        }

        Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
        ObjectSpecification specification = holder.getObjectAction().getSpecificationLoader()
                .loadSpecification(domainObjectInstanceClass);
        if (specification == null) {
            // not expected
            return String.format("Disabled; could not determine target object's type ('%s')", domainObjectInstanceClass.getName());
        }

        ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);

        val usable = objectAction.isUsable(owner, InteractionInitiatedBy.USER, Where.ANYWHERE);
        return usable.getReasonAsString().orElse(null);
    }

}
