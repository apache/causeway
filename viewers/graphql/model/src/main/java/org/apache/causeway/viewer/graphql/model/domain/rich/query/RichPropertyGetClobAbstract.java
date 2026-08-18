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
import graphql.schema.GraphQLOutputType;

import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public abstract class RichPropertyGetClobAbstract extends Element {

    final MemberInteractor<OneToOneAssociation> holder;

    public RichPropertyGetClobAbstract(
            final MemberInteractor<OneToOneAssociation> memberInteractor,
            final Context context,
            final String name) {
        this(memberInteractor, context, name, Scalars.GraphQLString);
    }

    protected RichPropertyGetClobAbstract(
            final MemberInteractor<OneToOneAssociation> memberInteractor,
            final Context context,
            final String name,
            final GraphQLOutputType outputType) {
        super(context);
        this.holder = memberInteractor;

        setField(GraphQLFieldDefinition.newFieldDefinition()
                    .name(name)
                    .type(outputType)
                    .build());
    }

    protected Object fetchDataFromClob(DataFetchingEnvironment environment, Function<Clob, ?> mapper) {
        var sourcePojo = BookmarkedPojo.sourceFrom(environment);

        var sourcePojoClass = sourcePojo.getClass();
        var objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        var association = holder.getObjectMember();
        var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        var resultManagedObject = association.get(managedObject);

        return Optional.ofNullable(resultManagedObject)
                .map(ManagedObject::getPojo)
                .filter(Clob.class::isInstance)
                .map(Clob.class::cast)
                .map(mapper)
                .orElse(null);
    }

}
