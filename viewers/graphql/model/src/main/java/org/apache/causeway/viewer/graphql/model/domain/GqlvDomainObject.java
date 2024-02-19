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
import java.util.SortedMap;
import java.util.TreeMap;

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
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;

import graphql.schema.GraphQLList;

import lombok.Getter;
import lombok.val;

/**
 * Exposes a domain object (view model or entity) via the GQL viewer.
 */
public class GqlvDomainObject
        extends GqlvAbstractCustom
        implements GqlvMember.Holder, GqlvMeta.Holder {

    @Getter private final ObjectSpecification objectSpecification;

    private final GqlvMeta meta;

    private final SortedMap<String, GqlvProperty> properties = new TreeMap<>();
    private final SortedMap<String, GqlvCollection> collections = new TreeMap<>();
    private final Map<String, GqlvAction> actions = new TreeMap<>();


    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    public static GqlvDomainObject of(
            final ObjectSpecification objectSpecification,
            final Context context) {
        return context.domainObjectBySpec.computeIfAbsent(objectSpecification, spec -> new GqlvDomainObject(spec, context));
    }

    private GqlvDomainObject(
            final ObjectSpecification objectSpecification,
            final Context context) {
        super(TypeNames.objectTypeNameFor(objectSpecification), context);

        this.objectSpecification = objectSpecification;
        gqlObjectTypeBuilder.description(objectSpecification.getDescription());

        if(isBuilt()) {
            this.meta = null;
            this.gqlInputObjectType = null;
            return;
        }

        addChildFieldFor(this.meta = new GqlvMeta(this, context));

        val inputObjectTypeBuilder = newInputObject().name(TypeNames.inputTypeNameFor(objectSpecification));
        inputObjectTypeBuilder
                .field(newInputObjectField()
                        .name("id")
                        .type(Scalars.GraphQLID)
                        .build()
                )
                .field(newInputObjectField()
                        .name("ref")
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
                TypeNames.objectTypeNameFor(objectSpec),
                lookupConfig.getFieldNameSuffix());

        return newFieldDefinition()
                .name(fieldName)
                .type(this.context.typeMapper.outputTypeFor(objectSpec))
                .argument(GraphQLArgument.newArgument()
                        .name(lookupConfig.getArgument())   // eg "object"
                        .type(gqlInputObjectType)
                        .build())
                .build();
    }


    private void addMembers() {

        objectSpecification.streamProperties(MixedIn.INCLUDED).forEach(this::addProperty);
        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(this::addCollection);

        objectSpecification.streamActions(context.getActionScope(), MixedIn.INCLUDED)
                .forEach(objectAction -> {
                    val gqlvAction = new GqlvAction(this, objectAction, context);
                    addChildFieldFor(gqlvAction);
                    actions.put(objectAction.getId(), gqlvAction);
                });
    }

    @SuppressWarnings("unused")
    private ActionScope determineActionScope() {
        return context.causewaySystemEnvironment.getDeploymentType().isProduction()
                ? ActionScope.PRODUCTION
                : ActionScope.PROTOTYPE;
    }

    private void addProperty(final OneToOneAssociation otoa) {
        val gqlvProperty = new GqlvProperty(this, otoa, context);
        addChildFieldFor(gqlvProperty);
        properties.put(otoa.getId(), gqlvProperty);
    }

    private void addCollection(OneToManyAssociation otom) {
        val gqlvCollection = new GqlvCollection(this, otom, context);
        addChildFieldFor(gqlvCollection);
        if (gqlvCollection.isFieldDefined()) {
            collections.put(otom.getId(), gqlvCollection);
        }
    }


    @Override
    protected void addDataFetchersForChildren() {
        if(meta == null) {
            return;
        }
        meta.addDataFetcher(this);
        properties.forEach((id, property) -> property.addDataFetcher(this));
        collections.forEach((id, collection) -> collection.addDataFetcher(this));
        actions.forEach((id, action) -> action.addDataFetcher(this));
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        Object target = dataFetchingEnvironment.getArgument("object");
        return GqlvAction.asPojo(getObjectSpecification(), target, this.context.bookmarkService, new Environment.For(dataFetchingEnvironment))
                .orElse(null);
    }


    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }


}
