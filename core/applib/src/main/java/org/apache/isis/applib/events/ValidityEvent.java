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

package org.apache.isis.applib.events;

import org.apache.isis.applib.Identifier;

/**
 * <i>Supported only by {@link org.apache.isis.applib.services.wrapper.WrapperFactory} service, </i> represents a check to determine whether a proposed change is valid.
 * 
 * <p>
 * Multiple subclasses, including:
 * <ul>
 * <li>modifying a property</li>
 * <li>adding to/removing from a collection</li>
 * <li>checking a single argument for an action invocation</li>
 * <li>checking all arguments for an action invocation</li>
 * <li>checking all properties for an object before saving</li>
 * </ul>
 * 
 * <p>
 * If {@link #getReason()} is <tt>null</tt>, then is usable; otherwise is
 * disabled.
 * 
 * @see AccessEvent
 * @see VisibilityEvent
 * @see UsabilityEvent
 *
 * @deprecated - superceded by <code>domainEvent</code> support ({@link org.apache.isis.applib.services.eventbus.PropertyDomainEvent}, {@link org.apache.isis.applib.IsisApplibModule.CollectionDomainEvent}, {@link org.apache.isis.applib.services.eventbus.ActionDomainEvent}).
 */
@Deprecated
public abstract class ValidityEvent extends InteractionEvent implements ProposedHolderEvent {

    private static final long serialVersionUID = 1L;

    public ValidityEvent(final Object source, final Identifier identifier) {
        super(source, identifier);
    }

    @Override
    public Object getSource() {
        return super.getSource();
    }
    
    @Override
    public String getReasonMessage() {
    	return String.format("Source: %s. %s", this.getSource(), super.getReasonMessage());
    }
}
