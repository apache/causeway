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

package org.apache.isis.runtimes.dflt.runtime.testspec;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.interactions.PropertyAccessContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.internal.RuntimeContextFromSession;

public abstract class ValueFieldTest extends FacetHolderNoop implements OneToOneAssociation {

    private final RuntimeContext runtimeContext;

    public ValueFieldTest() {
        runtimeContext = new RuntimeContextFromSession();
    }

    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    @Override
    public boolean isOneToManyAssociation() {
        return false;
    }

    @Override
    public boolean isOneToOneAssociation() {
        return true;
    }

    public boolean canClear() {
        return false;
    }

    public boolean canWrap() {
        return false;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getBusinessKeyName() {
        return "";
    }

    @Override
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

    @Override
    public String getHelp() {
        return "";
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

    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isNotPersisted() {
        return false;
    }

    @Override
    public boolean isEmpty(final ObjectAdapter adapter) {
        return false;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
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

    @Override
    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        return null;
    }

    @Override
    public void toDefault(final ObjectAdapter target) {
    }

    @Override
    public Identifier getIdentifier() {
        return null;
    }

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(final AuthenticationSession session,
        final InteractionInvocationMethod invocationMethod, final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    @Override
    public UsabilityContext<?> createUsableInteractionContext(final AuthenticationSession session,
        final InteractionInvocationMethod invocationMethod, final ObjectAdapter target) {
        return null;
    }

    @Override
    public ValidityContext<?> createValidateInteractionContext(final AuthenticationSession session,
        final InteractionInvocationMethod invocationMethod, final ObjectAdapter owningObjectAdapter,
        final ObjectAdapter newValue) {
        return null;
    }

    @Override
    public PropertyAccessContext createAccessInteractionContext(final AuthenticationSession session,
        final InteractionInvocationMethod interactionMethod, final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    @Override
    public Instance getInstance(final ObjectAdapter adapter) {
        final OneToOneAssociation specification = this;
        return adapter.getInstance(specification);
    }

    @Override
    public boolean isAction() {
        return false;
    }

    @Override
    public boolean isPropertyOrCollection() {
        return true;
    }

}
