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
package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class GqlvPropertyGet  extends GqlvAssociationGet<OneToOneAssociation> {

    public GqlvPropertyGet(
            final Holder holder,
            final Context context) {
        super(holder, context);
    }

    @Override
    GraphQLOutputType outputTypeFor(GqlvAssociationGet.Holder<OneToOneAssociation> holder) {
        val oneToOneAssociation = holder.getObjectAssociation();
        return TypeMapper.outputTypeFor(oneToOneAssociation);
    }

    public interface Holder extends GqlvAssociationGet.Holder<OneToOneAssociation> {

        @Override
        OneToOneAssociation getObjectAssociation();
    }
}
