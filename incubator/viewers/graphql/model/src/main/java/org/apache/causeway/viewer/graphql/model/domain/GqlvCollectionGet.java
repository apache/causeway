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

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class GqlvCollectionGet {

    private final GqlvCollectionGetHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final GraphQLFieldDefinition field;


    public GqlvCollectionGet(
            final GqlvCollectionGetHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
    }

    private static GraphQLFieldDefinition fieldDefinition(final GqlvCollectionGetHolder holder) {

        val oneToManyAssociation = holder.getOneToManyAssociation();

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = TypeMapper.listTypeForElementTypeOf(oneToManyAssociation);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name("get")
                    .type(type);
            fieldDefinition = fieldBuilder.build();

            holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    public void addDataFetcher() {

        val association = holder.getOneToManyAssociation();
        val fieldObjectSpecification = association.getElementType();
        val beanSort = fieldObjectSpecification.getBeanSort();

        switch (beanSort) {

            case VALUE:
            case VIEW_MODEL:
            case ENTITY:

                codeRegistryBuilder.dataFetcher(
                        holder.coordinatesFor(field),
                        this::get);

                break;

        }
    }

    private Object get(final DataFetchingEnvironment dataFetchingEnvironment) {

        val association = holder.getOneToManyAssociation();

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val specificationLoader = association.getSpecificationLoader();
        val objectSpecification = specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        // TODO: probably incorrect to adapt as a singular here?
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val resultManagedObject = association.get(managedObject);

        return resultManagedObject != null
                ? resultManagedObject.getPojo()
                : null;
    }

}
