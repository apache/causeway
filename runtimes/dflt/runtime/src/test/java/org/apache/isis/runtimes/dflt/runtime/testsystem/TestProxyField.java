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

package org.apache.isis.runtimes.dflt.runtime.testsystem;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public class TestProxyField extends FacetHolderImpl implements ObjectAssociation {

    private final String name;
    private final ObjectSpecification spec;
    private final Identifier identifier;
    private final RuntimeContext runtimeContext;

    public TestProxyField(final String name, final ObjectSpecification spec) {
        this.name = name;
        this.spec = spec;
        identifier = Identifier.propertyOrCollectionIdentifier(spec.getFullIdentifier(), name);
        runtimeContext = new RuntimeContextNoRuntime();
    }

    @Override
    public String debugData() {
        return "";
    }

    @Override
    public ObjectAdapter get(final ObjectAdapter fromObject) {
        return ((TestProxyAdapter) fromObject).getField(this); // contentObject;
    }

    @Override
    public String getBusinessKeyName() {
        return null;
    }

    @Override
    public boolean isOneToManyAssociation() {
        return getSpecification().isCollection();
    }

    @Override
    public boolean isOneToOneAssociation() {
        return !isOneToManyAssociation();
    }

    @Override
    public boolean isNotPersisted() {
        return false;
    }

    @Override
    public boolean isEmpty(final ObjectAdapter adapter) {
        return false;
    }

    public boolean isObject() {
        return getSpecification().isNotCollection();
    }

    @Override
    public boolean hasChoices() {
        return false;
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
    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        return null;
    }

    @Override
    public void toDefault(final ObjectAdapter target) {
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public ObjectAdapter[] getChoices(final ObjectAdapter object) {
        return new ObjectAdapter[] {};
    }

    @Override
    public ObjectSpecification getSpecification() {
        return spec;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getName() {
        return name;
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

    public ValidityContext<?> createValidateInteractionContext(final AuthenticationSession session,
        final ObjectAdapter parent, final ObjectAdapter newValue) {
        return null;
    }

    public InteractionResultSet isAssociationValidResultSet(final ObjectAdapter targetObject,
        final ObjectAdapter newValue) {
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

    @Override
    public boolean isAction() {
        return false;
    }

    @Override
    public boolean isPropertyOrCollection() {
        return true;
    }

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.PROPERTY;
    }

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    @Override
    public Instance getInstance(final ObjectAdapter adapter) {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // RuntimeContext
    // /////////////////////////////////////////////////////////////

    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

}
