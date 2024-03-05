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

import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import org.springframework.lang.Nullable;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Log4j2
public class RichActionInvokeTarget
        extends Element {

    @Getter private final ActionInteractor actionInteractor;

    public RichActionInvokeTarget(
            final ActionInteractor actionInteractor,
            final Context context) {
        super(context);
        this.actionInteractor = actionInteractor;

        val objectSpecification = actionInteractor.getObjectSpecification();

        val graphQLOutputType = context.typeMapper.outputTypeFor(objectSpecification, actionInteractor.getSchemaType());

        if (graphQLOutputType != null) {
            val fieldBuilder = newFieldDefinition()
                    .name("target")
                    .type(graphQLOutputType);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context).getTargetPojo();
    }

}
