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

package org.apache.isis.metamodel.facets.objectvalue.mandatory;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * Whether a property or a parameter is mandatory (not optional).
 *
 * <p>
 * For a mandatory property, the object cannot be saved/updated without the
 * value being provided. For a mandatory parameter, the action cannot be invoked
 * without the value being provided.
 *
 * <p>
 * In the standard Apache Isis Programming Model, specify mandatory by
 * <i>omitting</i> the <tt>@Optional</tt> annotation.
 */
public interface MandatoryFacet extends Facet, ValidatingInteractionAdvisor {

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
    public boolean isInvertedSemantics();
}
