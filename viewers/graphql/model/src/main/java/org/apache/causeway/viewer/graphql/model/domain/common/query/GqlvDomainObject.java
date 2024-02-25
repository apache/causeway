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
package org.apache.causeway.viewer.graphql.model.domain.common.query;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;

import java.util.ArrayList;
import java.util.List;

import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import lombok.Getter;
import lombok.val;

/**
 * Exposes a domain object (view model or entity) via the GQL viewer.
 */
public class GqlvDomainObject
        extends GqlvAbstractCustom
        implements ObjectInteractor, ObjectSpecificationProvider, SchemaTypeProvider {

    private final SchemaStrategy schemaStrategy;
    @Getter private final ObjectSpecification objectSpecification;

    @Override
    public SchemaType getSchemaType() {
        return schemaStrategy.getSchemaType();
    }

    private final GqlvAbstractCustom meta;

    private final List<GqlvAbstract> properties = new ArrayList<>();
    private final List<GqlvAbstract> collections = new ArrayList<>();
    private final List<GqlvAbstract> actions = new ArrayList<>();

    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    public static String typeNameFor(SchemaStrategy schemaStrategy, ObjectSpecification objectSpecification) {
        return TypeNames.objectTypeNameFor(objectSpecification, schemaStrategy.getSchemaType());
    }

    public GqlvDomainObject(
            final SchemaStrategy schemaStrategy,
            final String typeName,
            final ObjectSpecification objectSpecification,
            final Context context) {
        super(typeName, context);
        this.schemaStrategy = schemaStrategy;
        this.objectSpecification = objectSpecification;

        gqlObjectTypeBuilder.description(objectSpecification.getDescription());

        addChildFieldFor(this.meta = schemaStrategy.newGqlvMeta(this, context));

        val inputObjectTypeBuilder = newInputObject().name(TypeNames.inputTypeNameFor(objectSpecification, getSchemaType()));
        inputObjectTypeBuilder
                .field(newInputObjectField()
                        .name("id")
                        .description("Use either 'id' or 'ref'; looks up an entity from the persistent data store, or if a view model, then recreates using the id as a memento of the object's state")
                        .type(Scalars.GraphQLID)
                        .build()
                )
                .field(newInputObjectField()
                        .name("logicalTypeName")
                        .description("If object identified by 'id', then optionally specifies concrete type.  This is only required if the parameter type defines a super class")
                        .type(Scalars.GraphQLString)
                        .build()
                )
                .field(newInputObjectField()
                        .name("ref")
                        .description("Use either 'ref' or 'id'; looks up an object previously saved to the execution context using 'saveAs(ref: ...)'")
                        .type(Scalars.GraphQLString)
                        .build()
                )
        ;
        gqlInputObjectType = inputObjectTypeBuilder.build();

        // hmm..
        setField(newField());

        addMembers();

        val objectType = buildObjectType();

        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(objectType);
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlInputObjectType);

    }

    public GraphQLFieldDefinition newField(
            final String fieldName,
            final String description) {
        return buildFieldDefinition(fieldName, gqlInputObjectType);
    }

    public GraphQLFieldDefinition newField() {

        // add domain object lookup to top-level query
        val fieldName = String.format("%s%s%s",
                graphqlConfiguration.getLookup().getFieldNamePrefix(),  // eg "_gqlv_lookup__"
                TypeNames.objectTypeFieldNameFor(objectSpecification),
                graphqlConfiguration.getLookup().getFieldNameSuffix());

        return buildFieldDefinition(fieldName, gqlInputObjectType);
    }

    private GraphQLFieldDefinition buildFieldDefinition(
            final String fieldName,
            final GraphQLInputObjectType gqlInputObjectType
    ) {
        val objectSpec = getObjectSpecification();

        return newFieldDefinition()
                .name(fieldName)
                .type(this.context.typeMapper.outputTypeFor(objectSpec, getSchemaType()))
                .argument(GraphQLArgument.newArgument()
                        .name(graphqlConfiguration.getLookup().getArgument())   // eg "object"
                        .type(gqlInputObjectType)
                        .build())
                .build();
    }


    private void addMembers() {

        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(prop -> properties.add(addChildFieldFor(schemaStrategy.newGqlvProperty(this, prop, context))));
        objectSpecification.streamCollections(MixedIn.INCLUDED)
                .forEach(coll -> collections.add(addChildFieldFor(schemaStrategy.newGqlvCollection(this, coll, context))));
        objectSpecification.streamActions(context.getActionScope(), MixedIn.INCLUDED)
                .filter(act -> schemaStrategy.shouldInclude(graphqlConfiguration.getApiVariant(), act))
                .forEach(act -> actions.add(addChildFieldFor(schemaStrategy.newGqlvAction(this, act, context))));
    }

    @SuppressWarnings("unused")
    private ActionScope determineActionScope() {
        return context.causewaySystemEnvironment.getDeploymentType().isProduction()
                ? ActionScope.PRODUCTION
                : ActionScope.PROTOTYPE;
    }


    @Override
    protected void addDataFetchersForChildren() {
        if(meta == null) {
            return;
        }
        meta.addDataFetcher(this);
        properties.forEach(property -> property.addDataFetcher(this));
        collections.forEach(collection -> collection.addDataFetcher(this));
        actions.forEach(action -> action.addDataFetcher(this));
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        Object target = dataFetchingEnvironment.getArgument("object");
        return GvqlActionUtils.asPojo(getObjectSpecification(), target, new Environment.For(dataFetchingEnvironment), context)
                .orElse(null);
    }


    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }

}
