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
package org.apache.causeway.core.metamodel.facets.param.hide;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 * The mechanism by which a single parameter of the action can be hidden
 * before the action itself is invoked.
 *
 * <p>
 * In the standard Apache Causeway Programming Model, corresponds to invoking the
 * <tt>hideNXxx</tt> support method for an action.
 */
public interface ActionParameterHiddenFacet
extends Facet, HidingInteractionAdvisor {

    /**
     * Whether the parameter is hidden.
     */
    public boolean isHidden(ManagedObject target, Can<ManagedObject> arguments);
}
