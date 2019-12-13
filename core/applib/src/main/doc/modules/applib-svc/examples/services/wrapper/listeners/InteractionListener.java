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

public interface InteractionListener {

    /**
     * The title was read.
     *
     * @param ev
     */
    void objectTitleRead(ObjectTitleEvent ev);

    /**
     * The object was persisted (or an attempt to persist it was made).
     *
     * @param ev
     */
    void objectPersisted(ObjectValidityEvent ev);

    /**
     * A check was made to determine if a property was visible.
     *
     * @param ev
     */
    void propertyVisible(PropertyVisibilityEvent ev);

    /**
     * A check was made to determine if a property was usable.
     *
     * @param ev
     */
    void propertyUsable(PropertyUsabilityEvent ev);

    /**
     * A property was read.
     *
     * <p>
     * Unlike most other events, a {@link PropertyAccessEvent} will never have
     * been vetoed (that is, {@link PropertyAccessEvent#isVeto()} will always be
     * <tt>false</tt>).
     *
     * @param ev
     */
    void propertyAccessed(PropertyAccessEvent ev);

    /**
     * A property was modified (or an attempt to modify it was made)
     *
     * <p>
     * Use {@link PropertyModifyEvent#getProposed()} to determine whether the
     * property was being set or cleared.
     *
     * @param ev
     */
    void propertyModified(PropertyModifyEvent ev);

    /**
     * A check was made to determine if a collection was visible.
     *
     * <p>
     * Will be fired prior to
     * {@link #collectionUsable(CollectionUsabilityEvent)}.
     *
     * @param ev
     */
    void collectionVisible(CollectionVisibilityEvent ev);

    /**
     * A check was made to determine if a collection was usable.
     *
     * <p>
     * Will be fired prior to either
     * {@link #collectionAccessed(CollectionAccessEvent)} or
     * {@link #collectionAddedTo(CollectionAddToEvent)} or
     * {@link #collectionRemovedFrom(CollectionRemoveFromEvent)}.
     *
     * @param ev
     */
    void collectionUsable(CollectionUsabilityEvent ev);

    /**
     * A collection was read.
     *
     * <p>
     * Unlike most other events, a {@link CollectionAccessEvent} will never have
     * been vetoed (that is, {@link CollectionAccessEvent#isVeto()} will always
     * be <tt>false</tt>).
     *
     * @param ev
     */
    void collectionAccessed(CollectionAccessEvent ev);

    /**
     * An object was added to the collection (or an attempt to add it was made).
     *
     * @param ev
     */
    void collectionAddedTo(CollectionAddToEvent ev);

    /**
     * An object was removed from the collection (or an attempt to remove it was
     * made).
     *
     * @param ev
     */
    void collectionRemovedFrom(CollectionRemoveFromEvent ev);

    /**
     * A method of a collection (such as <tt>isEmpty()</tt> or <tt>size()</tt>) has been invoked.
     *
     *
     * <p>
     * Unlike the other methods in this interface, the source of these events will be an instance of a
     * Collection (such as <tt>java.util.List</tt>) rather than the domain object. (The domain object is
     * {@link CollectionMethodEvent#getDomainObject() still available,  however).
     *
     * @param interactionEvent
     */
    void collectionMethodInvoked(CollectionMethodEvent interactionEvent);

    /**
     * A check was made to determine if an action was visible.
     *
     * <p>
     * Will be fired prior to {@link #actionUsable(ActionUsabilityEvent)}.
     *
     * @param ev
     */
    void actionVisible(ActionVisibilityEvent interactionEvent);

    /**
     * A check was made to determine if an action was usable.
     *
     * <p>
     * Will be fired prior to {@link #actionArgument(ActionArgumentEvent)}.
     *
     * @param ev
     */
    void actionUsable(ActionUsabilityEvent ev);

    /**
     * A check was made as to whether an argument proposed for an action was
     * valid.
     *
     * <p>
     * Will be fired prior to {@link #actionInvoked(ActionInvocationEvent)}.
     *
     * @param ev
     */
    void actionArgument(ActionArgumentEvent ev);

    /**
     * An action was invoked (or an attempt to invoke it was made).
     *
     * @param ev
     */
    void actionInvoked(ActionInvocationEvent ev);

}
