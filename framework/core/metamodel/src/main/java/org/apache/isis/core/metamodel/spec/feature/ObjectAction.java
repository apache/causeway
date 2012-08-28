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

package org.apache.isis.core.metamodel.spec.feature;

import java.util.List;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.interactions.AccessContext;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Target;

public interface ObjectAction extends ObjectMember {

    // //////////////////////////////////////////////////////
    // Target, realTarget, getOnType
    // //////////////////////////////////////////////////////

    /**
     * Returns where the action should be executed: explicitly locally on the
     * client; explicitly remotely on the server; or where it normally should be
     * executed. By default instance methods should execute on the server,
     * static methods should execute on the client.
     */
    Target getTarget();

    /**
     * Determine the real target for this action. If this action represents an
     * object action than the target is returned. If this action is on a service
     * then that service will be returned.
     */
    ObjectAdapter realTarget(ObjectAdapter target);

    /**
     * Returns the specification for the type of object that this action can be
     * invoked upon.
     */
    ObjectSpecification getOnType();

    /**
     * Return true if the action is run on a service object using the target
     * object as a parameter.
     */
    boolean isContributed();

    boolean promptForParameters(ObjectAdapter target);

    // //////////////////////////////////////////////////////////////////
    // Type
    // //////////////////////////////////////////////////////////////////

    /**
     * Returns the {@link ActionType type} of action: user, exploration,
     * prototype or debug, or that it is a set of actions.
     */
    ActionType getType();

    // //////////////////////////////////////////////////////////////////
    // ReturnType
    // //////////////////////////////////////////////////////////////////

    /**
     * Returns the specifications for the return type.
     */
    ObjectSpecification getReturnType();

    /**
     * Returns true if the represented action returns something, else returns
     * false.
     */
    boolean hasReturn();

    // //////////////////////////////////////////////////////////////////
    // execute
    // //////////////////////////////////////////////////////////////////

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters.
     */
    ObjectAdapter execute(ObjectAdapter target, ObjectAdapter[] parameters);

    // //////////////////////////////////////////////////////////////////
    // valid
    // //////////////////////////////////////////////////////////////////

    /**
     * Creates an {@link ActionInvocationContext interaction context}
     * representing an attempt to invoke this action.
     * 
     * <p>
     * Typically it is easier to just call
     * {@link #isProposedArgumentSetValid(ObjectAdapter, ObjectAdapter[])
     * 
     * @link #isProposedArgumentSetValidResultSet(ObjectAdapter,
     *       ObjectAdapter[])}; this is provided as API for symmetry with
     *       interactions (such as {@link AccessContext} accesses) have no
     *       corresponding vetoing methods.
     */
    public ActionInvocationContext createActionInvocationInteractionContext(AuthenticationSession session, InteractionInvocationMethod invocationMethod, ObjectAdapter targetObject, ObjectAdapter[] proposedArguments);

    /**
     * Whether the provided argument set is valid, represented as a
     * {@link Consent}.
     */
    Consent isProposedArgumentSetValid(ObjectAdapter object, ObjectAdapter[] proposedArguments);

    // //////////////////////////////////////////////////////
    // Actions (for action set)
    // //////////////////////////////////////////////////////

    /**
     * Lists the sub-actions that are available under this name.
     * 
     * <p>
     * If any actions are returned then this action is only a set and not an
     * action itself.
     */
    List<ObjectAction> getActions();

    // //////////////////////////////////////////////////////
    // Parameters (declarative)
    // //////////////////////////////////////////////////////

    /**
     * Returns the number of parameters used by this method.
     */
    int getParameterCount();

    /**
     * Returns set of parameter information.
     * 
     * <p>
     * Implementations may build this array lazily or eagerly as required.
     * 
     * @return
     */
    List<ObjectActionParameter> getParameters();

    /**
     * Returns the {@link ObjectSpecification type} of each of the
     * {@link #getParameters() parameters}.
     */
    List<ObjectSpecification> getParameterTypes();

    /**
     * Returns set of parameter information matching the supplied filter.
     * 
     * @return
     */
    List<ObjectActionParameter> getParameters(Filter<ObjectActionParameter> filter);

    /**
     * Returns the parameter with provided id.
     */
    ObjectActionParameter getParameterById(String paramId);

    /**
     * Returns the parameter with provided name.
     */
    ObjectActionParameter getParameterByName(String paramName);

    // //////////////////////////////////////////////////////
    // Parameters (per instance)
    // //////////////////////////////////////////////////////

    /**
     * Returns the defaults references/values to be used for the action.
     */
    ObjectAdapter[] getDefaults(ObjectAdapter target);

    /**
     * Returns a list of possible references/values for each parameter, which
     * the user can choose from.
     */
    ObjectAdapter[][] getChoices(ObjectAdapter target);

    ActionSemantics getSemantics();

}
