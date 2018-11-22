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
package org.apache.isis.applib.services.eventbus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class ActionDomainEvent<S> extends AbstractInteractionEvent<S> {

    private static final long serialVersionUID = 1L;

    //region > Default class
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Action#domainEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the "isis.reflector.facet.actionAnnotation.domainEvent.postForDefault"
     * configuration property.
     */
    public static class Default extends ActionInteractionEvent<Object> {
        private static final long serialVersionUID = 1L;
        public Default(){}
        @Deprecated
        public Default(Object source, Identifier identifier, Object... arguments) {
            super(source, identifier, arguments);
        }
    }
    //endregion

    //region > Noop class

    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    public static class Noop extends ActionInteractionEvent<Object> {
        private static final long serialVersionUID = 1L;
    }
    //endregion

    //region > Doop class

    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    public static class Doop extends ActionInteractionEvent<Object> {
        private static final long serialVersionUID = 1L;
    }
    //endregion


    //region > constructors

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Recommended because it reduces the amount of boilerplate in the domain object classes.
     * </p>
     */
    public ActionDomainEvent() {
    }

    /**
     * @deprecated - the {@link #ActionDomainEvent() no-arg constructor} is recommended instead, to reduce boilerplate.
     */
    @Deprecated
    public ActionDomainEvent(
            final S source,
            final Identifier identifier) {
        super(source, identifier);
    }

    /**
     * @deprecated - the {@link #ActionDomainEvent() no-arg constructor} is recommended instead, to reduce boilerplate.
     */
    @Deprecated
    public ActionDomainEvent(
            final S source,
            final Identifier identifier,
            final Object... arguments) {
        this(source, identifier,
                asList(arguments));
    }

    private static List<Object> asList(final Object[] arguments) {
        return arguments != null
                ? Arrays.asList(arguments)
                : Collections.emptyList();
    }

    /**
     * @deprecated - the {@link #ActionDomainEvent() no-arg constructor} is recommended instead, to reduce boilerplate.
     */
    @Deprecated
    public ActionDomainEvent(
            final S source,
            final Identifier identifier,
            final List<Object> arguments) {
        this(source, identifier);
        this.arguments = Collections.unmodifiableList(arguments);
    }
    //endregion

    //region > command
    private Command command;

    /**
     * @deprecated - use {@link CommandContext#getCommand()} to obtain the current {@link Command}.
     */
    @Deprecated
    public Command getCommand() {
        return command;
    }

    /**
     * Not API - set by the framework.
     *
     * @deprecated - the corresponding {@link #getCommand()} should not be called, instead use {@link CommandContext#getCommand()} to obtain the current {@link Command}.
     */
    @Deprecated
    public void setCommand(Command command) {
        this.command = command;
    }
    //endregion

    //region > actionSemantics
    public SemanticsOf getSemantics() {
        return SemanticsOf.from(actionSemantics);
    }

    private ActionSemantics.Of actionSemantics;

    /**
     * @deprecated - use {@link #getSemantics()} instead.
     */
    @Deprecated
    public ActionSemantics.Of getActionSemantics() {
        return actionSemantics;
    }

    /**
     * Not API - set by the framework.
     */
    public void setActionSemantics(ActionSemantics.Of actionSemantics) {
        this.actionSemantics = actionSemantics;
    }

    //endregion

    //region > parameterNames
    private List<String> parameterNames;
    public List<String> getParameterNames() {
        return parameterNames;
    }
    public void setParameterNames(final List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }
    //endregion

    //region > parameterTypes
    private List<Class<?>> parameterTypes;
    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(final List<Class<?>> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    //endregion



    //region > arguments
    private List<Object> arguments;
    /**
     * The arguments being used to invoke the action; populated at {@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#VALIDATE} and subsequent phases
     * (but null for {@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#DISABLE disable} phases).
     */
    public List<Object> getArguments() {
        return arguments;
    }

    /**
     * Not API - set by the framework.
     */
    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }
    //endregion

    //region > returnValue
    /**
     *
     */
    private Object returnValue;

    /**
     * The value returned by the action.
     *
     * <p>
     *     Only available for the {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#EXECUTED}
     *     {@link #getEventPhase() phase}.
     * </p>
     */
    public Object getReturnValue() {
        return returnValue;
    }

    /**
     * Not API - set by the framework
     */
    public void setReturnValue(final Object returnValue) {
        this.returnValue = returnValue;
    }
    //endregion

    //region > toString
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,identifier,phase");
    }
    //endregion

}