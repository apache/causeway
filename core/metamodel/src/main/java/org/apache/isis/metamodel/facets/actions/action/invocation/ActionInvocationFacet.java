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

package org.apache.isis.metamodel.facets.actions.action.invocation;

import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;

/**
 * Represents the mechanism by which the action should be invoked.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to invoking the
 * actual action method itself (a <tt>public</tt> method that does not represent
 * a property, a collection or any of the supporting methods).
 */
public interface ActionInvocationFacet extends Facet {

    ManagedObject invoke(
            ObjectAction owningAction,
            ManagedObject targetAdapter,
            ManagedObject mixedInAdapter,  // null for regular or contributed (not mixin) actions
            ManagedObject[] argumentAdapters,
            InteractionInitiatedBy interactionInitiatedBy);
    
    ObjectSpecification getReturnType();

    ObjectSpecification getOnType();

}
