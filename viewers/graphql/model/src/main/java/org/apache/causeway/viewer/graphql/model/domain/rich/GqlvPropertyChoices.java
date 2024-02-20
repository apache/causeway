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
package org.apache.causeway.viewer.graphql.model.domain.rich;

import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.OneToOneAssociationProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

public class GqlvPropertyChoices extends GqlvAbstract {

    final Holder holder;

    public GqlvPropertyChoices(
            final Holder holder,
            final Context context) {
        super(context);
        this.holder = holder;

        val otoa = holder.getOneToOneAssociation();
        if (otoa.hasChoices()) {
            val elementType = otoa.getElementType();
            val fieldBuilder = newFieldDefinition()
                    .name("choices")
                    .type(GraphQLList.list(context.typeMapper.outputTypeFor(elementType)));
            holder.addGqlArgument(otoa, fieldBuilder, TypeMapper.InputContext.CHOICES);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
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

        val choicesManagedObject = association.getChoices(managedObject, InteractionInitiatedBy.USER);
        return choicesManagedObject.stream()
                    .map(ManagedObject::getPojo)
                    .collect(Collectors.toList());
    }

    public interface Holder
            extends ObjectSpecificationProvider,
                    OneToOneAssociationProvider {

        void addGqlArgument(OneToOneAssociation otoa, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext);
    }
}
