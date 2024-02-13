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

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class GqlvMetaSaveAs extends GqlvAbstract {

    public GqlvMetaSaveAs(final Context context) {
        super(context);

        setField(newFieldDefinition()
                    .name("saveAs")
                    .type(Scalars.GraphQLString)
                    .argument(new GraphQLArgument.Builder()
                            .name("ref")
                            .type(Scalars.GraphQLString)
                    )
                    .build());
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        String ref = environment.getArgument("ref");
        GqlvMeta.Fetcher source = environment.getSource();
        environment.getGraphQlContext().put(keyFor(ref), new BookmarkedPojo(source.bookmark(), context.bookmarkService));
        return ref;
    }

    public static String keyFor(String ref) {
        return GqlvMetaSaveAs.class.getName() + "#" + ref;
    }

}
