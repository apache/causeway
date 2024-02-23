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

import graphql.schema.DataFetchingEnvironment;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public class GqlvPropertyGetBlob
        extends GqlvAbstractCustom {

    final MemberInteractor<OneToOneAssociation> holder;
    final GqlvPropertyGetBlobBytes blobName;
    final GqlvPropertyGetBlobMimeType blobMimeType;
    final GqlvPropertyGetBlobName blobBytes;

    private final CausewayConfiguration.Viewer.Graphql graphqlConfiguration;

    public GqlvPropertyGetBlob(
            final MemberInteractor<OneToOneAssociation> holder,
            final Context context) {
        super(TypeNames.propertyBlobTypeNameFor(holder.getObjectSpecification(), holder.getObjectMember(), holder.getSchemaType()), context);
        this.holder = holder;

        this.graphqlConfiguration = context.causewayConfiguration.getViewer().getGraphql();

        if (isBuilt()) {
            // type already exists, nothing else to do.
            this.blobName = null;
            this.blobMimeType = null;
            this.blobBytes = null;
            return;
        }

        addChildFieldFor(blobName = new GqlvPropertyGetBlobBytes(holder, context));
        addChildFieldFor(blobMimeType = new GqlvPropertyGetBlobMimeType(holder, context));
        addChildFieldFor(blobBytes = isResourceNotForbidden() ? new GqlvPropertyGetBlobName(holder, context) : null);

        setField(newFieldDefinition()
                    .name("get")
                    .type(buildObjectType())
                    .build());
    }

    private boolean isResourceNotForbidden() {
        return graphqlConfiguration.getResources().getResponseType() != CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN;
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {
        return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context);
    }

    @Override
    protected void addDataFetchersForChildren() {
        if (blobName == null) {
            return;
        }
        blobName.addDataFetcher(this);
        blobMimeType.addDataFetcher(this);
        if (blobBytes != null) {
            blobBytes.addDataFetcher(this);
        }
    }

}
