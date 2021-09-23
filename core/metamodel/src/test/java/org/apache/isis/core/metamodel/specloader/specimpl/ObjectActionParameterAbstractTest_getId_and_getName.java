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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.function.Predicate;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class ObjectActionParameterAbstractTest_getId_and_getName {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock private ObjectActionDefault parentAction;
    @Mock private TypedHolder actionParamPeer;
    @Mock private ParamNamedFacet namedFacet;
    @Mock private FacetedMethod mockFacetedMethod;

    @Mock private ObjectSpecification stubSpecForString;
    @Mock private ObjectActionParameter stubObjectActionParameterString;
    @Mock private ObjectActionParameter stubObjectActionParameterString2;

    private static final class ObjectActionParameterAbstractToTest
    extends ObjectActionParameterAbstract {
        private ObjectActionParameterAbstractToTest(
                final int number, final ObjectActionDefault objectAction, final TypedHolder peer) {
            super(FeatureType.ACTION_PARAMETER_SCALAR, number, objectAction, peer);
        }

        private ObjectSpecification elementSpec;

        @Override
        public ManagedObject get(final ManagedObject owner, final InteractionInitiatedBy interactionInitiatedBy) {
            return null;
        }

        @Override
        public FeatureType getFeatureType() {
            return null;
        }

        @Override
        public String isValid(
                final InteractionHead head,
                final ManagedObject proposedValue,
                final InteractionInitiatedBy interactionInitiatedBy) {
            return null;
        }

        @Override
        public Consent isValid(final InteractionHead head, final Can<ManagedObject> pendingArgs,
                final InteractionInitiatedBy interactionInitiatedBy) {
            return null;
        }

        @Override
        public ObjectSpecification getElementType() {
            return elementSpec;
        }

        public void setSpecification(final ObjectSpecification elementSpec) {
            this.elementSpec = elementSpec;
        }

    }

    private ObjectActionParameterAbstractToTest objectActionParameter;

    @SuppressWarnings("unused")
    private static class Customer {
        public void aMethod(final Object someParameterName, final Object arg1, final Object arg2) {}
    }

    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(stubSpecForString).getSingularName();
                will(returnValue("string"));

                allowing(stubObjectActionParameterString).getElementType();
                will(returnValue(stubSpecForString));

                allowing(stubObjectActionParameterString2).getElementType();
                will(returnValue(stubSpecForString));

                allowing(parentAction).getFacetedMethod();
                will(returnValue(mockFacetedMethod));

                allowing(mockFacetedMethod).getMethod();
                will(returnValue(Customer.class.getMethod("aMethod", new Class[] {Object.class, Object.class, Object.class})));
            }
        });

    }

    @Test
    public void getId_whenNamedFacetPresent() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);

        assertThat(objectActionParameter.getId(), is("someParameterName"));
    }

    @Test
    public void getName_whenNamedFacetPresent() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);

        context.checking(new Expectations() {
            {
                oneOf(actionParamPeer).getFacet(ParamNamedFacet.class);
                will(returnValue(namedFacet));

                atLeast(1).of(namedFacet).translated();
                will(returnValue("Some parameter name"));
            }
        });

        assertThat(objectActionParameter.getStaticFriendlyName().get(), is("Some parameter name"));
    }

    @Test @Ignore("ParamNamedFacet is always present - ensured by facet post processing")
    public void whenNamedFaceNotPresentAndOnlyOneParamOfType() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);
        objectActionParameter.setSpecification(stubSpecForString);

        context.checking(new Expectations() {
            {
                oneOf(actionParamPeer).getFacet(ParamNamedFacet.class);
                will(returnValue(null));

                oneOf(parentAction).getParameters(with(Expectations.<Predicate<ObjectActionParameter>>anything()));
                will(returnValue(Can.ofCollection(_Lists.of(objectActionParameter))));
            }
        });

        assertThat(objectActionParameter.getStaticFriendlyName().get(), is("string"));
    }

    @Test @Ignore("ParamNamedFacet is always present - ensured by facet post processing")
    public void getName_whenNamedFaceNotPresentAndMultipleParamsOfSameType() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(2, parentAction, actionParamPeer);
        objectActionParameter.setSpecification(stubSpecForString);

        context.checking(new Expectations() {
            {
                allowing(actionParamPeer).getFacet(ParamNamedFacet.class);
                will(returnValue(null));

                oneOf(parentAction).getParameters(with(Expectations.<Predicate<ObjectActionParameter>>anything()));
                will(returnValue(Can.ofCollection(_Lists.of(stubObjectActionParameterString, objectActionParameter, stubObjectActionParameterString2))));
            }
        });

        assertThat(objectActionParameter.getStaticFriendlyName().get(), is("string 2"));
    }

}
