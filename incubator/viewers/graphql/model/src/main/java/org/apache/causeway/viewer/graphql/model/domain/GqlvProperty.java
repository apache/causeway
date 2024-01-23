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

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLTypeReference.typeRef;

public class GqlvProperty extends GqlvAssociation<OneToOneAssociation, GqlvPropertyHolder> {

    public GqlvProperty(
            final GqlvPropertyHolder domainObject,
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        super(domainObject, oneToOneAssociation, fieldDefinition(domainObject, oneToOneAssociation), codeRegistryBuilder);
    }

    @Nullable private static GraphQLFieldDefinition fieldDefinition(
            final GqlvPropertyHolder domainObject,
            final OneToOneAssociation otoa) {

        GraphQLOutputType type = outputTypeFor(otoa);

        GraphQLFieldDefinition fieldDefinition = null;
        if (type != null) {
            fieldDefinition = newFieldDefinition()
                    .name(otoa.getId())
                    .type(type).build();
            domainObject.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    private static GraphQLOutputType outputTypeFor(final OneToOneAssociation otoa) {
        ObjectSpecification otoaObjectSpec = otoa.getElementType();
        switch (otoaObjectSpec.getBeanSort()) {

            case VIEW_MODEL:
            case ENTITY:

                GraphQLTypeReference fieldTypeRef = typeRef(TypeNames.objectTypeNameFor(otoaObjectSpec));
                return otoa.isOptional()
                        ? fieldTypeRef
                        : nonNull(fieldTypeRef);

            case VALUE:

                // todo: map ...

                return otoa.isOptional()
                        ? Scalars.GraphQLString
                        : nonNull(Scalars.GraphQLString);
        }
        return null;
    }

    public OneToOneAssociation getOneToOneAssociation() {
        return getObjectAssociation();
    }

}
