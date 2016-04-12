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
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
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

    ObjectAdapter invoke(
            ObjectAction owningAction,
            ObjectAdapter targetAdapter,
            ObjectAdapter[] argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy);

    ObjectSpecification getReturnType();

    ObjectSpecification getOnType();

    class CurrentInvocation {

        private final Command command;
        
        private final ObjectAdapter target;
        private final ObjectAction action;
        private final IdentifiedHolder identifiedHolder;
        private final List<ObjectAdapter> parameters;
        private final ObjectAdapter result;

        public CurrentInvocation(
                final ObjectAdapter target,
                final ObjectAction objectAction,
                final IdentifiedHolder identifiedHolder,
                final ObjectAdapter[] parameters,
                final ObjectAdapter result, 
                final Command command) {
            this(target, objectAction, identifiedHolder, Arrays.asList(parameters), result, command);
        }

        public CurrentInvocation(
                final ObjectAdapter target,
                final ObjectAction objectAction,
                final IdentifiedHolder identifiedHolder,
                final List<ObjectAdapter> parameters,
                final ObjectAdapter result, 
                final Command command) {
            this.target = target;
            this.action = objectAction;
            this.identifiedHolder = identifiedHolder;
            this.parameters = parameters;
            this.result = result;
            this.command = command;
        }

        /**
         * Undeprecated ... isn't necessarily the same as the info held in Command because
         * Command only ever wraps the outer-most action, whereas this could be for an action invoked
         * via the WrapperFactory.
         */
        public ObjectAdapter getTarget() {
            return target;
        }

        public ObjectAction getAction() {
            return action;
        }

        /**
         * Undeprecated ... isn't necessarily the same as the info held in Command because
         * Command only ever wraps the outer-most action, whereas this could be for an action invoked
         * via the WrapperFactory.
         *
         * This is the FacetedMethod for the {@link #getAction()}.
         */
        public IdentifiedHolder getIdentifiedHolder() {
            return identifiedHolder;
        }

        public List<ObjectAdapter> getParameters() {
            return parameters;
        }

        /**
         * Undeprecated ... isn't necessarily the same as the info held in Command because
         * Command only ever wraps the outer-most action, whereas this could be for an action invoked
         * via the WrapperFactory.
         */
        public ObjectAdapter getResult() {
            return result;
        }
        
        public Command getCommand() {
            return command;
        }
    }
    
    /**
     * This thread-local is populated by ActionInvocationFacet, but is set to null when the action is
     * published.  Note that if the WrapperFactory is in use then this can possibly many time
     * within a calling action.  Therefore the contents of this thread-local are not necessarily
     * the same as the info held on the CommandContext (which always holds the outer-most action
     * details).
     */
    @Deprecated ThreadLocal<CurrentInvocation> currentInvocation = new ThreadLocal<>();


}
