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
package org.apache.isis.core.metamodel.facets.properties.validating;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetAbstract;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * The mechanism by which the proposed value of a property can be validated,
 * called immediately before {@link PropertySetterFacetAbstract setting the
 * value}.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to invoking the
 * <tt>validateXxx</tt> method for a property with an accessor of
 * <tt>getXxx</tt>.
 *
 * @see PropertySetterFacet
 */

public interface PropertyValidateFacet extends Facet, ValidatingInteractionAdvisor {

    /**
     * The reason why the proposed value is invalid.
     *
     * <p>
     * Should return <tt>null</tt> if the value is in fact valid.
     */
    public String invalidReason(ManagedObject targetObject, ManagedObject proposedValue);
}
