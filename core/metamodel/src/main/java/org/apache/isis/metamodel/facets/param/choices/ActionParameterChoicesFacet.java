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

package org.apache.isis.metamodel.facets.param.choices;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * Obtain choices for each of the parameters of the action.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to invoking the
 * <tt>choicesNXxx</tt> support method for an action (where N is the 0-based
 * parameter number).
 */
public interface ActionParameterChoicesFacet extends Facet {

    public Object[] getChoices(
            ManagedObject target,
            Can<ManagedObject> dependentArgs,
            InteractionInitiatedBy interactionInitiatedBy);
}
