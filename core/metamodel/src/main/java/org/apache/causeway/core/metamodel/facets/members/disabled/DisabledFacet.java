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
package org.apache.causeway.core.metamodel.facets.members.disabled;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.WhereValueFacet;
import org.apache.causeway.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;

/**
 * Disable a property, collection or action.
 *
 * <p>
 * In the standard Apache Causeway Programming Model, corresponds to annotating the
 * member with <tt>@Disabled</tt>.
 */
public interface DisabledFacet
extends WhereValueFacet, DisablingInteractionAdvisor {

    public enum Semantics {

        /** regular semantics */
        DISABLED,

        /** inverted semantics */
        ENABLED;

        public boolean isDisabled() {
            return this == DISABLED;
        }

        public boolean isEnabled() {
            return this == ENABLED;
        }
    }

    /**
     * "Special" phrase returned for facets which are always disabled.
     */
    String ALWAYS_DISABLED_REASON = "Always disabled";

    /**
     * The reason why the (feature of the) target object is currently disabled,
     * or <tt>null</tt> if enabled.
     */
    @Nullable
    public String disabledReason(ManagedObject target);

    public Semantics getSemantics();

    // -- PREDICATES

    static boolean isAlwaysDisabled(final @NonNull FacetHolder facetHolder) {
        return WhereValueFacet.isAlways(facetHolder, DisabledFacet.class);
    }
}
