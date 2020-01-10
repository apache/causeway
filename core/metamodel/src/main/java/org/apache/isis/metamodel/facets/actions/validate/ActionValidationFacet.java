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

package org.apache.isis.metamodel.facets.actions.validate;

import java.util.List;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * The mechanism by which the set of parameters of the action can be validated
 * before the action itself is invoked.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to invoking the
 * <tt>validateXxx</tt> support method for an action.
 *
 * <p>
 * Note that the parameters may be validated independently first (eg a range
 * check on a numeric parameter).
 *
 * @see ActionInvocationFacet
 */
public interface ActionValidationFacet extends Facet, ValidatingInteractionAdvisor {

    /**
     * Reason why the validation has failed, or <tt>null</tt> if okay.
     */
    public String invalidReason(ManagedObject target, List<ManagedObject> arguments);
}
