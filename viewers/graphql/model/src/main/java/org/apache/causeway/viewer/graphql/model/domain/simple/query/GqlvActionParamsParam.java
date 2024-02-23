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
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionParamsParam
        extends GqlvAbstractCustom
        implements GqlvActionParamsParamHidden.Holder,
                   GqlvActionParamsParamDisabled.Holder,
                   GqlvActionParamsParamChoices.Holder,
                   GqlvActionParamsParamAutoComplete.Holder,
                   GqlvActionParamsParamDefault.Holder,
                   GqlvActionParamsParamValidate.Holder,
                   GqlvActionParamsParamDatatype.Holder {

    @Getter private final Holder holder;
    @Getter private final ObjectActionParameter objectActionParameter;
    @Getter private final int paramNum;

    private final GqlvActionParamsParamHidden hidden;
    private final GqlvActionParamsParamDisabled disabled;
    /**
     * Populated iff there are choices for this param
     */
    private final GqlvActionParamsParamChoices choices;
    /**
     * Populated iff there is an autocomplete for this param
     */
    private final GqlvActionParamsParamAutoComplete autoComplete;
    /**
     * Populated iff there is a default for this param
     */
    private final GqlvActionParamsParamDefault default_;
    private final GqlvActionParamsParamValidate validate;
    private final GqlvActionParamsParamDatatype datatype;

    public GqlvActionParamsParam(
            final Holder holder,
            final ObjectActionParameter oap,
            final Context context,
            final int paramNum) {
        super(TypeNames.actionParamTypeNameFor(holder.getObjectSpecification(), oap, holder.getSchemaType()), context);
        this.holder = holder;
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


        addChildFieldFor(this.hidden = new GqlvActionParamsParamHidden(this, context));
        addChildFieldFor(this.disabled = new GqlvActionParamsParamDisabled(this, context));
        addChildFieldFor(this.choices = new GqlvActionParamsParamChoices(this, context));
        addChildFieldFor(this.autoComplete = new GqlvActionParamsParamAutoComplete(this, context));
        addChildFieldFor(this.default_ = new GqlvActionParamsParamDefault(this, context));
        addChildFieldFor(this.validate = new GqlvActionParamsParamValidate(this, context));
        addChildFieldFor(this.datatype = new GqlvActionParamsParamDatatype(this, context));

        buildObjectTypeAndField(oap.getId(), oap.getCanonicalDescription().orElse(oap.getCanonicalFriendlyName()));
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    @Override
    public ObjectAction getObjectMember() {
        return holder.getObjectMember();
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
        holder.addGqlArguments(objectAction, fieldBuilder, inputContext, paramNum);
    }

    @Override
    public Can<ManagedObject> argumentManagedObjectsFor(
            final Environment environment,
            final ObjectAction objectAction,
            final BookmarkService bookmarkService) {
        return holder.argumentManagedObjectsFor(environment, objectAction, bookmarkService);
    }

    @Override
    public void addGqlArgument(ObjectAction objectAction, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext, int paramNum) {
        // TODO: what lives here?
    }

    @Override
    public SchemaType getSchemaType() {
        return holder.getSchemaType();
    }

    public interface Holder
            extends ObjectSpecificationProvider,
                    ObjectActionProvider,
                    SchemaTypeProvider {

        void addGqlArguments(
                ObjectAction objectAction,
                GraphQLFieldDefinition.Builder fieldBuilder,
                TypeMapper.InputContext inputContext,
                int paramNum);

        Can<ManagedObject> argumentManagedObjectsFor(
                Environment dataFetchingEnvironment,
                ObjectAction objectAction,
                BookmarkService bookmarkService);
    }
}
