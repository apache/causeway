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

import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public class RichPropertyGetBlobBytes extends RichPropertyGetBlobAbstract {

    private final String graphqlPath;

    public RichPropertyGetBlobBytes(
            final MemberInteractor<OneToOneAssociation> memberInteractor,
            final Context context) {
        super(memberInteractor, context, "bytes");

        this.graphqlPath = context.causewayConfiguration.valueOf("spring.graphql.path").orElse("/graphql");
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        var sourcePojo = BookmarkedPojo.sourceFrom(environment);

        var bookmarkIfAny = context.bookmarkService.bookmarkFor(sourcePojo);
        return bookmarkIfAny.map(x -> {
            final ObjectFeature objectFeature = memberInteractor.getObjectMember();
            return String.format(
                    "//%s/object/%s:%s/%s/blobBytes", graphqlPath, x.logicalTypeName(), x.identifier(), objectFeature.asciiId());
        }).orElse(null);

    }

}
