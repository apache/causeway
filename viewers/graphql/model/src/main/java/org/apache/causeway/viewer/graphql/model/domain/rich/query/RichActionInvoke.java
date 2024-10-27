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

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RichActionInvoke
        extends ElementCustom {

    private final RichActionInvokeTarget target;
    private final RichActionInvokeArgs args;
    private final RichActionInvokeResult result;

    public RichActionInvoke(
            final ActionInteractor actionInteractor,
            final Context context) {
        super(TypeNames.actionInvokeTypeNameFor(actionInteractor.getObjectSpecification(), actionInteractor.getObjectMember(), actionInteractor.getSchemaType()), context);

        if(isBuilt()) {
            this.target = null;
            this.args = null;
            this.result = null;
            return;
        }

        addChildFieldFor(this.target = new RichActionInvokeTarget(actionInteractor, context));
        addChildFieldFor(this.args = new RichActionInvokeArgs(actionInteractor, context));
        addChildFieldFor(this.result = new RichActionInvokeResult(actionInteractor, context));

        var gqlObjectType = buildObjectType();
        var objectAction = actionInteractor.getObjectMember();
        var fieldBuilder = newFieldDefinition()
                .name(fieldNameForSemanticsOf(objectAction))
                .type(gqlObjectType);
        actionInteractor.addGqlArguments(objectAction, fieldBuilder, TypeMapper.InputContext.INVOKE, objectAction.getParameterCount());
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
        target.addDataFetcher(this);
        args.addDataFetcher(this);
        result.addDataFetcher(this);
    }

}
