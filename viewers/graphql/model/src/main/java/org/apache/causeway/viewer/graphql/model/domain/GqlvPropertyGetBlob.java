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

import graphql.schema.DataFetchingEnvironment;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectAssociationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

public class GqlvPropertyGetBlob
        extends GqlvAbstractCustom
        implements GqlvPropertyGetBlobBytes.Holder
{

    final Holder holder;
    final GqlvPropertyGetBlobBytes blobName;
    final GqlvPropertyGetBlobMimeType blobMimeType;
    final GqlvPropertyGetBlobName blobBytes;

    public GqlvPropertyGetBlob(
            final Holder holder,
            final Context context) {
        super(TypeNames.propertyBlobTypeNameFor(holder.getObjectSpecification(), holder.getObjectMember()), context);

        this.holder = holder;

        if (isBuilt()) {
            // type already exists, nothing else to do.
            this.blobName = null;
            this.blobMimeType = null;
            this.blobBytes = null;
            return;
        }

        addChildFieldFor(blobName = new GqlvPropertyGetBlobBytes(this, context));
        addChildFieldFor(blobMimeType = new GqlvPropertyGetBlobMimeType(this, context));
        addChildFieldFor(blobBytes = new GqlvPropertyGetBlobName(this, context));

        setField(newFieldDefinition()
                    .name("get")
                    .type(buildObjectType())
                    .build());
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
        blobBytes.addDataFetcher(this);
    }

    @Override
    public OneToOneAssociation getObjectAssociation() {
        return holder.getObjectAssociation();
    }

    @Override
    public OneToOneAssociation getObjectMember() {
        return holder.getObjectMember();
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    public interface Holder
            extends ObjectSpecificationProvider,
                    ObjectAssociationProvider<OneToOneAssociation> {

    }
}
