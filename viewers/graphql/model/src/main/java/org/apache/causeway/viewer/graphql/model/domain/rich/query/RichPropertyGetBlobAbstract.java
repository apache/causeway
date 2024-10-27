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

import java.util.Optional;
import java.util.function.Function;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public abstract class RichPropertyGetBlobAbstract extends Element {

    final MemberInteractor<OneToOneAssociation> memberInteractor;

    public RichPropertyGetBlobAbstract(
            final MemberInteractor<OneToOneAssociation> memberInteractor,
            final Context context, String name) {
        super(context);
        this.memberInteractor = memberInteractor;

        setField(GraphQLFieldDefinition.newFieldDefinition()
                    .name(name)
                    .type(Scalars.GraphQLString)
                    .build());
    }

    protected Object fetchDataFromBlob(DataFetchingEnvironment environment, Function<Blob, ?> mapper) {
        var sourcePojo = BookmarkedPojo.sourceFrom(environment);

        var sourcePojoClass = sourcePojo.getClass();
        var objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        var association = memberInteractor.getObjectMember();
        var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        var resultManagedObject = association.get(managedObject);

        return Optional.ofNullable(resultManagedObject)
                .map(ManagedObject::getPojo)
                .filter(Blob.class::isInstance)
                .map(Blob.class::cast)
                .map(mapper)
                .orElse(null);
    }

}
