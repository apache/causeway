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
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public abstract class GqlvAssociationGet<T extends ObjectAssociation> extends GqlvAbstract {

    final MemberInteractor<T> memberInteractor;

    public GqlvAssociationGet(
            final MemberInteractor<T> memberInteractor,
            final Context context) {
        super(context);
        this.memberInteractor = memberInteractor;

        GraphQLOutputType type = outputTypeFor(memberInteractor);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
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
        val sourcePojo = BookmarkedPojo.sourceFrom(environment);

        val sourcePojoClass = sourcePojo.getClass();
        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        val association = memberInteractor.getObjectMember();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val resultManagedObject = association.get(managedObject);

        return resultManagedObject != null
                ? resultManagedObject.getPojo()
                : null;
    }

}
