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


package org.apache.isis.core.metamodel.adapter;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.MultiTypedFacet;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Target;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;


/**
 * Has no functionality but makes it easier to write tests that require an instance of an {@link Identifier}.
 * 
 * <p>
 * Was previously DummyAction, in the web viewer project. Only used by tests there.
 */
public class ObjectActionNoop implements ObjectAction {

    public boolean[] canParametersWrap() {
        return null;
    }

    public String debugData() {
        return null;
    }

    public ObjectAdapter execute(final ObjectAdapter target, final ObjectAdapter[] parameters) {
        return null;
    }

    public ObjectAction[] getActions() {
        return null;
    }

    public ObjectAdapter[] getDefaults(final ObjectAdapter target) {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return false;
    }

    public <T extends Facet> T getFacet(final Class<T> cls) {
        return null;
    }

    public Class<? extends Facet>[] getFacetTypes() {
        return new Class[0];
    }

    public Facet[] getFacets(final Filter<Facet> filter) {
        return null;
    }

    public void addFacet(final Facet facet) {}

    public void addFacet(final MultiTypedFacet facet) {}

    public void removeFacet(final Facet facet) {}

    public void removeFacet(final Class<? extends Facet> facetType) {}

    public Identifier getIdentifier() {
        return null;
    }

    public String getHelp() {
        return null;
    }

    public String getId() {
        return null;
    }

    public String getName() {
        return null;
    }

    public ObjectSpecification getOnType() {
        return null;
    }

    public ObjectAdapter[][] getChoices(final ObjectAdapter target) {
        return null;
    }

    public int getParameterCount() {
        return 0;
    }

    public ObjectActionParameter[] getParameters() {
        return null;
    }

    public ObjectActionParameter[] getParameters(final Filter<ObjectActionParameter> filter) {
        return null;
    }

    public ObjectSpecification getReturnType() {
        return null;
    }

    public Target getTarget() {
        return null;
    }

    public ObjectActionType getType() {
        return null;
    }

    public boolean hasReturn() {
        return false;
    }

    public boolean isContributed() {
        return false;
    }

    public boolean promptForParameters(final ObjectAdapter target) {
        return false;
    }

    public VisibilityContext<?> createVisibleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    public boolean isAlwaysHidden() {
        return false;
    }
    
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    public Consent isProposedArgumentSetValid(final ObjectAdapter object, final ObjectAdapter[] parameters) {
        return Allow.DEFAULT;
    }

    public ObjectAdapter realTarget(final ObjectAdapter target) {
        return target;
    }

    public ObjectSpecification getSpecification() {
        return null;
    }

    public UsabilityContext<?> createUsableInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter target) {
        return null;
    }

    public ActionInvocationContext createActionInvocationInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter object,
            final ObjectAdapter[] candidateArguments) {
        return null;
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
    // getInstance
    // /////////////////////////////////////////////////////////////
    
    public Instance getInstance(ObjectAdapter adapter) {
        ObjectAction specification = this;
        return adapter.getInstance(specification);
    }

    public ObjectSpecification[] getParameterTypes() {
        return null;
    }

	public RuntimeContext getRuntimeContext() {
		// TODO Auto-generated method stub
		return null;
	}



}

