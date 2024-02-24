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
package org.apache.causeway.viewer.graphql.model.domain.simple.query;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import graphql.schema.GraphQLOutputType;

import lombok.val;

public class SimpleProperty
        extends SimpleAssociation<OneToOneAssociation, ObjectInteractor> {

    final SimplePropertyLobName lobName;
    final SimplePropertyLobMimeType lobMimeType;
    final SimplePropertyLobBytes lobBytes;
    final SimplePropertyLobChars lobChars;

    public SimpleProperty(
            final ObjectInteractor objectInteractor,
            final OneToOneAssociation otoa,
            final Context context) {
        super(objectInteractor, otoa, context);

        if (isBuilt()) {
            lobName = null;
            lobMimeType = null;
            lobBytes = null;
            lobChars = null;
            return;
        }

        if(isBlobOrClob()) {

            lobName = null;  // TODO
            lobMimeType = null;  // TODO

            if(isBlob()) {
                // new GqlvPropertyGetBlob(this, context)
                lobBytes = null;  // TODO

                lobChars = null;
            } else {
                // new GqlvPropertyGetClob(this, context)
                lobChars = null;  // TODO

                lobBytes = null;
            }
        } else {
            lobName = null;
            lobMimeType = null;
            lobBytes = null;
            lobChars = null;
        }
    }

    // TODO
    @Override
    GraphQLOutputType outputType() {

        if(isBlob()) {
            // new GqlvPropertyGetBlob(this, context)

        } else if (isClob()) {
            // new GqlvPropertyGetClob(this, context)

        } else {

        }

        return null;
    }

    private boolean isBlobOrClob() {
        return isBlob() || isClob();
    }

    private boolean isBlob() {
        return getObjectMember().getElementType().getCorrespondingClass() == Blob.class;
    }

    private boolean isClob() {
        return getObjectMember().getElementType().getCorrespondingClass() == Clob.class;
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
                .type(context.typeMapper.inputTypeFor(oneToOneAssociation, inputContext, SchemaType.RICH))
                .build();
    }

    @Override
    protected void addDataFetchersForChildren() {
        if(lobName != null) {
            lobName.addDataFetcher(this);
        }
        if(lobMimeType != null) {
            lobMimeType.addDataFetcher(this);
        }
        if(lobBytes != null) {
            lobBytes.addDataFetcher(this);
        }
        if(lobChars != null) {
            lobChars.addDataFetcher(this);
        }
    }

    GraphQLOutputType outputTypeFor(MemberInteractor<OneToOneAssociation> holder) {
        val oneToOneAssociation = holder.getObjectMember();
        return context.typeMapper.outputTypeFor(oneToOneAssociation, holder.getSchemaType());
    }

}
