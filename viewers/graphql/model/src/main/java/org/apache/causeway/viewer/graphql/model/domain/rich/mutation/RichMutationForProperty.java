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
package org.apache.causeway.viewer.graphql.model.domain.rich.mutation;

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
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.query.ObjectFeatureUtils;
import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.exceptions.InvalidException;
import org.apache.causeway.viewer.graphql.model.types.ResourceValueTypes;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

//@Slf4j
public class RichMutationForProperty extends Element {

    private final ObjectSpecification objectSpec;
    private final OneToOneAssociation oneToOneAssociation;
    private String argumentName;

    public RichMutationForProperty(
            final ObjectSpecification objectSpec,
            final OneToOneAssociation oneToOneAssociation,
            final Context context) {
        super(context);
        this.objectSpec = objectSpec;
        this.oneToOneAssociation = oneToOneAssociation;

        this.argumentName = context.causewayConfiguration.viewer().graphql().mutation().targetArgName();

        GraphQLOutputType type = context.typeMapper.outputTypeFor(objectSpec, SchemaType.RICH);  // setter returns void, so will return target instead.
        if (type != null) {
            var fieldBuilder = newFieldDefinition()
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
            final OneToOneAssociation otoa) {
        return TypeNames.objectTypeFieldNameFor(objectSpecification) + "__" + otoa.asciiId();
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        var environment = new Environment.For(dataFetchingEnvironment);
        var target = dataFetchingEnvironment.getArgument(argumentName);
        var sourcePojo = ObjectFeatureUtils.requirePojo(objectSpec, target, environment, context);
        var managedObject = ManagedObject.adaptSingular(objectSpec, sourcePojo);

        var visibleConsent = oneToOneAssociation.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visibleConsent.isVetoed()) {
            throw new HiddenException(oneToOneAssociation.getFeatureIdentifier());
        }

        var usableConsent = oneToOneAssociation.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usableConsent.isVetoed()) {
            throw new DisabledException(oneToOneAssociation.getFeatureIdentifier());
        }

        var argumentValue = dataFetchingEnvironment.getArgument(oneToOneAssociation.asciiId());
        var argumentPojo = ObjectFeatureUtils.unmarshalValue(
                oneToOneAssociation.getElementType(),
                argumentValue,
                environment,
                context);
        ResourceValueTypes.validateFileAccept(oneToOneAssociation, argumentPojo);
        if (!oneToOneAssociation.getElementType().isPojoCompatible(argumentPojo)) {
            throw new IllegalArgumentException(
                    "GraphQL input cannot be converted to the declared property type.");
        }
        var argumentManagedObject = ManagedObject.adaptProperty(oneToOneAssociation, argumentPojo);

        var validityConsent = oneToOneAssociation.isAssociationValid(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new InvalidException(validityConsent);
        }

        oneToOneAssociation.set(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);

        return managedObject.getPojo(); // setters return void, so return authoritative post-mutation state
    }

    private void addGqlArguments(final GraphQLFieldDefinition.Builder fieldBuilder) {

        // add target
        var targetArgName = context.causewayConfiguration.viewer().graphql().mutation().targetArgName();
        fieldBuilder.argument(
                GraphQLArgument.newArgument()
                        .name(targetArgName)
                        .type(context.typeMapper.inputTypeFor(objectSpec, SchemaType.RICH))
                        .build()
        );

        fieldBuilder.argument(
                GraphQLArgument.newArgument()
                        .name(oneToOneAssociation.asciiId())
                        .type(context.typeMapper.inputTypeFor(oneToOneAssociation, TypeMapper.InputContext.INVOKE, SchemaType.RICH))
                        .build());
    }
}
