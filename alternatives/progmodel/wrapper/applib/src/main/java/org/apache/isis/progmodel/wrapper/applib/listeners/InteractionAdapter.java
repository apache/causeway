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


package org.apache.isis.progmodel.wrapper.applib.listeners;

import org.apache.isis.applib.events.ActionArgumentEvent;
import org.apache.isis.applib.events.ActionInvocationEvent;
import org.apache.isis.applib.events.ActionUsabilityEvent;
import org.apache.isis.applib.events.ActionVisibilityEvent;
import org.apache.isis.applib.events.CollectionAccessEvent;
import org.apache.isis.applib.events.CollectionAddToEvent;
import org.apache.isis.applib.events.CollectionMethodEvent;
import org.apache.isis.applib.events.CollectionRemoveFromEvent;
import org.apache.isis.applib.events.CollectionUsabilityEvent;
import org.apache.isis.applib.events.CollectionVisibilityEvent;
import org.apache.isis.applib.events.ObjectTitleEvent;
import org.apache.isis.applib.events.ObjectValidityEvent;
import org.apache.isis.applib.events.PropertyAccessEvent;
import org.apache.isis.applib.events.PropertyModifyEvent;
import org.apache.isis.applib.events.PropertyUsabilityEvent;
import org.apache.isis.applib.events.PropertyVisibilityEvent;


/**
 * Provides no-op implementations of each of the methods within {@link InteractionListener}, to simplify the
 * creation of new listeners.
 */
public class InteractionAdapter implements InteractionListener {

    public void propertyVisible(final PropertyVisibilityEvent ev) {}

    public void propertyUsable(final PropertyUsabilityEvent ev) {}

    public void propertyAccessed(final PropertyAccessEvent ev) {}

    public void propertyModified(final PropertyModifyEvent ev) {}

    public void collectionVisible(final CollectionVisibilityEvent ev) {}

    public void collectionUsable(final CollectionUsabilityEvent ev) {}

    public void collectionAccessed(final CollectionAccessEvent ev) {}

    public void collectionAddedTo(final CollectionAddToEvent ev) {}

    public void collectionRemovedFrom(final CollectionRemoveFromEvent ev) {}

    public void collectionMethodInvoked(final CollectionMethodEvent interactionEvent) {}

    public void actionVisible(final ActionVisibilityEvent interactionEvent) {}

    public void actionUsable(final ActionUsabilityEvent ev) {}

    public void actionArgument(final ActionArgumentEvent ev) {}

    public void actionInvoked(final ActionInvocationEvent ev) {}

    public void objectPersisted(final ObjectValidityEvent ev) {}

    public void objectTitleRead(final ObjectTitleEvent ev) {}

}
