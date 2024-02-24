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

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import graphql.schema.GraphQLOutputType;

import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class SimpleProperty
        extends SimpleMember<OneToOneAssociation>
        implements MemberInteractor<OneToOneAssociation> {

    final SimplePropertyLobName lobName;
    final SimplePropertyLobMimeType lobMimeType;
    final SimplePropertyLobBytes lobBytes;
    final SimplePropertyLobChars lobChars;

    public SimpleProperty(
            final ObjectInteractor objectInteractor,
            final OneToOneAssociation otoa,
            final Context context) {
        super(objectInteractor, otoa, context);

        final GraphQLOutputType outputType;
        if (isBuilt() ||
            (outputType = outputType(otoa)) == null) {
            lobName = null;
            lobMimeType = null;
            lobBytes = null;
            lobChars = null;
            return;
        }

        val fieldBuilder = newFieldDefinition()
                .name(getId())
                .type(outputType);
        setField(fieldBuilder.build());

        if(isBlobOrClob()) {
            addChildFieldFor(lobName = new SimplePropertyLobName(this, context));
            addChildFieldFor(lobMimeType = new SimplePropertyLobMimeType(this, context));

            if(isBlob()) {
                addChildFieldFor(lobBytes = new SimplePropertyLobBytes(this, context));
                lobChars = null;
            } else {
                addChildFieldFor(lobChars = new SimplePropertyLobChars(this, context));
                lobBytes = null;
            }
        } else {
            lobName = null;
            lobMimeType = null;
            lobBytes = null;
            lobChars = null;
        }
        buildObjectType();
    }

    GraphQLOutputType outputType(final OneToOneAssociation otoa) {

        if(isBlobOrClob()) {
            val typeName = TypeNames.propertyLobTypeNameFor(objectInteractor.getObjectSpecification(), otoa, objectInteractor.getSchemaType());
            return newObject()
                    .name(typeName)
                    .build();
        } else {
            return context.typeMapper.outputTypeFor(otoa, objectInteractor.getSchemaType());
        }
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


    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        if(isBlobOrClob()) {
            return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context);
        } else {
            val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

            val sourcePojoClass = sourcePojo.getClass();
            val objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
            if (objectSpecification == null) {
                // not expected
                return null;
            }

            val association = getObjectMember();
            val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
            val resultManagedObject = association.get(managedObject);

            return resultManagedObject != null
                    ? resultManagedObject.getPojo()
                    : null;
        }
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

    @Override
    public ObjectSpecification getObjectSpecification() {
        return objectInteractor.getObjectSpecification();
    }

    @Override
    public SchemaType getSchemaType() {
        return objectInteractor.getSchemaType();
    }
}
