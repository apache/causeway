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
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionParamInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionParameterProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RichActionParamsParam
        extends ElementCustom
        implements ActionParamInteractor,
                   ObjectActionParameterProvider {

    @Getter private final ActionInteractor actionInteractor;
    @Getter private final ObjectActionParameter objectActionParameter;
    @Getter private final int paramNum;

    private final RichActionParamsParamHidden hidden;
    private final RichActionParamsParamDisabled disabled;
    /**
     * Populated iff there are choices for this param
     */
    private final RichActionParamsParamChoices choices;
    /**
     * Populated iff there is an autocomplete for this param
     */
    private final RichActionParamsParamAutoComplete autoComplete;
    /**
     * Populated iff there is a default for this param
     */
    private final RichActionParamsParamDefault default_;
    private final RichActionParamsParamValidate validate;
    private final RichActionParamsParamDatatype datatype;

    public RichActionParamsParam(
            final ActionInteractor holder,
            final ObjectActionParameter oap,
            final Context context,
            final int paramNum) {
        super(TypeNames.actionParamTypeNameFor(holder.getObjectSpecification(), oap, holder.getSchemaType()), context);
        this.actionInteractor = holder;
        this.objectActionParameter = oap;
        this.paramNum = paramNum;

        if (isBuilt()) {
            this.hidden = null;
            this.disabled = null;
            this.choices = null;
            this.autoComplete = null;
            this.default_ = null;
            this.validate = null;
            this.datatype = null;

            // nothing else to be done
            return;
        }

        addChildFieldFor(this.hidden = new RichActionParamsParamHidden(this, context));
        addChildFieldFor(this.disabled = new RichActionParamsParamDisabled(this, context));
        addChildFieldFor(this.choices = new RichActionParamsParamChoices(this, context));
        addChildFieldFor(this.autoComplete = new RichActionParamsParamAutoComplete(this, context));
        addChildFieldFor(this.default_ = new RichActionParamsParamDefault(this, context));
        addChildFieldFor(this.validate = new RichActionParamsParamValidate(this, context));
        addChildFieldFor(this.datatype = new RichActionParamsParamDatatype(this, context));

        buildObjectTypeAndField(oap.asciiId(), oap.getCanonicalDescription().orElse(oap.getCanonicalFriendlyName()));
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return actionInteractor.getObjectSpecification();
    }

    @Override
    public ObjectAction getObjectMember() {
        return actionInteractor.getObjectMember();
    }

    @Override
    protected void addDataFetchersForChildren() {

        if (hidden == null) {
            return;
        }

        hidden.addDataFetcher(this);
        disabled.addDataFetcher(this);

        if (choices != null) {
            choices.addDataFetcher(this);
        }

        if (autoComplete != null) {
            autoComplete.addDataFetcher(this);
        }

        if (default_ != null) {
            default_.addDataFetcher(this);
        }

        validate.addDataFetcher(this);

        datatype.addDataFetcher(this);
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context);
    }

    @Override
    public void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder fieldBuilder,
            final TypeMapper.InputContext inputContext,
            final int paramNum) {
        actionInteractor.addGqlArguments(objectAction, fieldBuilder, inputContext, paramNum);
    }

    @Override
    public Can<ManagedObject> argumentManagedObjectsFor(
            final Environment environment,
            final ObjectAction objectAction,
            final BookmarkService bookmarkService) {
        return actionInteractor.argumentManagedObjectsFor(environment, objectAction, bookmarkService);
    }

    @Override
    public void addGqlArgument(ObjectAction objectAction, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext, int paramNum) {
        // TODO: what lives here?
    }

    @Override
    public SchemaType getSchemaType() {
        return actionInteractor.getSchemaType();
    }

}
