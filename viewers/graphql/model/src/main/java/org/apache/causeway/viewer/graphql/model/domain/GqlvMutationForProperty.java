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

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.exceptions.InvalidException;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

//@Log4j2
public class GqlvMutationForProperty extends GqlvAbstract {

    private final ObjectSpecification objectSpec;
    private final OneToOneAssociation oneToOneAssociation;
    private String argumentName;

    public GqlvMutationForProperty(
            final ObjectSpecification objectSpec,
            final OneToOneAssociation oneToOneAssociation,
            final Context context) {
        super(context);
        this.objectSpec = objectSpec;
        this.oneToOneAssociation = oneToOneAssociation;

        this.argumentName = context.causewayConfiguration.getViewer().getGraphql().getMutation().getTargetArgName();

        GraphQLOutputType type = context.typeMapper.outputTypeFor(objectSpec);  // setter returns void, so will return target instead.
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name(fieldName(objectSpec, oneToOneAssociation))
                    .type(type);
            addGqlArguments(fieldBuilder);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    private static String fieldName(
            final ObjectSpecification objectSpecification,
            final OneToOneAssociation oneToOneAssociation) {
        return TypeNames.objectTypeNameFor(objectSpecification) + "__" + oneToOneAssociation.getId();
    }


    public void addDataFetcher(Parent parent) {

        val beanSort = oneToOneAssociation.getElementType().getBeanSort();

        switch (beanSort) {
            case VALUE:
            case VIEW_MODEL:
            case ENTITY:
                context.codeRegistryBuilder.dataFetcher(
                        parent.coordinatesFor(getField()),
                        this::fetchData);

                break;
        }
    }

    private Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {


        Object target = dataFetchingEnvironment.getArgument(argumentName);
        Object sourcePojo = GqlvAction.asPojo(objectSpec, target, context.bookmarkService)
                    .orElseThrow(); // TODO: better error handling if no such object found.

        val managedObject = ManagedObject.adaptSingular(objectSpec, sourcePojo);

        Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
        Object argumentValue = arguments.get(oneToOneAssociation.getId());
        ManagedObject argumentManagedObject = ManagedObject.adaptProperty(oneToOneAssociation, argumentValue);

        val visibleConsent = oneToOneAssociation.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visibleConsent.isVetoed()) {
            throw new HiddenException(oneToOneAssociation.getFeatureIdentifier());
        }

        val usableConsent = oneToOneAssociation.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usableConsent.isVetoed()) {
            throw new DisabledException(oneToOneAssociation.getFeatureIdentifier());
        }

        val validityConsent = oneToOneAssociation.isAssociationValid(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new InvalidException(validityConsent);
        }

        oneToOneAssociation.set(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);

        return managedObject; // return the original object because setters return void
    }


    private void addGqlArguments(final GraphQLFieldDefinition.Builder fieldBuilder) {

        // add target
        val targetArgName = context.causewayConfiguration.getViewer().getGraphql().getMutation().getTargetArgName();
        fieldBuilder.argument(
                GraphQLArgument.newArgument()
                        .name(targetArgName)
                        .type(context.typeMapper.inputTypeFor(objectSpec))
                        .build()
        );

        fieldBuilder.argument(
                GraphQLArgument.newArgument()
                        .name(oneToOneAssociation.getId())
                        .type(context.typeMapper.inputTypeFor(oneToOneAssociation, TypeMapper.InputContext.INVOKE))
                        .build());
    }


}
