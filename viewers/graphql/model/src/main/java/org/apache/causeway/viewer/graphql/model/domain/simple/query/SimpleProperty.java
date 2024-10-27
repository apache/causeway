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
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.Parent;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import lombok.Getter;

public class SimpleProperty
        extends Element
        implements MemberInteractor<OneToOneAssociation>, Parent {

    @Getter final ObjectInteractor objectInteractor;
    @Getter private final OneToOneAssociation objectMember;

    /**
     * Only populated if there are child fields.
     */
    final GraphQLObjectType gqlObjectType;
    String getTypeName() {
        return gqlObjectType != null ? gqlObjectType.getName() : null;
    }

    final SimplePropertyLobName lobName;
    final SimplePropertyLobMimeType lobMimeType;
    final SimplePropertyLobBytes lobBytes;
    final SimplePropertyLobChars lobChars;

    public SimpleProperty(
            final ObjectInteractor objectInteractor,
            final OneToOneAssociation otoa,
            final Context context) {

        super(context);
        this.objectInteractor = objectInteractor;
        this.objectMember = otoa;

        final GraphQLOutputType gqlOutputType;
        if (isBlobOrClob(otoa)) {
            var glqObjectTypeBuilder = newObject()
                    .name(TypeNames.memberTypeNameFor(objectInteractor.getObjectSpecification(), otoa, objectInteractor.getSchemaType()))
                    .description(otoa.getCanonicalDescription().orElse(otoa.getCanonicalFriendlyName()))
                    ;

            addChildFieldFor(glqObjectTypeBuilder, lobName = new SimplePropertyLobName(this, context));
            addChildFieldFor(glqObjectTypeBuilder, lobMimeType = new SimplePropertyLobMimeType(this, context));

            if(isBlob(getObjectMember())) {
                addChildFieldFor(glqObjectTypeBuilder, lobBytes = new SimplePropertyLobBytes(this, context));
                lobChars = null;
            } else {
                addChildFieldFor(glqObjectTypeBuilder, lobChars = new SimplePropertyLobChars(this, context));
                lobBytes = null;
            }

            gqlOutputType = gqlObjectType = glqObjectTypeBuilder.build();
        } else {
            gqlObjectType = null;
            gqlOutputType = outputType(objectInteractor, otoa, context);
            lobName = null;
            lobMimeType = null;
            lobBytes = null;
            lobChars = null;
        }

        if (gqlOutputType != null) {
            var fieldBuilder = newFieldDefinition()
                    .name(otoa.asciiId())
                    .type(gqlOutputType);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    private static GraphQLOutputType outputType(
            final ObjectInteractor objectInteractor,
            final OneToOneAssociation otoa,
            final Context context) {

        if(isBlobOrClob(otoa)) {
            var typeName = TypeNames.propertyLobTypeNameFor(objectInteractor.getObjectSpecification(), otoa, objectInteractor.getSchemaType());
            return newObject()
                    .name(typeName)
                    .build();
        } else {
            return context.typeMapper.outputTypeFor(otoa, objectInteractor.getSchemaType());
        }
    }

    static <T extends Element> void addChildFieldFor(
            final GraphQLObjectType.Builder glqObjectTypeBuilder,
            final T hasField) {
        GraphQLFieldDefinition childField = hasField.getField();
        if (childField != null) {
            glqObjectTypeBuilder.field(childField);
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

        if(isBlobOrClob(getObjectMember())) {
            return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context);
        } else {
            var sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

            var sourcePojoClass = sourcePojo.getClass();
            var objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
            if (objectSpecification == null) {
                // not expected
                return null;
            }

            var association = getObjectMember();
            var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
            var resultManagedObject = association.get(managedObject);

            return resultManagedObject != null
                    ? resultManagedObject.getPojo()
                    : null;
        }
    }

    private static boolean isBlobOrClob(OneToOneAssociation otota) {
        return isBlob(otota) || isClob(otota);
    }

    private static boolean isBlob(OneToOneAssociation otoa) {
        return otoa.getElementType().getCorrespondingClass() == Blob.class;
    }

    private static boolean isClob(OneToOneAssociation otoa) {
        return otoa.getElementType().getCorrespondingClass() == Clob.class;
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return objectInteractor.getObjectSpecification();
    }

    @Override
    public SchemaType getSchemaType() {
        return objectInteractor.getSchemaType();
    }

    public final FieldCoordinates coordinatesFor(final GraphQLFieldDefinition field) {
        if (gqlObjectType == null) {
            throw new IllegalStateException(
                    String.format("GQL Object Type for '%s' not yet built", getTypeName()));
        }
        return FieldCoordinates.coordinates(gqlObjectType, field);
    }

    public final FieldCoordinates coordinatesFor(final String fieldName) {
        if (gqlObjectType == null) {
            throw new IllegalStateException(
                    String.format("GQL Object Type for '%s' not yet built", getTypeName()));
        }
        return FieldCoordinates.coordinates(gqlObjectType, fieldName);
    }

}
