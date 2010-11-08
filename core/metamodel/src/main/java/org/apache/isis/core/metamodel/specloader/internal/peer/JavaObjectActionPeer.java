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


package org.apache.isis.core.metamodel.specloader.internal.peer;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.core.metamodel.facets.actions.executed.ExecutedFacet;
import org.apache.isis.core.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Target;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.specloader.ReflectiveActionException;


/*
 * TODO (in all Java...Peer classes) make all methodsArray throw ReflectiveActionException when 
 * an exception occurs when calling a method reflectively (see execute method).  Then instead of 
 * calling invocationExcpetion() the exception will be passed though, and dealt with generally by 
 * the reflection package (which will be the same for all reflectors and will allow the message to
 * be better passed back to the client).
 */
public class JavaObjectActionPeer extends JavaObjectMemberPeer implements ObjectActionPeer {

    private final JavaObjectActionParamPeer[] parameters;

    public JavaObjectActionPeer(final Identifier identifier, final JavaObjectActionParamPeer[] parameters) {
        super(identifier);
        this.parameters = parameters;
    }

    // ////////////////////// Type etc ////////////////////

    public Target getTarget() {
        final ExecutedFacet executedFacet = getFacet(ExecutedFacet.class);
        return executedFacet == null ? Target.DEFAULT : executedFacet.getTarget();
    }

    public ObjectActionType getType() {
        return ObjectActionType.getType(this);
    }

    // ////////////////////// execute ////////////////////

    public ObjectAdapter execute(final ObjectAdapter inObject, final ObjectAdapter[] parameters) throws ReflectiveActionException {
        final ActionInvocationFacet facet = getFacet(ActionInvocationFacet.class);
        return facet.invoke(inObject, parameters);
    }

    // ////////////////////// Parameters ////////////////////

    public int getParameterCount() {
        return parameters.length;
    }

    public ObjectActionParamPeer[] getParameters() {
        return parameters;
    }

    // ///////////////////////// toString, debugData /////////////////////////

    @Override
    public String toString() {
        final StringBuffer parameters = new StringBuffer();
        final ActionInvocationFacet facet = getFacet(ActionInvocationFacet.class);
        final ObjectSpecification onType = facet.getOnType();
        return "JavaAction [name=" + getIdentifier().getMemberName() + ",type=" + onType.getShortName() + ",parameters="
                + parameters + "]";
    }

}
