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
package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import java.util.ArrayList;
import java.util.List;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;

import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import lombok.Getter;
import lombok.val;

/**
 * Exposes a domain object (view model or entity) via the GQL viewer.
 */
public class GqlvDomainObject
        extends GqlvAbstractCustom
        implements GqlvMember.Holder, GqlvMeta.Holder {

    private static final SchemaType SCHEMA_TYPE = SchemaType.RICH;

    @Getter private final ObjectSpecification objectSpecification;

    private final GqlvMeta meta;

    private final List<GqlvProperty> properties = new ArrayList<>();
    private final List<GqlvCollection> collections = new ArrayList<>();
    private final List<GqlvAction> actions = new ArrayList<>();


    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    public static GqlvDomainObject of(
            final ObjectSpecification objectSpecification,
            final Context context) {

        mapSuperclassesIfNecessary(objectSpecification, context);

        return computeIfAbsentGqlvDomainObject(context, objectSpecification);
    }

    private static void mapSuperclassesIfNecessary(
            final ObjectSpecification objectSpecification,
            final Context context) {
        // no need to map if the target subclass has already been built
        if(context.richDomainObjectBySpec.containsKey(objectSpecification)) {
            return;
        }
        val superclasses = superclassesOf(objectSpecification);
        superclasses.forEach(objectSpec -> computeIfAbsentGqlvDomainObject(context, objectSpec));
    }

    private static GqlvDomainObject computeIfAbsentGqlvDomainObject(Context context, ObjectSpecification objectSpec) {
        return context.richDomainObjectBySpec.computeIfAbsent(objectSpec, spec -> new GqlvDomainObject(spec, context));
    }

    private static List<ObjectSpecification> superclassesOf(final ObjectSpecification objectSpecification) {
        val superclasses = new ArrayList<ObjectSpecification>();
        ObjectSpecification superclass = objectSpecification.superclass();
        while (superclass != null && superclass.getCorrespondingClass() != Object.class) {
            superclasses.add(0, superclass);
            superclass = superclass.superclass();
        }
        return superclasses;
    }

    private GqlvDomainObject(
            final ObjectSpecification objectSpecification,
            final Context context) {
        super(TypeNames.objectTypeNameFor(objectSpecification, SCHEMA_TYPE), context);

        this.objectSpecification = objectSpecification;
        gqlObjectTypeBuilder.description(objectSpecification.getDescription());

        if(isBuilt()) {
            this.meta = null;
            this.gqlInputObjectType = null;
            return;
        }

        addChildFieldFor(this.meta = new GqlvMeta(this, context));

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

        setField(buildFieldDefinition(gqlInputObjectType));

        addMembers();

        val objectType = buildObjectType();

        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(objectType);
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlInputObjectType);

    }

    private GraphQLFieldDefinition buildFieldDefinition(final GraphQLInputObjectType gqlInputObjectType) {
        val lookupConfig = this.context.causewayConfiguration.getViewer().getGraphql().getLookup();
        val objectSpec = getObjectSpecification();
        val fieldName = String.format("%s%s%s",
                lookupConfig.getFieldNamePrefix(),          // eg "_gqlv_lookup__"
                TypeNames.objectTypeFieldNameFor(objectSpec),
                lookupConfig.getFieldNameSuffix());

        return newFieldDefinition()
                .name(fieldName)
                .type(this.context.typeMapper.outputTypeFor(objectSpec, getSchemaType()))
                .argument(GraphQLArgument.newArgument()
                        .name(lookupConfig.getArgument())   // eg "object"
                        .type(gqlInputObjectType)
                        .build())
                .build();
    }


    private void addMembers() {

        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(prop -> properties.add(addChildFieldFor(new GqlvProperty(this, prop, context))));
        objectSpecification.streamCollections(MixedIn.INCLUDED)
                .forEach(coll -> collections.add(addChildFieldFor(new GqlvCollection(this, coll, context))));
        objectSpecification.streamActions(context.getActionScope(), MixedIn.INCLUDED)
                .forEach(act -> actions.add(addChildFieldFor(new GqlvAction(this, act, context))));
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
        return GqlvAction.asPojo(getObjectSpecification(), target, new Environment.For(dataFetchingEnvironment), context)
                .orElse(null);
    }

    public SchemaType getSchemaType() {
        return SCHEMA_TYPE;
    }

    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }

}
