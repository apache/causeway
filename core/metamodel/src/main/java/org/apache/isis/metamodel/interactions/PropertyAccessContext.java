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

package org.apache.isis.metamodel.interactions;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.isis.metamodel.consent.InteractionContextType;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;

import static org.apache.isis.metamodel.spec.ManagedObject.unwrapPojo;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link PropertyAccessEvent}.
 */
public class PropertyAccessContext extends AccessContext<PropertyAccessEvent> {

    private final ManagedObject value;

    public PropertyAccessContext(
            final ManagedObject targetAdapter,
            final Identifier id,
            final ManagedObject value,
            final InteractionInitiatedBy interactionInitiatedBy) {
        super(InteractionContextType.PROPERTY_READ, id, targetAdapter, interactionInitiatedBy);

        this.value = value;
    }

    /**
     * The current value for a property.
     */
    public ManagedObject getValue() {
        return value;
    }

    @Override
    public PropertyAccessEvent createInteractionEvent() {
        return new PropertyAccessEvent(unwrapPojo(getTarget()), getIdentifier(), unwrapPojo(getValue()));
    }

}
