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

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionInvoke
        extends GqlvAbstractCustom
        implements HolderActionInvokeResult,
        HolderActionInvoke {

    private final HolderActionInvoke holder;
    private final GqlvActionInvokeResult result;
    private final GqlvActionInvokeArgs args;

    public GqlvActionInvoke(
            final HolderActionInvoke holder,
            final Context context) {
        super(TypeNames.actionInvokeTypeNameFor(holder.getObjectSpecification(), holder.getObjectAction(), holder.getSchemaType()), context);

        this.holder = holder;

        if(isBuilt()) {
            this.result = null;
            this.args = null;
            return;
        }

        addChildFieldFor(this.result = new GqlvActionInvokeResult(this, context));
        addChildFieldFor(this.args = new GqlvActionInvokeArgs(this, context));

        val gqlObjectType = buildObjectType();
        val objectAction = holder.getObjectAction();
        val fieldBuilder = newFieldDefinition()
                .name(fieldNameForSemanticsOf(objectAction))
                .type(gqlObjectType);
        holder.addGqlArguments(objectAction, fieldBuilder, TypeMapper.InputContext.INVOKE, objectAction.getParameterCount());
        setField(fieldBuilder.build());
    }

    private static String fieldNameForSemanticsOf(ObjectAction objectAction) {
        switch (objectAction.getSemantics()) {
            case SAFE_AND_REQUEST_CACHEABLE:
            case SAFE:
                return "invoke";
            case IDEMPOTENT:
            case IDEMPOTENT_ARE_YOU_SURE:
                return "invokeIdempotent";
            case NON_IDEMPOTENT:
            case NON_IDEMPOTENT_ARE_YOU_SURE:
            case NOT_SPECIFIED:
            default:
                return "invokeNonIdempotent";
        }
    }


    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        // make args available to child fields
        dataFetchingEnvironment.getGraphQlContext().put("arguments", dataFetchingEnvironment.getArguments());

        return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context);
    }

    @Override
    protected void addDataFetchersForChildren() {
        result.addDataFetcher(this);
        args.addDataFetcher(this);
    }

    @Override
    public void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder fieldBuilder,
            final TypeMapper.InputContext inputContext,
            final int parameterCount) {
        holder.addGqlArguments(objectAction, fieldBuilder, inputContext, parameterCount);
    }

    @Override
    public Can<ManagedObject> argumentManagedObjectsFor(
            final Environment environment,
            final ObjectAction objectAction,
            final BookmarkService bookmarkService) {
        return holder.argumentManagedObjectsFor(environment, objectAction, bookmarkService);
    }

    @Override
    public ObjectAction getObjectAction() {
        return holder.getObjectAction();
    }

    @Override
    public ObjectAction getObjectMember() {
        return holder.getObjectMember();
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    @Override
    public SchemaType getSchemaType() {
        return holder.getSchemaType();
    }

}
