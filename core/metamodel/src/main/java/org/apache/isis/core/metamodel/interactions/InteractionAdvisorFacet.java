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

import java.util.Map;

import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionAdvisor;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

/**
 * Used by {@link Consent} (specifically the main implementations {@link Allow}
 * and {@link Veto}), with the idea being that the only things that can create
 * {@link Consent} objects are {@link Facet}s.
 * 
 * <p>
 * TODO: note, this is a work-in-progress, because the DnD viewer in particular
 * creates its own {@link Allow}s and {@link Veto}s. The constructors that it
 * uses have been deprecated to flag that the DnD logic should move into
 * {@link Facet}s that implement this interface.
 * 
 * @author Dan Haywood
 * 
 */
public interface InteractionAdvisorFacet extends InteractionAdvisor, Facet {

    /**
     * For testing purposes only.
     */
    public static InteractionAdvisorFacet NOOP = new InteractionAdvisorFacet() {
        @Override
        public void appendAttributesTo(final Map<String, Object> attributeMap) {
        }

        @Override
        public boolean alwaysReplace() {
            return false;
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
        public boolean isNoop() {
            return true;
        }

        @Override
        public void setFacetHolder(final FacetHolder facetHolder) {
        }

        @Override
        public Facet getUnderlyingFacet() {
            return null;
        }

        @Override
        public void setUnderlyingFacet(final Facet underlyingFacet) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDerived() {
            return false;
        }

    };

}
