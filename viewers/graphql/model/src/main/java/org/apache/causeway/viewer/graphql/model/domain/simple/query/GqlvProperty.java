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

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.PropertyInteractor;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvPropertyAutoComplete;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvPropertyChoices;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvPropertyDatatype;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvPropertySet;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvPropertyValidate;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectMemberProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;

public class GqlvProperty
        extends GqlvAssociation<OneToOneAssociation, ObjectInteractor>
        implements
        MemberInteractor<OneToOneAssociation>,
        PropertyInteractor,
        ObjectSpecificationProvider, ObjectMemberProvider<OneToOneAssociation>, SchemaTypeProvider {

    private final GqlvAbstract get;

    public GqlvProperty(
            final ObjectInteractor holder,
            final OneToOneAssociation otoa,
            final Context context) {
        super(holder, otoa, TypeNames.propertyTypeNameFor(holder.getObjectSpecification(), otoa, holder.getSchemaType()), context);

        if (isBuilt()) {
            this.get = null;
            return;
        }

        addChildFieldFor(
                this.get = isBlob()
                            ? new GqlvPropertyGetBlob(this, context)
                            : isClob()
                                ? new GqlvPropertyGetClob(this, context)
                                : new GqlvPropertyGet(this, context)
        );

        buildObjectTypeAndField(otoa.getId(), otoa.getCanonicalDescription().orElse(otoa.getCanonicalFriendlyName()));
    }

    private boolean isSetterAllowed() {
        val apiVariant = context.causewayConfiguration.getViewer().getGraphql().getApiVariant();
        switch (apiVariant) {
            case QUERY_ONLY:
            case QUERY_AND_MUTATIONS:
                return false;
            case QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT:
                return true;
            default:
                // shouldn't happen
                throw new IllegalArgumentException("Unknown API variant: " + apiVariant);
        }
    }



    private boolean isBlob() {
        return getOneToOneAssociation().getElementType().getCorrespondingClass() == Blob.class;
    }

    private boolean isClob() {
        return getOneToOneAssociation().getElementType().getCorrespondingClass() == Clob.class;
    }

    public void addGqlArgument(
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLFieldDefinition.Builder builder,
            final TypeMapper.InputContext inputContext) {
        builder.argument(gqlArgumentFor(oneToOneAssociation, inputContext));
    }

    private GraphQLArgument gqlArgumentFor(
            final OneToOneAssociation oneToOneAssociation,
            final TypeMapper.InputContext inputContext) {
        return GraphQLArgument.newArgument()
                .name(oneToOneAssociation.getId())
                .type(context.typeMapper.inputTypeFor(oneToOneAssociation, inputContext, SchemaType.RICH))
                .build();
    }


    @Override
    public ObjectSpecification getObjectSpecification() {
        return interactor.getObjectSpecification();
    }

    public OneToOneAssociation getOneToOneAssociation() {
        return getObjectMember();
    }

    @Override
    protected void addDataFetchersForChildren() {
        get.addDataFetcher(this);
    }

    @Override
    public SchemaType getSchemaType() {
        return interactor.getSchemaType();
    }

}
