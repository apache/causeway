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

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

public class GqlvPropertyValidate extends GqlvAbstract {

    final HolderPropertySet holder;

    public GqlvPropertyValidate(
            final HolderPropertySet holder,
            final Context context) {
        super(context);
        this.holder = holder;

        val fieldBuilder = newFieldDefinition()
                .name("validate")
                .type((GraphQLOutputType) context.typeMapper.outputTypeFor(String.class));
        holder.addGqlArgument(holder.getOneToOneAssociation(), fieldBuilder, TypeMapper.InputContext.VALIDATE);

        setField(fieldBuilder.build());
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return null;
        }

        val association = holder.getOneToOneAssociation();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        val arguments = dataFetchingEnvironment.getArguments();
        val argumentValue = arguments.get(association.getId());
        val argumentManagedObject = ManagedObject.adaptProperty(association, argumentValue);

        val valid = association.isAssociationValid(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);
        return valid.isVetoed() ? valid.getReasonAsString().orElse("invalid") : null;
    }

}
