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

import java.util.Map;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.exceptions.InvalidException;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.OneToOneAssociationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

public class GqlvPropertySet extends GqlvAbstract {

    final Holder holder;

    public GqlvPropertySet(
            final Holder holder,
            final Context context) {
        super(context);
        this.holder = holder;

        // setters return void, so we return the domain object instead
        val graphQLOutputType = this.context.typeMapper.outputTypeFor(holder.getObjectSpecification(), SchemaType.RICH);

        val fieldBuilder = newFieldDefinition()
                .name("set")
                .type(graphQLOutputType);
        holder.addGqlArgument(holder.getOneToOneAssociation(), fieldBuilder, TypeMapper.InputContext.INVOKE);
        setField(fieldBuilder.build());
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            return null;
        }

        val association = holder.getOneToOneAssociation();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);

        Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
        Object argumentValue = arguments.get(association.getId());
        ManagedObject argumentManagedObject = ManagedObject.adaptProperty(association, argumentValue);

        val visibleConsent = association.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visibleConsent.isVetoed()) {
            throw new HiddenException(association.getFeatureIdentifier());
        }

        val usableConsent = association.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usableConsent.isVetoed()) {
            throw new DisabledException(association.getFeatureIdentifier());
        }

        val validityConsent = association.isAssociationValid(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new InvalidException(validityConsent);
        }

        association.set(managedObject, argumentManagedObject, InteractionInitiatedBy.USER);

        return managedObject; // return the original object because setters return void
    }

    public interface Holder
            extends ObjectSpecificationProvider,
                    OneToOneAssociationProvider,
                    SchemaTypeProvider {

        void addGqlArgument(OneToOneAssociation oneToOneAssociation, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext);
    }
}
