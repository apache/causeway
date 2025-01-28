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
package org.apache.causeway.core.metamodel.facets.properties.disabled.fromimmutable;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import org.jspecify.annotations.NonNull;

public class DisabledFacetOnPropertyFromImmutable
extends DisabledFacetAbstract {

    // -- FACTORY

    public static DisabledFacetOnPropertyFromImmutable forImmutable(
            final @NonNull FacetedMethod facetedMethodFor,
            final @NonNull ImmutableFacet immutableFacet) {

        return new DisabledFacetOnPropertyFromImmutable(facetedMethodFor, immutableFacet);
    }

    // -- FIELDS

    private final ImmutableFacet reasonProvidingImmutableFacet;

    // -- CONSTRUCTOR

    private DisabledFacetOnPropertyFromImmutable(
            final @NonNull FacetHolder holder,
            final @NonNull ImmutableFacet reasonProvidingImmutableFacet) {

        super(Where.ANYWHERE, VetoReason.delegatedTo(reasonProvidingImmutableFacet.getClass()),
                holder);

        this.reasonProvidingImmutableFacet = reasonProvidingImmutableFacet;
    }

    @Override
    public Optional<VetoReason> disabledReason(final ManagedObject target) {
        var reason = reasonProvidingImmutableFacet.disabledReason(target);
        // ensure non empty reason
        return reason
                .or(()->VetoReason.immutableIfNoReasonGivenByImmutableFacet().toOptional());
    }

}
