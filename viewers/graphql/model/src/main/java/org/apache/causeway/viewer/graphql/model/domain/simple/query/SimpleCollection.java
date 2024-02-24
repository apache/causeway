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
import graphql.schema.GraphQLOutputType;

import lombok.val;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class SimpleCollection
        extends SimpleMember<OneToManyAssociation>
        implements MemberInteractor<OneToManyAssociation> {

    public SimpleCollection(
            final ObjectInteractor objectInteractor,
            final OneToManyAssociation otma,
            final Context context
    ) {
        super(objectInteractor, otma, context);

        if (isBuilt()) {
            return;
        }
        GraphQLOutputType type = outputType();
        val fieldBuilder = newFieldDefinition()
                .name(getId())
                .type(type);
        setField(fieldBuilder.build());

    }

    GraphQLOutputType outputType() {
        val oneToManyAssociation = this.getObjectMember();
        return context.typeMapper.listTypeForElementTypeOf(oneToManyAssociation, getSchemaType());
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return objectInteractor.getObjectSpecification();
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment environment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(environment);

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



    @Override
    public SchemaType getSchemaType() {
        return objectInteractor.getSchemaType();
    }

}
