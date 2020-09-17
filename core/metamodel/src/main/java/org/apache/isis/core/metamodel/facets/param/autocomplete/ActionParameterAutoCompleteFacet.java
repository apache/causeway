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

package org.apache.isis.core.metamodel.facets.param.autocomplete;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Obtain auto-completion for each of the parameters of the action.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to invoking the
 * <tt>autoCompleteNXxx(..., String)</tt> support method for an action (where N is the 0-based
 * parameter number).
 */
public interface ActionParameterAutoCompleteFacet extends Facet {

    public Can<ManagedObject> autoComplete(
            ManagedObject inObject,
            Can<ManagedObject> pendingArgs,
            String searchArg,
            InteractionInitiatedBy interactionInitiatedBy);
    /**
     * The minimum number of characters that need to be entered.
     */
    public int getMinLength();
}
