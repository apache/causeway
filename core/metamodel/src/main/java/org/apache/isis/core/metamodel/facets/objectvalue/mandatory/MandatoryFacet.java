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

package org.apache.isis.core.metamodel.facets.objectvalue.mandatory;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.val;

/**
 * Whether a property or a parameter is mandatory (not optional).
 *
 * <p>
 * For a mandatory property, the object cannot be saved/updated without the
 * value being provided. For a mandatory parameter, the action cannot be invoked
 * without the value being provided.
 *
 */
public interface MandatoryFacet
extends Facet, ValidatingInteractionAdvisor {

    public enum Semantics {
        REQUIRED,
        OPTIONAL;

        public static Semantics of(boolean required) {
            return required ? REQUIRED: OPTIONAL;
        }

        public boolean isRequired() {
            return this == REQUIRED;
        }

        public boolean isOptional() {
            return this == OPTIONAL;
        }
    }

    /**
     * Whether this value is required but has not been provided (and is
     * therefore invalid).
     *
     * <p>
     * If the value has been provided, <i>or</i> if the property or parameter is
     * not required, then will return <tt>false</tt>.
     */
    boolean isRequiredButNull(ManagedObject adapter);

    /**
     * Indicates that the implementation is overriding the usual semantics, in
     * other words that the {@link FacetHolder} to which this {@link Facet} is
     * attached is <i>not</i> mandatory.
     */
    public Semantics getSemantics();

    static boolean isMandatory(final @NonNull FacetHolder facetHolder) {
        val mandatoryFacet = facetHolder.getFacet(MandatoryFacet.class);
        if(mandatoryFacet == null) {
            // absence of the mandatory facet indicates OPTIONAL
            return false;
        }
        return mandatoryFacet.getSemantics()==null
                || mandatoryFacet.getSemantics().isRequired();
    }
}
