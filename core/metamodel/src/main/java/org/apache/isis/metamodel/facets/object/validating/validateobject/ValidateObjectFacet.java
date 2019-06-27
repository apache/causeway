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

package org.apache.isis.metamodel.facets.object.validating.validateobject;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * Mechanism for determining whether this object is in a valid state, for
 * example so that it can be persisted or updated.
 *
 * <p>
 * Even though all the properties of an object may themselves be valid, there
 * could be inter-property dependencies which are invalid. For example
 * <tt>fromDate</tt> > <tt>toDate</tt> would probably represent an invalid
 * state.
 *
 * <p>
 * In the standard Apache Isis Programming Model, typically corresponds to the
 * <tt>validate</tt> method.
 *
 * @see PersistingCallbackFacet
 * @see UpdatingCallbackFacet
 */
public interface ValidateObjectFacet extends Facet, ValidatingInteractionAdvisor {

    /**
     * The reason the object is invalid.
     *
     * <p>
     * . If the object is actually valid, should return <tt>null</tt>.
     */
    public String invalidReason(ManagedObject adapter);

}
