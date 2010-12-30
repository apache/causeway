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

import java.util.Hashtable;

import junit.framework.Assert;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent2.Allow;
import org.apache.isis.core.metamodel.consent2.Consent;
import org.apache.isis.core.metamodel.consent2.ConsentAbstract;
import org.apache.isis.core.metamodel.consent2.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent2.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetHolderNoop;
import org.apache.isis.core.metamodel.interactions2.InteractionContext;
import org.apache.isis.core.metamodel.interactions2.PropertyAccessContext;
import org.apache.isis.core.metamodel.interactions2.UsabilityContext;
import org.apache.isis.core.metamodel.interactions2.ValidityContext;
import org.apache.isis.core.metamodel.interactions2.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;


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
    @Override
    public void clearAssociation(final ObjectAdapter inObject) {
        values.put(inObject, "NULL");
    }

    @Override
    public String debugData() {
        return "";
    }

    @Override
    public ObjectAdapter get(final ObjectAdapter inObject) {
        return (ObjectAdapter) values.get(inObject);
    }

    @Override
    public String getBusinessKeyName() {
        return null;
    }

    @Override
    public String getDescription() {
        return "no description";
    }

    @Override
    public Facet getFacet(final Class cls) {
        throw new UnexpectedCallException();
    }

    @Override
    public String getHelp() {
        return "no help";
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ObjectAdapter[] getChoices(final ObjectAdapter target) {
        return null;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return spec;
    }

    @Override
    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        setAssociation(inObject, associate);
    }

    @Override
    public Consent isAssociationValid(final ObjectAdapter inObject, final ObjectAdapter value) {
        throw new UnexpectedCallException();
    }

    @Override
    public boolean isNotPersisted() {
        return false;
    }

    @Override
    public boolean isEmpty(final ObjectAdapter inObject) {
        throw new UnexpectedCallException();
    }

    @Override
    public boolean isMandatory() {
        throw new UnexpectedCallException();
    }

    @Override
    public boolean hasChoices() {
        return false;
    }

    @Override
    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target) {
        return ConsentAbstract.allowIf(target != unuseableForObject);
    }

    @Override
    public boolean isAlwaysHidden() {
        return false;
    }

    @Override
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return ConsentAbstract.allowIf(isVisible);
    }

    @Override
    public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        values.put(inObject, associate);
    }

    @Override
    public void set(ObjectAdapter owner, ObjectAdapter newValue) {
        setAssociation(owner, newValue);
    }

    @Override
    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        return null;
    }

    @Override
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

    @Override
    public PropertyAccessContext createAccessInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod interactionMethod,
            final ObjectAdapter targetObjectAdapter) {
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
    public boolean isOneToManyAssociation() {
        return false;
    }
    @Override
    public boolean isOneToOneAssociation() {
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
