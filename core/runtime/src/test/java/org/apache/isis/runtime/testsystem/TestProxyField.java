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


package org.apache.isis.runtime.testsystem;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facets.FacetHolderImpl;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;


public class TestProxyField extends FacetHolderImpl implements ObjectAssociation {

    private final String name;
    private final ObjectSpecification spec;
    private Identifier identifier;
	private final RuntimeContext runtimeContext;

    public TestProxyField(final String name, final ObjectSpecification spec) {
        this.name = name;
        this.spec = spec;
        identifier = Identifier.propertyOrCollectionIdentifier(spec.getFullName(), name);
		runtimeContext = new RuntimeContextNoRuntime();
    }

    public String debugData() {
        return "";
    }

    public ObjectAdapter get(final ObjectAdapter fromObject) {
        return ((TestProxyAdapter) fromObject).getField(this); // contentObject;
    }

    public String getBusinessKeyName() {
        return null;
    }

    public boolean isOneToManyAssociation() {
        return getSpecification().isCollection();
    }

    public boolean isOneToOneAssociation() {
        return !isOneToManyAssociation();
    }

    public boolean isNotPersisted() {
        return false;
    }

    public boolean isEmpty(final ObjectAdapter adapter) {
        return false;
    }

    public boolean isObject() {
        return getSpecification().isNotCollection();
    }

    public boolean hasChoices() {
        return false;
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

    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        return null;
    }

    public void toDefault(final ObjectAdapter target) {}

    public Identifier getIdentifier() {
        return identifier;
    }

    public ObjectAdapter[] getChoices(final ObjectAdapter object) {
        return new ObjectAdapter[] {};
    }

    public ObjectSpecification getSpecification() {
        return spec;
    }

    public boolean isMandatory() {
        return false;
    }

    public String getHelp() {
        return null;
    }

    public String getId() {
        return name;
    }

    public String getDescription() {
        return null;
    }

    public String getName() {
        return name;
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
            final ObjectAdapter parent,
            final ObjectAdapter newValue) {
        return null;
    }

    public InteractionResultSet isAssociationValidResultSet(final ObjectAdapter targetObject, final ObjectAdapter newValue) {
        return new InteractionResultSet();
    }

    public InteractionResult isUsableResult(final AuthenticationSession session, final ObjectAdapter target) {
        return null;
    }

    public InteractionResult isVisibleResult(final AuthenticationSession session, final ObjectAdapter target) {
        return null;
    }


    // /////////////////////////////////////////////////////////////
    // isAction, isAssociation
    // /////////////////////////////////////////////////////////////

    public boolean isAction() {
        return false;
    }

    public boolean isAssociation() {
        return true;
    }

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////
    
    public Instance getInstance(ObjectAdapter adapter) {
        return null;
    }

    
    // /////////////////////////////////////////////////////////////
    // RuntimeContext
    // /////////////////////////////////////////////////////////////

	public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

}

