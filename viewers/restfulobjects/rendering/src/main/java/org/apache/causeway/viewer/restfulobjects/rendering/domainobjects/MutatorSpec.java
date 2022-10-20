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
package org.apache.causeway.viewer.restfulobjects.rendering.domainobjects;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulHttpMethod;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class MutatorSpec {

    public static MutatorSpec of(
            final Rel rel,
            final Class<? extends Facet> mutatorFacetType,
            final RestfulHttpMethod httpMethod,
            final BodyArgs argSpec) {
        return of(rel, mutatorFacetType, httpMethod, argSpec, null);
    }

    public final Rel rel;
    private final Class<? extends Facet> mutatorFacetType;
    public final RestfulHttpMethod httpMethod;
    public final BodyArgs arguments;
    public final String suffix;

    public boolean appliesTo(final ObjectMember objectMember) {
        return objectMember.containsFacet(mutatorFacetType);
    }

}