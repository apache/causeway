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

import org.apache.causeway.applib.services.bookmark.BookmarkService;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.*;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvProperty
        extends GqlvAssociation<OneToOneAssociation, GqlvProperty.Holder>
        implements GqlvMemberHidden.Holder,
        GqlvMemberDisabled.Holder,
        GqlvPropertyGet.Holder,
        GqlvPropertySet.Holder,
        GqlvPropertyValidate.Holder {

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;
    private final GqlvMemberHidden hidden;
    private final GqlvMemberDisabled disabled;
    private final GqlvPropertyGet get;
    private final GqlvPropertySet set;
    private final GqlvPropertyValidate validate;

    public GqlvProperty(
            final Holder holder,
            final OneToOneAssociation oneToOneAssociation,
            final Context context) {
        super(holder, oneToOneAssociation, context);

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.propertyTypeNameFor(this.holder.getObjectSpecification(), oneToOneAssociation));

        this.hidden = new GqlvMemberHidden(this, context);
        this.disabled = new GqlvMemberDisabled(this, context);
        this.get = new GqlvPropertyGet(this, context);
        this.set = new GqlvPropertySet(this, context);
        this.validate = new GqlvPropertyValidate(this, context);

        this.gqlObjectType = gqlObjectTypeBuilder.build();

        setField(
            this.holder.addField(
                newFieldDefinition()
                    .name(oneToOneAssociation.getId())
                    .type(gqlObjectTypeBuilder)
                    .build()
            )
        );
    }

    static void addGqlArgument(
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLFieldDefinition.Builder builder,
            final TypeMapper.InputContext inputContext) {
        builder.argument(gqlArgumentFor(oneToOneAssociation, inputContext));
    }

    private static GraphQLArgument gqlArgumentFor(
            final OneToOneAssociation oneToOneAssociation,
            final TypeMapper.InputContext inputContext) {
        return GraphQLArgument.newArgument()
                .name(oneToOneAssociation.getId())
                .type(TypeMapper.inputTypeFor(oneToOneAssociation, inputContext))
                .build();
    }


    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    public OneToOneAssociation getOneToOneAssociation() {
        return getObjectAssociation();
    }

    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }

    public void addDataFetcher() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                new BookmarkedPojoFetcher(context.bookmarkService));

        hidden.addDataFetcher();
        disabled.addDataFetcher();
        get.addDataFetcher();
        set.addDataFetcher();
        validate.addDataFetcher();
    }


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }


    public interface Holder extends GqlvAssociation.Holder {

    }
}
