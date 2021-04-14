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

package org.apache.isis.applib.services.wrapper.listeners;

import org.apache.isis.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.isis.applib.services.wrapper.events.ActionInvocationEvent;
import org.apache.isis.applib.services.wrapper.events.ActionUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.ActionVisibilityEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionAccessEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionAddToEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionMethodEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionRemoveFromEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionVisibilityEvent;
import org.apache.isis.applib.services.wrapper.events.ObjectTitleEvent;
import org.apache.isis.applib.services.wrapper.events.ObjectValidityEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyModifyEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyVisibilityEvent;

/**
 * Provides no-op implementations of each of the methods within
 * {@link InteractionListener}, to simplify the creation of new listeners.
 *
 * @since 1.x {@index}
 */
public class InteractionAdapter implements InteractionListener {

    @Override
    public void propertyVisible(final PropertyVisibilityEvent ev) {
    }

    @Override
    public void propertyUsable(final PropertyUsabilityEvent ev) {
    }

    @Override
    public void propertyAccessed(final PropertyAccessEvent ev) {
    }

    @Override
    public void propertyModified(final PropertyModifyEvent ev) {
    }

    @Override
    public void collectionVisible(final CollectionVisibilityEvent ev) {
    }

    @Override
    public void collectionUsable(final CollectionUsabilityEvent ev) {
    }

    @Override
    public void collectionAccessed(final CollectionAccessEvent ev) {
    }

    @Override
    public void collectionAddedTo(final CollectionAddToEvent ev) {
    }

    @Override
    public void collectionRemovedFrom(final CollectionRemoveFromEvent ev) {
    }

    @Override
    public void collectionMethodInvoked(final CollectionMethodEvent interactionEvent) {
    }

    @Override
    public void actionVisible(final ActionVisibilityEvent interactionEvent) {
    }

    @Override
    public void actionUsable(final ActionUsabilityEvent ev) {
    }

    @Override
    public void actionArgument(final ActionArgumentEvent ev) {
    }

    @Override
    public void actionInvoked(final ActionInvocationEvent ev) {
    }

    @Override
    public void objectPersisted(final ObjectValidityEvent ev) {
    }

    @Override
    public void objectTitleRead(final ObjectTitleEvent ev) {
    }

}
