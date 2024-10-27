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

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public class SimpleCollection
        extends Element {

    final ObjectInteractor objectInteractor;
    private final OneToManyAssociation objectMember;

    public SimpleCollection(
            final ObjectInteractor objectInteractor,
            final OneToManyAssociation otma,
            final Context context
    ) {
        super(context);
        this.objectInteractor = objectInteractor;
        this.objectMember = otma;

        var objectType = this.context.typeMapper.listTypeForElementTypeOf(otma, objectInteractor.getSchemaType());
        if(objectType != null) {
            setField(newFieldDefinition()
                        .name(getId())
                        .description(otma.getCanonicalDescription().orElse(otma.getCanonicalFriendlyName()))
                        .type(objectType)
                        .build()
            );
        } else {
            setField(null);
        }
    }

    public String getId() {
        return objectMember.asciiId();
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment environment) {

        var sourcePojo = BookmarkedPojo.sourceFrom(environment);

        var sourcePojoClass = sourcePojo.getClass();
        var objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        var otma = objectMember;
        var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        var resultManagedObject = otma.get(managedObject);

        return resultManagedObject != null
                ? resultManagedObject.getPojo()
                : null;
    }
}
