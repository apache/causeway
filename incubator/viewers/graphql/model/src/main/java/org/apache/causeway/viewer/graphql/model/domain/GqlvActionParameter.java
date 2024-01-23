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

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.types.ScalarMapper;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeReference;

public class GqlvActionParameter {
    public static GraphQLInputType inputTypeFor(final ObjectActionParameter objectActionParameter){
        ObjectSpecification elementType = objectActionParameter.getElementType();
        switch (elementType.getBeanSort()) {
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:

                return GraphQLTypeReference.typeRef(TypeNames.inputTypeNameFor(elementType));

            case VALUE:
                return (GraphQLInputType) ScalarMapper.typeFor(elementType.getCorrespondingClass());

            case COLLECTION:
                // TODO ...
            default:
                // for now
                return Scalars.GraphQLString;
        }

    }
}
