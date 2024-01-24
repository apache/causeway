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
import java.util.stream.Collectors;

import org.apache.causeway.core.metamodel.consent.Consent;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Log4j2
public class GqlvActionValidate {

    private final GqlvActionValidateHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final GraphQLFieldDefinition field;

    public GqlvActionValidate(
            final GqlvActionValidateHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
    }

    private static GraphQLFieldDefinition fieldDefinition(final GqlvActionValidateHolder holder) {

        val objectAction = holder.getObjectAction();

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = TypeMapper.scalarTypeFor(String.class);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name("validate")
                    .type(type);
            addGqlArguments(objectAction, fieldBuilder);
            fieldDefinition = fieldBuilder.build();

            holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    static void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder builder) {

        GqlvActionInvoke.addGqlArguments(objectAction, builder);
    }

    public void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                this::validate
        );
    }

    private Object validate(final DataFetchingEnvironment dataFetchingEnvironment) {

        final ObjectAction objectAction = holder.getObjectAction();

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val specificationLoader = objectAction.getSpecificationLoader();
        val objectSpecification = specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val actionInteractionHead = objectAction.interactionHead(managedObject);

        Map<String, Object> argumentPojos = dataFetchingEnvironment.getArguments();
        Can<ObjectActionParameter> parameters = objectAction.getParameters();
        Can<ManagedObject> argumentManagedObjects = parameters
                .map(oap -> {
                    Object argumentValue = argumentPojos.get(oap.getId());
                    return ManagedObject.adaptParameter(oap, argumentValue);
                });

        Consent consent = objectAction.isArgumentSetValid(actionInteractionHead, argumentManagedObjects, InteractionInitiatedBy.USER);

        return consent.isVetoed() ? consent.getReasonAsString().orElse("Invalid") : null;
    }

}
