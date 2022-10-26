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
package org.apache.causeway.core.metamodel.facets.members.disabled.method;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

public abstract class DisableForContextFacetAbstract extends FacetAbstract implements DisableForContextFacet {

    private static final Class<? extends Facet> type() {
        return DisableForContextFacet.class;
    }

    public DisableForContextFacetAbstract(final FacetHolder holder) {
        super(type(), holder);
    }

    public DisableForContextFacetAbstract(final FacetHolder holder, final Facet.Precedence precedence) {
        super(type(), holder, precedence);
    }

}
