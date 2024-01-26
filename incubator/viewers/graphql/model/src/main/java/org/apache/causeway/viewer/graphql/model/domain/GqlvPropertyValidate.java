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

import java.util.Map;

import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.OneToOneAssociationProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class GqlvPropertyValidate {

    final Holder holder;
    private final Context context;
    final GraphQLFieldDefinition field;

    public GqlvPropertyValidate(
            final Holder holder,
            final Context context) {
        this.holder = holder;
        this.context = context;
        this.field = fieldDefinition(holder);
    }

    GraphQLFieldDefinition fieldDefinition(final Holder holder) {

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = outputTypeFor();
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name("validate")
                    .type(type);
            final OneToOneAssociation oneToOneAssociation = holder.getOneToOneAssociation();

            GqlvProperty.addGqlArgument(oneToOneAssociation, fieldBuilder, TypeMapper.InputContext.VALIDATE);
            fieldDefinition = fieldBuilder.build();

            holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    GraphQLOutputType outputTypeFor() {
        return TypeMapper.scalarTypeFor(String.class);
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
                        this::validate);

                break;
        }
    }

    Object validate(final DataFetchingEnvironment dataFetchingEnvironment) {


        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        val association = holder.getOneToOneAssociation();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
        Object argumentValue = arguments.get(association.getId());
        ManagedObject argumentManagedObject = ManagedObject.adaptProperty(association, argumentValue);
        Consent consent = association.isAssociationValid(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);
        return consent.isVetoed() ? consent.getReasonAsString().orElse("invalid") : null;
    }

    public interface Holder
            extends GqlvHolder,
            ObjectSpecificationProvider,
            OneToOneAssociationProvider {

        GqlvProperty.Holder getHolder();
    }
}
