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
package org.apache.causeway.applib.events.metamodel;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Convenience interface to listen on {@link MetamodelEvent}s.
 *
 * <p>
 * Provided as an alternative to directly listening to these events.
 * </p>
 *
 * <p>
 * Uses a precedence {@link Order} of {@link PriorityPrecedence#MIDPOINT}.
 * For fine grained precedence control, instead listen directly to these events rather than implementing this interface.
 * </p>
 *
 * @since 2.0
 */
@FunctionalInterface
public interface MetamodelListener {

    /**
     * Emitted by the framework, once the <i>Metamodel</i> is populated.
     *
     * <p>
     * A common use-case is to seed database values,
     * right after the <i>Metamodel</i> was loaded.
     * </p>
     *
     * <p>
     *     NOTE that this callback is called without any
     *     {@link org.apache.causeway.applib.services.iactnlayer.InteractionLayer interaction} (aka persistence session)
     *     set up; use {@link org.apache.causeway.applib.services.iactnlayer.InteractionService} to create one if required.
     * </p>
     */
    void onMetamodelLoaded();

    /**
     * <p>
     *     NOTE that this callback is called without any
     *     {@link org.apache.causeway.applib.services.iactnlayer.InteractionLayer interaction} (aka persistence session)
     *     set up; use {@link org.apache.causeway.applib.services.iactnlayer.InteractionService} to create one if required.
     * </p>
     */
    default void onMetamodelAboutToBeLoaded() {}

    @EventListener(MetamodelEvent.class)
    @Order(PriorityPrecedence.MIDPOINT)
    default void onMetamodelEvent(final MetamodelEvent event) {
        switch(event) {
        case BEFORE_METAMODEL_LOADING:
            onMetamodelAboutToBeLoaded();
            return;
        case AFTER_METAMODEL_LOADED:
            onMetamodelLoaded();
            return;
        default:
            throw _Exceptions.unmatchedCase(event);
        }
    }

}
