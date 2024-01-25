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
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvCollection extends GqlvAssociation<OneToManyAssociation, GqlvCollection.Holder> implements GqlvCollectionGet.Holder, GqlvMemberHidden.Holder, GqlvMemberDisabled.Holder {

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;
    private final GqlvMemberHidden hidden;
    private final GqlvMemberDisabled disabled;
    private final GqlvCollectionGet get;
    private final BookmarkService bookmarkService;

    public GqlvCollection(
            final Holder domainObject,
            final OneToManyAssociation oneToManyAssociation,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService
    ) {
        super(domainObject, oneToManyAssociation, codeRegistryBuilder);

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.collectionTypeNameFor(holder.getObjectSpecification(), oneToManyAssociation));
        this.bookmarkService = bookmarkService;

        this.hidden = new GqlvMemberHidden(this, codeRegistryBuilder);
        this.disabled = new GqlvMemberDisabled(this, codeRegistryBuilder);
        this.get = new GqlvCollectionGet(this, codeRegistryBuilder, specificationLoader);

        this.gqlObjectType = gqlObjectTypeBuilder.build();

        setField(
            holder.addField(
                newFieldDefinition()
                    .name(oneToManyAssociation.getId())
                    .type(gqlObjectTypeBuilder)
                    .build()
            )
        );
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    public OneToManyAssociation getOneToManyAssociation() {
        return getObjectAssociation();
    }

    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }

    public void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                new BookmarkedPojoFetcher(bookmarkService));

        hidden.addDataFetcher();
        disabled.addDataFetcher();
        get.addDataFetcher();
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }

    public interface Holder extends GqlvAssociation.Holder {

    }
}
