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

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract.Validating;

/**
 * Mix-in interface for {@link Facet}s that can advise as to whether a proposed
 * value is valid.
 *
 * <p>
 * For example, <tt>MaxLengthFacet</tt> does constrain the length of candidate
 * values, whereas <tt>DebugFacet</tt> or <tt>MemberOrderFacet</tt> do not -
 * they are basically just UI hints.
 *
 * @see DisablingInteractionAdvisor
 * @see HidingInteractionAdvisor
 */
public interface ValidatingInteractionAdvisor extends InteractionAdvisorFacet, Validating {

    /**
     * Whether the validation represented by this facet passes or fails.
     *
     * <p>
     * Implementations should use the provided {@link ValidityContext} to
     * determine whether they declare the interaction invalid. They must however
     * guard against a <tt>null</tt> {@link ValidityContext#getTarget() target} 
     * is not guaranteed to be populated.
     */
    String invalidates(ValidityContext ic);
}
