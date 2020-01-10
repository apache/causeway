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

package org.apache.isis.core.metamodel.interactions;

import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract.Disabling;

/**
 * Mix-in interface for facets that can advise as to whether a member should be
 * disabled.
 *
 * @see ValidatingInteractionAdvisor
 * @see HidingInteractionAdvisor
 */
public interface DisablingInteractionAdvisor extends InteractionAdvisorFacet, Disabling {

    /**
     * Whether the rule represented by this facet disables the member to which
     * it applies.
     *
     * <p>
     * Implementations should use the provided {@link InteractionContext} to
     * determine whether they disable the member from being modified or used.
     * They must however guard against a <tt>null</tt>
     * {@link InteractionContext#getTarget() target} - this is not guaranteed to be populated.
     */
    String disables(final UsabilityContext<? extends UsabilityEvent> ic);

}
