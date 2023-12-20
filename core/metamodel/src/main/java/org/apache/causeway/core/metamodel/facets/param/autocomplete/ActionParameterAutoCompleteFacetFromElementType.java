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
package org.apache.causeway.core.metamodel.facets.param.autocomplete;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.NonNull;

public class ActionParameterAutoCompleteFacetFromElementType
extends ActionParameterAutoCompleteFacetAbstract {

    public static Optional<ActionParameterAutoCompleteFacet> create(
            final @NonNull ObjectActionParameter param) {

        return param.getElementType()
                .lookupNonFallbackFacet(AutoCompleteFacet.class)
                .map(autoCompleteFacet->
                    new ActionParameterAutoCompleteFacetFromElementType(autoCompleteFacet, param.getFacetHolder()));
    }

    private final AutoCompleteFacet autoCompleteFacet;

    private ActionParameterAutoCompleteFacetFromElementType(
            final AutoCompleteFacet autoCompleteFacet,
            final FacetHolder holder) {
        super(holder, Precedence.INFERRED);
        this.autoCompleteFacet = autoCompleteFacet;
    }

    @Override
    public int getMinLength() {
        return autoCompleteFacet.getMinLength();
    }

    @Override
    public Can<ManagedObject> autoComplete(
            final ObjectSpecification elementSpec,
            final ManagedObject owningAdapter,
            final Can<ManagedObject> pendingArgs,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return autoCompleteFacet.execute(searchArg, interactionInitiatedBy);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        autoCompleteFacet.visitAttributes(visitor);
    }

}
