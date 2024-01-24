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
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class GqlvPropertyValidate {

    final GqlvPropertyValidateHolder holder;
    final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    final SpecificationLoader specificationLoader;
    final GraphQLFieldDefinition field;

    public GqlvPropertyValidate(
            final GqlvPropertyValidateHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final SpecificationLoader specificationLoader) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
        this.specificationLoader = specificationLoader;
    }

    GraphQLFieldDefinition fieldDefinition(final GqlvPropertyValidateHolder holder) {

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = outputTypeFor(holder);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name("validate")
                    .type(type);
            addGqlArgument(holder.getOneToOneAssociation(), fieldBuilder);
            fieldDefinition = fieldBuilder.build();

            holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    GraphQLOutputType outputTypeFor(GqlvPropertyValidateHolder holder) {
        return TypeMapper.scalarTypeFor(String.class);
    }

    static void addGqlArgument(
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLFieldDefinition.Builder builder) {

        builder.argument(gqlArgumentFor(oneToOneAssociation));
    }

    private static GraphQLArgument gqlArgumentFor(final OneToOneAssociation oneToOneAssociation) {
        return GraphQLArgument.newArgument()
                .name(oneToOneAssociation.getId())
                .type(TypeMapper.inputTypeFor(oneToOneAssociation))
                .build();
    }


    void addDataFetcher() {

        val association = holder.getOneToOneAssociation();
        val fieldObjectSpecification = association.getElementType();
        val beanSort = fieldObjectSpecification.getBeanSort();

        switch (beanSort) {
            case VALUE:
            case VIEW_MODEL:
            case ENTITY:
                codeRegistryBuilder.dataFetcher(
                        holder.coordinatesFor(field),
                        this::validate);

                break;
        }
    }

    Object validate(final DataFetchingEnvironment dataFetchingEnvironment) {

        val association = holder.getOneToOneAssociation();

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val objectSpecification = specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
        Object argumentValue = arguments.get(association.getId());
        ManagedObject argumentManagedObject = ManagedObject.adaptProperty(association, argumentValue);
        Consent associationValid = association.isAssociationValid(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);

        return associationValid.isVetoed() ? associationValid.getReasonAsString().orElse("invalid") : null;
    }

}
