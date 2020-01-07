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

package org.apache.isis.legacy.applib.services.eventbus;

import java.util.List;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;

@Deprecated
public abstract class ActionDomainEvent<S> extends AbstractDomainEvent<S> {

    // -- Default class
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Action#domainEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the <tt>isis.reflector.facet.actionAnnotation.domainEvent.postForDefault</tt>
     * configuration property.
     */
    public static class Default extends ActionDomainEvent<Object> {    }
    

    // -- Noop class

    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    public static class Noop extends ActionDomainEvent<Object> {    }
    

    // -- Doop class

    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    public static class Doop extends ActionDomainEvent<Object> {    }
    


    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Recommended because it reduces the amount of boilerplate in the domain object classes.
     * </p>
     */
    public ActionDomainEvent() {
    }

    // -- command
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
    

    // -- actionSemantics
    public SemanticsOf getSemantics() {
        return actionSemantics;
    }

    private SemanticsOf actionSemantics;

    /**
     * @deprecated - use {@link #getSemantics()} instead.
     */
    @Deprecated
    public SemanticsOf getActionSemantics() {
        return actionSemantics;
    }

    /**
     * Not API - set by the framework.
     */
    public void setActionSemantics(SemanticsOf actionSemantics) {
        this.actionSemantics = actionSemantics;
    }

    

    // -- parameterNames
    private List<String> parameterNames;
    public List<String> getParameterNames() {
        return parameterNames;
    }
    public void setParameterNames(final List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }
    

    // -- parameterTypes
    private List<Class<?>> parameterTypes;
    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(final List<Class<?>> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    



    // -- arguments
    private List<Object> arguments;
    /**
     * The arguments being used to invoke the action; populated at {@link Phase#VALIDATE} and subsequent phases
     * (but null for {@link Phase#HIDE hidden} and {@link Phase#DISABLE disable} phases).
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
    

    // -- returnValue
    /**
     *
     */
    private Object returnValue;

    /**
     * The value returned by the action.
     *
     * <p>
     *     Only available for the {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#EXECUTED}
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
    

    
    private final static ToString<ActionDomainEvent<?>> toString = ObjectContracts.<ActionDomainEvent<?>>
    		toString("source", ActionDomainEvent::getSource)
    		.thenToString("identifier", ActionDomainEvent::getIdentifier)
    		.thenToString("eventPhase", ActionDomainEvent::getEventPhase)
    		;
    
    @Override
    public String toString() {
    	return toString.toString(this);
    }


}