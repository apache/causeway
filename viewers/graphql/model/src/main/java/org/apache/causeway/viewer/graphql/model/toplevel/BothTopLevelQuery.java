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
package org.apache.causeway.viewer.graphql.model.toplevel;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.RichTopLevelQuery;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.SimpleTopLevelQuery;

public class BothTopLevelQuery extends ElementCustom {

    private final RichTopLevelQuery richSchema;
    private final SimpleTopLevelQuery simpleSchema;

    public BothTopLevelQuery(final Context context) {
        super("SimpleAndRich", context);

        addChildFieldFor(richSchema = new RichTopLevelQuery(context));
        addChildFieldFor(simpleSchema = new SimpleTopLevelQuery(context));

        buildObjectType();
    }

    @Override
    public void addDataFetchers() {
        addDataFetchersForChildren();
    }

    @Override
    protected void addDataFetchersForChildren() {
        richSchema.addDataFetcher(this);
        simpleSchema.addDataFetcher(this);
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return environment;
    }
}
