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

package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

/**
 * Represents the mechanism by which the action should be invoked.
 * 
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to invoking the
 * actual action method itself (a <tt>public</tt> method that does not represent
 * a property, a collection or any of the supporting methods).
 */
public interface ActionInvocationFacet extends Facet {

    @Deprecated
    public ObjectAdapter invoke(ObjectAdapter targetAdapter, ObjectAdapter[] argumentAdapters);

    public ObjectAdapter invoke(ObjectAction owningAction, ObjectAdapter targetAdapter, ObjectAdapter[] argumentAdapters);

    public ObjectSpecification getReturnType();

    public ObjectSpecification getOnType();

    public static class CurrentInvocation {

        private final Command command;
        
        private final ObjectAdapter target;
        private final IdentifiedHolder action;
        private final List<ObjectAdapter> parameters;
        private final ObjectAdapter result;

        public CurrentInvocation(
                final ObjectAdapter target,
                final IdentifiedHolder action,
                final ObjectAdapter[] parameters,
                final ObjectAdapter result, 
                final Command command) {
            this(target, action, Arrays.asList(parameters), result, command);
        }

        public CurrentInvocation(
                final ObjectAdapter target,
                final IdentifiedHolder action,
                final List<ObjectAdapter> parameters,
                final ObjectAdapter result, 
                final Command command) {
            this.target = target;
            this.action = action;
            this.parameters = parameters;
            this.result = result;
            this.command = command;
        }

        /**
         * deprecated since part of {@link #getCommand()}
         */
        @Deprecated
        public ObjectAdapter getTarget() {
            return target;
        }
        /**
         * deprecated since part of {@link #getCommand()}
         */
        @Deprecated
        public IdentifiedHolder getAction() {
            return action;
        }

        public List<ObjectAdapter> getParameters() {
            return parameters;
        }
        
        /**
         * deprecated since part of {@link #getCommand()}
         */
        @Deprecated
        public ObjectAdapter getResult() {
            return result;
        }
        
        public Command getCommand() {
            return command;
        }
    }
    
    /**
     * TODO...
     * @deprecated - should instead use the {@link CommandContext} request.
     */
    @Deprecated
    public static ThreadLocal<CurrentInvocation> currentInvocation = new ThreadLocal<CurrentInvocation>();


}
