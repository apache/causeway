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
package org.apache.isis.applib.events.domain;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.util.ObjectContracts;

import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;


/**
 * Fired whenever the framework interacts with a domain object's action.
 *
 * <p>
 * This is the specialization of {@link AbstractDomainEvent}, for actions,
 * which should then be further subclassed by domain application.
 * </p>
 *
 * <p>
 * The class has a number of responsibilities (in addition to those it
 * inherits):
 * </p>
 *
 * <ul>
 *     <li>
 *          capture the arguments for each of the action's parameters
 *     </li>
 *     <li>
 *          provide selected metadata about the action parameters from the
 *          metamodel (names, types)
 *     </li>
 * </ul>
 *
 * <p>
 * The class itself is instantiated automatically by the framework using a
 * no-arg constructor; fields are set reflectively.
 * </p>
 *
 * @since 1.x {@index}
 */
public abstract class ActionDomainEvent<S> extends org.apache.isis.applib.services.eventbus.ActionDomainEvent<S> {

    /**
     * This class is the default for the
     * {@link Action#domainEvent()} annotation attribute.
     *
     * <p>
     * Whether this raises an event or not depends upon the
     * <tt>isis.applib.annotation.action.domain-event.post-for-default</tt>
     * configuration property.
     * </p>
     */
    public static class Default extends org.apache.isis.applib.services.eventbus.ActionDomainEvent.Default {}

    /**
     * Convenience class to use indicating that an event should <i>not</i> be
     * posted (irrespective of the configuration property setting for the
     * {@link ActionDomainEvent.Default} event.
     */
    public static class Noop extends org.apache.isis.applib.services.eventbus.ActionDomainEvent.Noop {}

    /**
     * Convenience class meaning that an event <i>should</i> be posted
     * (irrespective of the configuration property setting for the
     * {@link ActionDomainEvent.Default} event.
     */
    public static class Doop extends org.apache.isis.applib.services.eventbus.ActionDomainEvent.Doop {}

    /**
     * Subtypes can define a no-arg constructor; the framework sets state
     * via (non-API) setters.
     */
    public ActionDomainEvent() {
    }

    /**
     * Subtypes can define a one-arg constructor (for nested non-static classes of nested non-static mixins);
     * the framework sets state via (non-API) setters.
     */
    public ActionDomainEvent(final S source) {
        super(source, null);
    }


}
