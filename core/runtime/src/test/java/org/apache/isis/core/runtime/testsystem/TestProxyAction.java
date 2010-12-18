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


package org.apache.isis.core.runtime.testsystem;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facets.FacetHolderNoop;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.FeatureType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Target;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;


public class TestProxyAction extends FacetHolderNoop implements ObjectAction {

    private final String id;
	private final RuntimeContext runtimeContext;

    public TestProxyAction(final String id) {
        this.id = id;
		runtimeContext = new RuntimeContextNoRuntime();
    }

    @Override
    public ObjectAdapter execute(final ObjectAdapter target, final ObjectAdapter[] parameters) {
        return null;
    }

    @Override
    public ObjectAction[] getActions() {
        return null;
    }

    @Override
    public ObjectAdapter[] getDefaults(final ObjectAdapter target) {
        return null;
    }

    @Override
    public ObjectSpecification getOnType() {
        return null;
    }

    @Override
    public ObjectAdapter[][] getChoices(final ObjectAdapter target) {
        return null;
    }

    @Override
    public int getParameterCount() {
        return 0;
    }

    @Override
    public ObjectActionParameter[] getParameters() {
        return null;
    }

    @Override
    public ObjectSpecification[] getParameterTypes() {
        return null;
    }

    @Override
    public ObjectActionParameter[] getParameters(final Filter<ObjectActionParameter> filter) {
        return null;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return null;
    }

    @Override
    public Target getTarget() {
        return null;
    }

    @Override
    public ObjectActionType getType() {
        return null;
    }

    @Override
    public boolean hasReturn() {
        return false;
    }

    @Override
    public boolean isContributed() {
        return false;
    }

    @Override
    public boolean promptForParameters(final ObjectAdapter target) {
        return false;
    }

    public boolean isOnInstance() {
        return false;
    }

    @Override
    public Consent isProposedArgumentSetValid(final ObjectAdapter object, final ObjectAdapter[] parameters) {
        return null;
    }

    @Override
    public ObjectAdapter realTarget(final ObjectAdapter target) {
        return null;
    }

    @Override
    public String debugData() {
        return null;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    @Override
    public boolean isAlwaysHidden() {
        return false;
    }
    
    @Override
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.classIdentifier("");
    }

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter target) {
        return null;
    }

    @Override
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

    @Override
    public boolean isAction() {
        return true;
    }

    @Override
    public boolean isPropertyOrCollection() {
        return false;
    }
    @Override
    public boolean isOneToManyAssociation() {
        return false;
    }
    @Override
    public boolean isOneToOneAssociation() {
        return false;
    }

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////
    
    @Override
    public Instance getInstance(ObjectAdapter adapter) {
        return null;
    }

    
    // /////////////////////////////////////////////////////////////
    // RuntimeContext
    // /////////////////////////////////////////////////////////////

	public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

    /* (non-Javadoc)
     * @see org.apache.isis.core.metamodel.spec.feature.ObjectFeature#getFeatureType()
     */
    @Override
    public FeatureType getFeatureType() {
        return FeatureType.ACTION;
    }

}

