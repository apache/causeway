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
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvTopLevelQueryRichSchema;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvTopLevelQuerySimpleSchema;

public class GqlvTopLevelQueryBothSchemas extends GqlvAbstractCustom {

    private final GqlvTopLevelQueryRichSchema richSchema;
    private final GqlvTopLevelQuerySimpleSchema simpleSchema;

    public GqlvTopLevelQueryBothSchemas(final Context context) {
        super("SimpleAndRich", context);

        addChildFieldFor(richSchema = new GqlvTopLevelQueryRichSchema(context));
        addChildFieldFor(simpleSchema = new GqlvTopLevelQuerySimpleSchema(context));

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
