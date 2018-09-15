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

package org.apache.isis.core.metamodel.facets.collections.disabled.fromimmutable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;

public class DisabledFacetOnCollectionDerivedFromImmutable extends DisabledFacetAbstract {

    public DisabledFacetOnCollectionDerivedFromImmutable(final ImmutableFacet immutableFacet, final FacetHolder holder) {
        super(Where.ANYWHERE, holder);
    }

    @Override
    public String disabledReason(final ObjectAdapter target) {
        return "Immutable";
    }

}
