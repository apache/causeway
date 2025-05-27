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

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RichActionInvokeTarget
        extends Element {

    @Getter private final ActionInteractor actionInteractor;

    public RichActionInvokeTarget(
            final ActionInteractor actionInteractor,
            final Context context) {
        super(context);
        this.actionInteractor = actionInteractor;

        var objectSpecification = actionInteractor.getObjectSpecification();

        var graphQLOutputType = context.typeMapper.outputTypeFor(objectSpecification, actionInteractor.getSchemaType());

        if (graphQLOutputType != null) {
            var fieldBuilder = newFieldDefinition()
                    .name("target")
                    .type(graphQLOutputType);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {
        return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context).getTargetPojo();
    }

}
