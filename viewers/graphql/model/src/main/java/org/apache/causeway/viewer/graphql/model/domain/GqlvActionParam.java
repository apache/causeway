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

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojoFetcher;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionParam
        extends GqlvAbstractCustom
        implements GqlvActionParamHidden.Holder,
                   GqlvActionParamDisabled.Holder,
                   GqlvActionParamChoices.Holder,
                   GqlvActionParamAutoComplete.Holder,
                   GqlvActionParamDefault.Holder,
                   GqlvActionParamValidate.Holder {

    @Getter private final Holder holder;
    @Getter private final ObjectActionParameter objectActionParameter;
    @Getter private final int paramNum;

    private final GqlvActionParamHidden hidden;
    private final GqlvActionParamDisabled disabled;
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
    private final GqlvActionParamValidate validate;

    public GqlvActionParam(
            final Holder holder,
            final ObjectActionParameter objectActionParameter,
            final Context context,
            final int paramNum) {
        super(newObject().name(TypeNames.actionParamTypeNameFor(holder.getObjectSpecification(), objectActionParameter)), context);
        this.holder = holder;
        this.objectActionParameter = objectActionParameter;
        this.paramNum = paramNum;

        this.hidden = new GqlvActionParamHidden(this, context);
        addChildField(hidden.getField());
        this.disabled = new GqlvActionParamDisabled(this, context);
        addChildField(disabled.getField());

        val choices = new GqlvActionParamChoices(this, context);
        addChildField(choices.getField());
        if (choices.isFieldDefined()) {
            this.choices = choices;
        } else {
            this.choices = null;
        }

        val autoComplete = new GqlvActionParamAutoComplete(this, context);
        addChildField(autoComplete.getField());
        if (autoComplete.isFieldDefined()) {
            this.autoComplete = autoComplete;
        } else {
            this.autoComplete = null;
        }

        val default_ = new GqlvActionParamDefault(this, context);
        addChildField(default_.getField());
        if (default_.isFieldDefined()) {
            this.default_ = default_;
        } else {
            this.default_ = null;
        }

        this.validate = new GqlvActionParamValidate(this, context);
        addChildField(validate.getField());

        buildObjectTypeAndField(objectActionParameter.getId());
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

    public void addDataFetcher() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
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
    public void addGqlArguments(ObjectAction objectAction, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext, int paramNum) {
        holder.addGqlArguments(objectAction, fieldBuilder, inputContext, paramNum);
    }

    @Override
    public Can<ManagedObject> argumentManagedObjectsFor(DataFetchingEnvironment dataFetchingEnvironment, ObjectAction objectAction, BookmarkService bookmarkService) {
        return holder.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, bookmarkService);
    }

    @Override
    public void addGqlArgument(ObjectAction objectAction, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext, int paramNum) {
        // TODO: what lives here?
    }


    public interface Holder
            extends GqlvHolder,
                    ObjectSpecificationProvider,
                    ObjectActionProvider {

        void addGqlArguments(
                ObjectAction objectAction,
                GraphQLFieldDefinition.Builder fieldBuilder,
                TypeMapper.InputContext inputContext,
                int paramNum);

        Can<ManagedObject> argumentManagedObjectsFor(
                DataFetchingEnvironment dataFetchingEnvironment,
                ObjectAction objectAction,
                BookmarkService bookmarkService);
    }
}
