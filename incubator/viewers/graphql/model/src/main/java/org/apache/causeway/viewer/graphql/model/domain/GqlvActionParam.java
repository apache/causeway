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
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojoFetcher;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Log4j2
public class GqlvActionParam
        implements GqlvActionParamHidden.Holder,
                   GqlvActionParamDisabled.Holder,
                   GqlvActionParamChoices.Holder,
                   GqlvActionParamAutoComplete.Holder,
                   GqlvActionParamDefault.Holder,
                   GqlvActionParamValidate.Holder {

    @Getter private final Holder holder;
    @Getter private final ObjectActionParameter objectActionParameter;
    private final Context context;
    @Getter private final int paramNum;

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;

    private final GqlvActionParamHidden hidden;
    private final GqlvActionParamDisabled validate;
    /**
     * Populated iff there are choices for this param
     */
    private final GqlvActionParamChoices choices;
    /**
     * Populated iff there is an autocomplete for this param
     */
    private final GqlvActionParamAutoComplete autoComplete;
    /**
     * Populated iff there is a default for this param
     */
    private final GqlvActionParamDefault default_;
    private final GqlvActionParamValidate disabled;

    private final GraphQLFieldDefinition field;

    public GqlvActionParam(
            final Holder holder,
            final ObjectActionParameter objectActionParameter,
            final Context context,
            final int paramNum) {
        this.holder = holder;
        this.objectActionParameter = objectActionParameter;
        this.context = context;
        this.paramNum = paramNum;
        this.gqlObjectTypeBuilder = newObject().name(TypeNames.actionParamTypeNameFor(holder.getObjectSpecification(), objectActionParameter));

        this.hidden = new GqlvActionParamHidden(this, context);
        this.disabled = new GqlvActionParamValidate(this, context);
        val choices = new GqlvActionParamChoices(this, context);
        this.choices = choices.hasChoices() ? choices : null;
        val autoComplete = new GqlvActionParamAutoComplete(this, context);
        this.autoComplete = autoComplete.hasAutoComplete() ? autoComplete : null;
        val default_ = new GqlvActionParamDefault(this, context);
        this.default_ = default_.hasDefault() ? default_ : null;
        this.validate = new GqlvActionParamDisabled(this, context);

        this.gqlObjectType = gqlObjectTypeBuilder.build();

        this.field = holder.addField(newFieldDefinition()
                        .name(objectActionParameter.getId())
                        .type(gqlObjectTypeBuilder)
                        .build());
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    @Override
    public ObjectAction getObjectMember() {
        return getObjectAction();
    }

    @Override
    public ObjectAction getObjectAction() {
        return holder.getObjectAction();
    }

    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }

    public void addDataFetcher() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                new BookmarkedPojoFetcher(context.bookmarkService));

        hidden.addDataFetcher();
        disabled.addDataFetcher();
        if (choices != null) {
            choices.addDataFetcher();
        }
        if (autoComplete != null) {
            autoComplete.addDataFetcher();
        }
        if (default_ != null) {
            default_.addDataFetcher();
        }
        validate.addDataFetcher();
    }


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }


    public interface Holder
            extends GqlvHolder,
                    ObjectSpecificationProvider,
                    ObjectActionProvider {

    }
}
