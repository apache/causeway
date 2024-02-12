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

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojoFetcher;

import lombok.val;

public class GqlvProperty
        extends GqlvAssociation<OneToOneAssociation, GqlvProperty.Holder>
        implements GqlvMemberHidden.Holder<OneToOneAssociation>,
                   GqlvMemberDisabled.Holder<OneToOneAssociation>,
                   GqlvPropertyGet.Holder,
                   GqlvPropertyChoices.Holder,
                   GqlvPropertyAutoComplete.Holder,
                   GqlvPropertyValidate.Holder,
                   GqlvPropertySet.Holder {

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;
    private final GqlvMemberHidden<OneToOneAssociation> hidden;
    private final GqlvMemberDisabled<OneToOneAssociation> disabled;
    private final GqlvPropertyGet get;
    /**
     * Populated iff there are choices
     */
    private final GqlvPropertyChoices choices;
    /**
     * Populated iff there is an autoComplete
     */
    private final GqlvPropertyAutoComplete autoComplete;
    private final GqlvPropertyValidate validate;
    /**
     * Populated iff the API variant allows for it.
     */
    private final GqlvPropertySet set;

    public GqlvProperty(
            final Holder holder,
            final OneToOneAssociation oneToOneAssociation,
            final Context context) {
        super(holder, oneToOneAssociation, context);

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.propertyTypeNameFor(this.holder.getObjectSpecification(), oneToOneAssociation));

        this.hidden = new GqlvMemberHidden<>(this, context);
        addField(hidden.getField());

        this.disabled = new GqlvMemberDisabled<>(this, context);
        addField(disabled.getField());

        this.get = new GqlvPropertyGet(this, context);
        addField(get.getField());

        this.validate = new GqlvPropertyValidate(this, context);
        addField(this.validate.getField());

        val choices = new GqlvPropertyChoices(this, context);
        if (choices.hasChoices()) {
            addField(choices.getField());
            this.choices = choices;
        } else {
            this.choices = null;
        }

        val autoComplete = new GqlvPropertyAutoComplete(this, context);
        if (autoComplete.hasAutoComplete()) {
            addField(autoComplete.getField());
            this.autoComplete = autoComplete;
        } else {
            this.autoComplete = null;
        }

        val variant = context.causewayConfiguration.getViewer().getGraphql().getApiVariant();
        if (variant == CausewayConfiguration.Viewer.Graphql.ApiVariant.QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT) {
            this.set = new GqlvPropertySet(this, context);
            addField(set.getField());
        } else {
            this.set = null;
        }


        this.gqlObjectType = gqlObjectTypeBuilder.build();

        setField(
            newFieldDefinition()
                .name(oneToOneAssociation.getId())
                .type(gqlObjectTypeBuilder)
                .build()
        );
    }

    public void addGqlArgument(
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLFieldDefinition.Builder builder,
            final TypeMapper.InputContext inputContext) {
        builder.argument(gqlArgumentFor(oneToOneAssociation, inputContext));
    }

    private GraphQLArgument gqlArgumentFor(
            final OneToOneAssociation oneToOneAssociation,
            final TypeMapper.InputContext inputContext) {
        return GraphQLArgument.newArgument()
                .name(oneToOneAssociation.getId())
                .type(context.typeMapper.inputTypeFor(oneToOneAssociation, inputContext))
                .build();
    }


    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    public OneToOneAssociation getOneToOneAssociation() {
        return getObjectAssociation();
    }

    private GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        if (field != null) {
            gqlObjectTypeBuilder.field(field);
        }
        return field;
    }

    public void addDataFetcher() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                new BookmarkedPojoFetcher(context.bookmarkService));

        hidden.addDataFetcher();
        disabled.addDataFetcher();
        get.addDataFetcher();
        if(choices != null) {
            choices.addDataFetcher();
        }
        if(autoComplete != null) {
            autoComplete.addDataFetcher();
        }
        validate.addDataFetcher();
        if (set != null) {
            set.addDataFetcher();
        }
    }


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }


    public interface Holder
            extends GqlvAssociation.Holder {
    }
}
