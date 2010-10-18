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


package org.apache.isis.metamodel.runtimecontext.spec.feature;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.exceptions.UnexpectedCallException;
import org.apache.isis.commons.filters.Filter;
import org.apache.isis.metamodel.adapter.Instance;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.Allow;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.MultiTypedFacet;
import org.apache.isis.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Target;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;


public class ObjectActionSet implements ObjectAction {

    private final String name;
    private final String id;
    private final ObjectAction[] actions;
	private final RuntimeContext runtimeContext;
	
    public ObjectActionSet(
    		final String id, 
    		final String name, 
    		final ObjectAction[] actions, 
    		final RuntimeContext runtimeContext) {
        this.id = id;
        this.name = name;
        this.actions = actions;
        this.runtimeContext = runtimeContext;
    }

    public ObjectActionSet(
    		final String id, 
    		final String name, 
    		final List<ObjectAction> actions, 
    		final RuntimeContext runtimeContext) {
        this(id, name, actions.toArray(new ObjectAction[]{}), runtimeContext);
    }

    // /////////////////////////////////////////////////////////////
    // description, actions
    // /////////////////////////////////////////////////////////////

    public ObjectAction[] getActions() {
        return actions;
    }

    public String getDescription() {
        return "";
    }

    public Identifier getIdentifier() {
        return null;
    }

    public String getHelp() {
        return "";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ObjectSpecification getOnType() {
        return null;
    }

    public ObjectSpecification getReturnType() {
        return null;
    }

    public Target getTarget() {
        return Target.DEFAULT;
    }

    public ObjectActionType getType() {
        return ObjectActionType.SET;
    }

    public boolean hasReturn() {
        return false;
    }

    public boolean isContributed() {
        return false;
    }

    /**
     * Always returns <tt>null</tt>.
     */
    public ObjectSpecification getSpecification() {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // target
    // /////////////////////////////////////////////////////////////

    public ObjectAdapter realTarget(final ObjectAdapter target) {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // execute
    // /////////////////////////////////////////////////////////////

    public ObjectAdapter execute(final ObjectAdapter target, final ObjectAdapter[] parameters) {
        throw new UnexpectedCallException();
    }

    // /////////////////////////////////////////////////////////////
    // facets
    // /////////////////////////////////////////////////////////////

    /**
     * Does nothing
     */
    public <T extends Facet> T getFacet(final Class<T> cls) {
        return null;
    }

    /**
     * Does nothing
     */
    public Class<? extends Facet>[] getFacetTypes() {
        return new Class[0];
    }

    /**
     * Does nothing
     */
    public Facet[] getFacets(final Filter<Facet> filter) {
        return new Facet[0];
    }

    /**
     * Does nothing
     */
    public void addFacet(final Facet facet) {}

    /**
     * Does nothing
     */
    public void addFacet(final MultiTypedFacet facet) {}

    /**
     * Does nothing
     */
    public void removeFacet(final Facet facet) {}

    /**
     * Does nothing
     */
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return false;
    }

    /**
     * Does nothing
     */
    public void removeFacet(final Class<? extends Facet> facetType) {}

    // /////////////////////////////////////////////////////////////
    // parameters
    // /////////////////////////////////////////////////////////////

    public int getParameterCount() {
        return 0;
    }

    public ObjectActionParameter[] getParameters() {
        return new ObjectActionParameter[0];
    }

    public ObjectSpecification[] getParameterTypes() {
        return new ObjectSpecification[0];
    }


    public ObjectActionParameter[] getParameters(final Filter<ObjectActionParameter> filter) {
        return new ObjectActionParameter[0];
    }

    public boolean promptForParameters(final ObjectAdapter target) {
        return false;
    }

    // /////////////////////////////////////////////////////////////
    // visibility
    // /////////////////////////////////////////////////////////////

    public boolean isAlwaysHidden() {
        return false;
    }

    /**
     * Does nothing, but shouldn't be called.
     */
    public VisibilityContext<?> createVisibleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    // /////////////////////////////////////////////////////////////
    // usability
    // /////////////////////////////////////////////////////////////

    public UsabilityContext<?> createUsableInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter target) {
        return null;
    }

    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    // /////////////////////////////////////////////////////////////
    // validity
    // /////////////////////////////////////////////////////////////

    public ActionInvocationContext createActionInvocationInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter object,
            final ObjectAdapter[] candidateArguments) {
        return null;
    }

    public Consent isProposedArgumentSetValid(final ObjectAdapter object, final ObjectAdapter[] parameters) {
        throw new UnexpectedCallException();
    }

    // /////////////////////////////////////////////////////////////
    // defaults
    // /////////////////////////////////////////////////////////////

    public ObjectAdapter[] getDefaults(final ObjectAdapter target) {
        throw new UnexpectedCallException();
    }

    // /////////////////////////////////////////////////////////////
    // options
    // /////////////////////////////////////////////////////////////

    public ObjectAdapter[][] getChoices(final ObjectAdapter target) {
        throw new UnexpectedCallException();
    }

    
    // /////////////////////////////////////////////////////////////
    // isAction, isAssociation
    // /////////////////////////////////////////////////////////////

    public boolean isAction() {
        return true;
    }

    public boolean isAssociation() {
        return false;
    }
    public boolean isOneToManyAssociation() {
        return false;
    }
    public boolean isOneToOneAssociation() {
        return false;
    }


    // /////////////////////////////////////////////////////////////
    // debug
    // /////////////////////////////////////////////////////////////

    public String debugData() {
        return "";
    }

    
    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////
    
    public Instance getInstance(ObjectAdapter adapter) {
        ObjectAction specification = this;
        return adapter.getInstance(specification);
    }

    
    // /////////////////////////////////////////////////////////////
    // RuntimeContext
    // /////////////////////////////////////////////////////////////

	public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

}
