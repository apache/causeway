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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RichActionParams
        extends ElementCustom {

    @Getter private final ActionInteractor actionInteractor;

    private final List<RichActionParamsParam> params = new ArrayList<>();

    public RichActionParams(
            final ActionInteractor actionInteractor,
            final Context context) {
        super(TypeNames.actionParamsTypeNameFor(actionInteractor.getObjectSpecification(), actionInteractor.getObjectMember(), actionInteractor.getSchemaType()), context);
        this.actionInteractor = actionInteractor;

        if (isBuilt()) {
            // nothing else to be done
            return;
        }

        var idx = new AtomicInteger(0);
        actionInteractor.getObjectMember().getParameters()
                .forEach(oap -> params.add(addChildFieldFor(new RichActionParamsParam(actionInteractor, oap, this.context, idx.getAndIncrement()))));

        if (params.isEmpty()) {
            return;
        }

        buildObjectTypeAndField("params", "Parameters of this action");
    }

    @Override
    protected void addDataFetchersForChildren() {
        params.forEach(param -> param.addDataFetcher(this));
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context);
    }

}
