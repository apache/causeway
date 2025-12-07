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
package org.apache.causeway.core.metamodel.interactions.vis;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.wrapper.events.VisibilityEvent;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionEventSupplier;
import org.apache.causeway.core.metamodel.interactions.RenderPolicy;
import org.apache.causeway.core.metamodel.interactions.VisibilityConstraint;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link VisibilityEvent}.
 */
public sealed interface VisibilityContext
extends InteractionContext, InteractionEventSupplier<VisibilityEvent>
permits ParamVisibilityContext, ActionVisibilityContext, CollectionVisibilityContext,
    ObjectVisibilityContext, PropertyVisibilityContext {

	/**
	 * for debugging visibility when prototyping
	 */
    RenderPolicy renderPolicy();

    /**
     * Where and by what viewer the element is to be rendered.
     */
    VisibilityConstraint visibilityConstraint();

    /**
     * Where the element is to be rendered.
     */
    default Where where() {
    	return visibilityConstraint().where();
    }

}
