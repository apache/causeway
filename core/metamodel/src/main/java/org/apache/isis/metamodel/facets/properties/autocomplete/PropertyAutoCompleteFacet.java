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

package org.apache.isis.metamodel.facets.properties.autocomplete;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;

/**
 * Provides a set of auto-complete choices for a property.
 *
 * <p>
 * Viewers would typically represent this as a drop-down list box for the
 * property.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to the
 * <tt>autoCompletexx</tt> supporting method for the property with accessor
 * <tt>getXxx</tt>.
 */
public interface PropertyAutoCompleteFacet extends Facet {

    /**
     * Gets the available auto-complete choices for this property.
     */
    public Object[] autoComplete(
            final ObjectAdapter inObject,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy);
    /**
     * The minimum number of characters that need to be entered.
     */
    public int getMinLength();
}
