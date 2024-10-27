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
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public abstract class RichAssociationGet<T extends ObjectAssociation> extends Element {

    final MemberInteractor<T> memberInteractor;

    public RichAssociationGet(
            final MemberInteractor<T> memberInteractor,
            final Context context) {
        super(context);
        this.memberInteractor = memberInteractor;

        GraphQLOutputType type = outputTypeFor(memberInteractor);
        if (type != null) {
            var fieldBuilder = newFieldDefinition()
                    .name("get")
                    .type(type);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    abstract GraphQLOutputType outputTypeFor(MemberInteractor<T> holder);

    @Override
    protected Object fetchData(final DataFetchingEnvironment environment) {

        // TODO: introduce evaluator
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

        return resultManagedObject != null
                ? resultManagedObject.getPojo()
                : null;
    }

}
