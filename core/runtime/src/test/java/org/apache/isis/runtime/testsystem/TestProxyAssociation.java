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

import java.util.Hashtable;

import junit.framework.Assert;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.ConsentAbstract;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolderNoop;
import org.apache.isis.metamodel.interactions.InteractionContext;
import org.apache.isis.metamodel.interactions.PropertyAccessContext;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.ValidityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;


public class TestProxyAssociation extends FacetHolderNoop implements OneToOneAssociation {

    private final String name;
    private final Hashtable<ObjectAdapter, Object> values = new Hashtable<ObjectAdapter, Object>();
    private final TestProxySpecification spec;

    private boolean isVisible = true;

    private ObjectAdapter unuseableForObject;
	private final RuntimeContext runtimeContext;

    public TestProxyAssociation(final String name, final TestProxySpecification valueFieldSpec) {
        this.name = name;
        this.spec = valueFieldSpec;
		runtimeContext = new RuntimeContextNoRuntime();
    }

    // TODO: this is inconsistent with #get that casts the value to a ObjectAdapter
    public void clearAssociation(final ObjectAdapter inObject) {
        values.put(inObject, "NULL");
    }

    public String debugData() {
        return "";
    }

    public ObjectAdapter get(final ObjectAdapter inObject) {
        return (ObjectAdapter) values.get(inObject);
    }

    public String getBusinessKeyName() {
        return null;
    }

    public String getDescription() {
        return "no description";
    }

    @Override
    public Facet getFacet(final Class cls) {
        throw new UnexpectedCallException();
    }

    public String getHelp() {
        return "no help";
    }

    public String getId() {
        return name;
    }

    public String getName() {
        return name;
    }

    public ObjectAdapter[] getChoices(final ObjectAdapter target) {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return spec;
    }

    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        setAssociation(inObject, associate);
    }

    public Consent isAssociationValid(final ObjectAdapter inObject, final ObjectAdapter value) {
        throw new UnexpectedCallException();
    }

    public boolean isNotPersisted() {
        return false;
    }

    public boolean isEmpty(final ObjectAdapter inObject) {
        throw new UnexpectedCallException();
    }

    public boolean isMandatory() {
        throw new UnexpectedCallException();
    }

    public boolean hasChoices() {
        return false;
    }

    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target) {
        return ConsentAbstract.allowIf(target != unuseableForObject);
    }

    public boolean isAlwaysHidden() {
        return false;
    }

    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return ConsentAbstract.allowIf(isVisible);
    }

    public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        values.put(inObject, associate);
    }

    public void set(ObjectAdapter owner, ObjectAdapter newValue) {
        setAssociation(owner, newValue);
    }

    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        return null;
    }

    public void toDefault(final ObjectAdapter target) {}

    @Override
    public Identifier getIdentifier() {
        return null;
    }

    public Consent isUsable(final InteractionContext ic) {
        return Allow.DEFAULT;
    }

    public boolean isVisible(final InteractionContext ic) {
        return true;
    }

    public void assertField(final Object inObject, final Object value) {
        final ObjectAdapter field = (ObjectAdapter) values.get(inObject);
        Assert.assertEquals(value, field.getObject());
    }

    public void assertFieldEmpty(final ObjectAdapter object) {
        final Object field = values.get(object);
        Assert.assertEquals("NULL", field);

    }

    public void setUpIsVisible(final boolean isVisible) {
        this.isVisible = isVisible;
    }

    public void setUpIsUnusableFor(final ObjectAdapter object) {
        this.unuseableForObject = object;
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

    public InteractionResult isAssociationValidResult(final ObjectAdapter targetObject, final ObjectAdapter newValue) {
        return null;
    }

    public InteractionResult isUsableResult(final AuthenticationSession session, final ObjectAdapter target) {
        return null;
    }

    public InteractionResult isVisibleResult(final AuthenticationSession session, final ObjectAdapter target) {
        return null;
    }

    public PropertyAccessContext createAccessInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod interactionMethod,
            final ObjectAdapter targetObjectAdapter) {
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

    public boolean isOneToManyAssociation() {
        return false;
    }
    public boolean isOneToOneAssociation() {
        return true;
    }

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    public Instance getInstance(ObjectAdapter adapter) {
        OneToOneAssociation specification = this;
        return adapter.getInstance(specification);
    }


    // /////////////////////////////////////////////////////////////
    // RuntimeContext
    // /////////////////////////////////////////////////////////////

	public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

}
