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

import graphql.schema.GraphQLOutputType;

import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;

import lombok.val;

public class GqlvCollectionGet extends GqlvAssociationGet<OneToManyAssociation> {

    public GqlvCollectionGet(
            final MemberInteractor<OneToManyAssociation> holder,
            final Context context) {
        super(holder, context);
    }

    @Override
    GraphQLOutputType outputTypeFor(MemberInteractor<OneToManyAssociation> holder) {
        val oneToManyAssociation = holder.getObjectMember();
        return context.typeMapper.listTypeForElementTypeOf(oneToManyAssociation, holder.getSchemaType());
    }

}
