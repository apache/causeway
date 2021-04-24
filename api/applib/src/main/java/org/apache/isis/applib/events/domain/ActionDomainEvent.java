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
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;

import lombok.Getter;
import lombok.Setter;

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
public abstract class ActionDomainEvent<S> extends AbstractDomainEvent<S> {

    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Action#domainEvent()} annotation attribute.
     *
     * <p>
     * Whether this raises an event or not depends upon the
     * <tt>isis.applib.annotation.action.domain-event.post-for-default</tt>
     * configuration property.
     * </p>
     */
    public static class Default extends ActionDomainEvent<Object> {}

    /**
     * Convenience class to use indicating that an event should <i>not</i> be
     * posted (irrespective of the configuration property setting for the
     * {@link Default} event.
     */
    public static class Noop extends ActionDomainEvent<Object> {}

    /**
     * Convenience class meaning that an event <i>should</i> be posted
     * (irrespective of the configuration property setting for the
     * {@link Default} event..
     */
    public static class Doop extends ActionDomainEvent<Object> {}

    /**
     * Subtypes can define a no-arg constructor; the framework sets state
     * via (non-API) setters.
     */
    public ActionDomainEvent() {
    }

    /**
     * The semantics of the action being invoked.
     *
     * <p>
     *     Copied over from {@link Action#semantics()}
     * </p>
     */
    @Getter
    private SemanticsOf semantics;

    /**
     * The names of the parameters of the actions.
     *
     * @see #getParameterTypes()
     * @see #getArguments()
     */
    @Getter
    private List<String> parameterNames;

    /**
     * The types of the parameters of the actions.
     *
     * <p>
     *     The {@link #getArguments() arguments} will be castable to the
     *     parameter types here.
     * </p>
     *
     * @see #getParameterNames()
     * @see #getArguments()
     */
    @Getter
    private List<Class<?>> parameterTypes;

    /**
     * Populated only for mixins; holds the underlying domain object that the mixin contributes to.
     */
    @Getter
    private Object mixedIn;

    /**
     * The arguments being used to invoke the action.
     *
     * <p>
     * Populated at {@link AbstractDomainEvent.Phase#VALIDATE} and subsequent
     * phases (but null for {@link AbstractDomainEvent.Phase#HIDE hidden} and
     * {@link AbstractDomainEvent.Phase#DISABLE disable} phases).
     * </p>
     *
     * <p>
     *     The argument values can also be modified by event handlers
     *     during the {@link AbstractDomainEvent.Phase#EXECUTING} phase. The
     *     new value must be the same type as the expected value; the framework
     *     performs no sanity checks.
     * </p>
     *
     * @see #getParameterNames()
     * @see #getParameterTypes()
     */
    @Getter @Setter
    private List<Object> arguments;

    /**
     * The value returned by the action.
     *
     * <p>
     * Only available for the {@link AbstractDomainEvent.Phase#EXECUTED}
     * {@link #getEventPhase() phase}.
     * </p>
     */
    @Getter
    private Object returnValue;

    /**
     * Set by the framework.
     *
     * <p>
     * Event subscribers can replace the value with some other value if they
     * wish, though only in the {@link AbstractDomainEvent.Phase#EXECUTED} phase.
     * </p>
     */
    public void setReturnValue(final Object returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * @apiNote : NOT API, set by the framework
     */
    public void setSemantics(SemanticsOf semantics) {
        this.semantics = semantics;
    }
    /**
     * @apiNote : NOT API, set by the framework
     */
    public void setParameterNames(final List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }
    /**
     * @apiNote : NOT API, set by the framework
     */
    public void setParameterTypes(final List<Class<?>> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    /**
     * @apiNote : NOT API, set by the framework
     */
    @Override
    public void setMixedIn(final Object mixedIn) {
        this.mixedIn = mixedIn;
    }


    private static final ToString<ActionDomainEvent<?>> toString = ObjectContracts.<ActionDomainEvent<?>>
    toString("source", ActionDomainEvent::getSource)
    .thenToString("identifier", ActionDomainEvent::getIdentifier)
    .thenToString("eventPhase", ActionDomainEvent::getEventPhase)
    ;

    @Override
    public String toString() {
        return toString.toString(this);
    }


}
