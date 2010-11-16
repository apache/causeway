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


package org.apache.isis.core.runtime.testspec;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facets.FacetHolderNoop;
import org.apache.isis.core.metamodel.interactions.PropertyAccessContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;


public abstract class ValueFieldTest extends FacetHolderNoop implements OneToOneAssociation {

    private final RuntimeContext runtimeContext;

    public ValueFieldTest() {
    	runtimeContext = new RuntimeContextFromSession();
    }
    
    public RuntimeContext getRuntimeContext() {
    	return runtimeContext;
    }

    public boolean isOneToManyAssociation() {
        return false;
    }
    public boolean isOneToOneAssociation() {
        return true;
    }

    public boolean canClear() {
        return false;
    }

    public boolean canWrap() {
        return false;
    }

    public String getDescription() {
        return "";
    }

    public String getBusinessKeyName() {
        return "";
    }

    public ObjectAdapter[] getChoices(final ObjectAdapter target) {
        return null;
    }

    public int getMaximumLength() {
        return 0;
    }

    public int getNoLines() {
        return 0;
    }

    public int getTypicalLineLength() {
        return 0;
    }

    public String getHelp() {
        return "";
    }

    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    public boolean isAlwaysHidden() {
        return false;
    }
    
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isNotPersisted() {
        return false;
    }

    public boolean isEmpty(final ObjectAdapter adapter) {
        return false;
    }

    public boolean isMandatory() {
        return false;
    }

    public boolean hasChoices() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public Consent isUsable(final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    public boolean isVisible(final ObjectAdapter target) {
        return true;
    }

    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        return null;
    }

    public void toDefault(final ObjectAdapter target) {}

    @Override
    public Identifier getIdentifier() {
        return null;
    }

    public VisibilityContext<?> createVisibleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    public UsabilityContext<?> createUsableInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter target) {
        return null;
    }

    public ValidityContext<?> createValidateInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter owningObjectAdapter,
            final ObjectAdapter newValue) {
        return null;
    }

    public PropertyAccessContext createAccessInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod interactionMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    public Instance getInstance(ObjectAdapter adapter) {
        OneToOneAssociation specification = this;
        return adapter.getInstance(specification);
    }

    public boolean isAction() {
        return false;
    }

    public boolean isAssociation() {
        return true;
    }

}
