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
package org.apache.causeway.core.metamodel.consent;

import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.InteractionAdvisorFacet;

import lombok.NonNull;

/**
 * Marker interface for implementations (specifically, {@link Facet}s) that can
 * advise as to whether a member should be disabled.
 *
 * Used within {@link Allow} and {@link Veto}.
 */
public interface InteractionAdvisor {

    /**
     * For testing purposes only.
     */
    public static InteractionAdvisor forTesting() {
        return new InteractionAdvisorFacet() {

            @Override
            public boolean semanticEquals(final @NonNull Facet other) {
                return this == other;
            }

            @Override
            public void visitAttributes(final BiConsumer<String, Object> visitor) {
            }

            @Override
            public Class<? extends Facet> facetType() {
                return null;
            }

            @Override
            public FacetHolder getFacetHolder() {
                return null;
            }

            @Override
            public Precedence getPrecedence() {
                return Facet.Precedence.FALLBACK;
            }

        };
    }

}
