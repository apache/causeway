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

import org.apache.isis.core.metamodel.facetapi.FacetAbstract.Hiding;

/**
 * Mix-in interface for facets that can advise as to whether a member should be
 * hidden.
 *
 * @see DisablingInteractionAdvisor
 * @see ValidatingInteractionAdvisor
 */
public interface HidingInteractionAdvisor extends InteractionAdvisorFacet, Hiding {

    /**
     * Whether the rule represented by this facet hides the member to which it
     * applies.
     *
     * <p>
     * Implementations should use the provided {@link InteractionContext} to
     * determine whether they declare the object/member is hidden. They must
     * however guard against a <tt>null</tt>
     * {@link InteractionContext#getTarget() target} is not guaranteed
     * to be populated.
     */
    String hides(VisibilityContext ic);
}
