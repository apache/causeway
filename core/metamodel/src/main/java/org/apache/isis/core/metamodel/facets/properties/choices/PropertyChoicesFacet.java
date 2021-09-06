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
package org.apache.isis.core.metamodel.facets.properties.choices;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Provides a set of choices for a property.
 *
 * <p>
 * Viewers would typically represent this as a drop-down list box for the
 * property.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to the
 * <tt>choicesXxx</tt> supporting method for the property with accessor
 * <tt>getXxx</tt>.
 *
 * <p>
 * Note: an alternative mechanism may be to use the <tt>@Bounded</tt> annotation
 * against the referenced class.
 */
public interface PropertyChoicesFacet extends Facet {

    /**
     * Gets the available choices for this property.
     */
    public Can<ManagedObject> getChoices(
            final ManagedObject adapter,
            final InteractionInitiatedBy interactionInitiatedBy);
}
