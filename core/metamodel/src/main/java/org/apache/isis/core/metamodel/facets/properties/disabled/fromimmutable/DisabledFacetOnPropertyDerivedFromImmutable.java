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

package org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable;

import java.util.function.Function;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.val;

public class DisabledFacetOnPropertyDerivedFromImmutable extends DisabledFacetAbstract {

    private final Function<ManagedObject, String> reasonProvider;

    public DisabledFacetOnPropertyDerivedFromImmutable(
            @NonNull final FacetHolder holder, 
            @NonNull final Function<ManagedObject, String> reasonProvider) {
        
        super(Where.ANYWHERE, holder);
        this.reasonProvider = reasonProvider;
    }

    @Override
    public String disabledReason(final ManagedObject target) {
        val reason = reasonProvider.apply(target);
        // ensure non empty reason
        return _Strings.isNotEmpty(reason) ? reason : "Immutable";
    }

    public static DisabledFacetOnPropertyDerivedFromImmutable forImmutable(
            @NonNull final FacetedMethod facetedMethodFor,
            @NonNull final ImmutableFacet immutableFacet) {
        
        return new DisabledFacetOnPropertyDerivedFromImmutable(facetedMethodFor, immutableFacet::disabledReason);
    }

}
