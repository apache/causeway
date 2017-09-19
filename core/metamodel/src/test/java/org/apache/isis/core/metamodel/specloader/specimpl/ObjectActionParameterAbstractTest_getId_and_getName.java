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

import com.google.common.collect.Lists;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ObjectActionParameterAbstractTest_getId_and_getName {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ObjectActionDefault parentAction;
    @Mock
    private TypedHolder actionParamPeer;
    @Mock
    private NamedFacet namedFacet;

    @Mock
    private ObjectSpecification stubSpecForString;
    @Mock
    private ObjectActionParameter stubObjectActionParameterString;
    @Mock
    private ObjectActionParameter stubObjectActionParameterString2;


    private final static class ObjectActionParameterAbstractToTest extends ObjectActionParameterAbstract {
        private ObjectActionParameterAbstractToTest(final int number, final ObjectActionDefault objectAction, final TypedHolder peer) {
            super(FeatureType.ACTION_PARAMETER_SCALAR, number, objectAction, peer);
        }

        private ObjectSpecification objectSpec;

        @Override
        public ObjectAdapter get(final ObjectAdapter owner, final InteractionInitiatedBy interactionInitiatedBy) {
            return null;
        }

        @Override
        public FeatureType getFeatureType() {
            return null;
        }

        @Override
        public String isValid(
                final ObjectAdapter objectAdapter,
                final Object proposedValue,
                final InteractionInitiatedBy interactionInitiatedBy) {
            return null;
        }

        @Override
        public ObjectSpecification getSpecification() {
            return objectSpec;
        }

        public void setSpecification(final ObjectSpecification objectSpec) {
            this.objectSpec = objectSpec;
        }

    }

    private ObjectActionParameterAbstractToTest objectActionParameter;

    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(stubSpecForString).getSingularName();
                will(returnValue("string"));

                allowing(stubObjectActionParameterString).getSpecification();
                will(returnValue(stubSpecForString));

                allowing(stubObjectActionParameterString2).getSpecification();
                will(returnValue(stubSpecForString));
            }
        });

    }

    @Test
    public void getId_whenNamedFacetPresent() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);

        context.checking(new Expectations() {
            {
                one(actionParamPeer).getFacet(NamedFacet.class);
                will(returnValue(namedFacet));

                atLeast(1).of(namedFacet).value();
                will(returnValue("Some parameter name"));
            }
        });

        assertThat(objectActionParameter.getId(), is("someParameterName"));
    }

    @Test
    public void getName_whenNamedFacetPresent() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);

        context.checking(new Expectations() {
            {
                one(actionParamPeer).getFacet(NamedFacet.class);
                will(returnValue(namedFacet));

                atLeast(1).of(namedFacet).value();
                will(returnValue("Some parameter name"));
            }
        });

        assertThat(objectActionParameter.getName(), is("Some parameter name"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenNamedFaceNotPresentAndOnlyOneParamOfType() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);
        objectActionParameter.setSpecification(stubSpecForString);

        context.checking(new Expectations() {
            {
                one(actionParamPeer).getFacet(NamedFacet.class);
                will(returnValue(null));

                one(parentAction).getParameters(with((Filter<ObjectActionParameter>)anything()));
                will(returnValue(Lists.newArrayList(objectActionParameter)));
            }
        });

        assertThat(objectActionParameter.getName(), is("string"));
    }

    @Test
    public void getId_whenNamedFaceNotPresentAndMultipleParamsOfSameType() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(2, parentAction, actionParamPeer);
        objectActionParameter.setSpecification(stubSpecForString);

        context.checking(new Expectations() {
            {
                one(actionParamPeer).getFacet(NamedFacet.class);
                will(returnValue(null));

                one(parentAction).getParameters(with((Filter<ObjectActionParameter>) anything()));
                will(returnValue(Lists.newArrayList(stubObjectActionParameterString, objectActionParameter, stubObjectActionParameterString2)));
            }
        });

        assertThat(objectActionParameter.getId(), is("string2"));
    }

    @Test
    public void getName_whenNamedFaceNotPresentAndMultipleParamsOfSameType() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(2, parentAction, actionParamPeer);
        objectActionParameter.setSpecification(stubSpecForString);

        context.checking(new Expectations() {
            {
                one(actionParamPeer).getFacet(NamedFacet.class);
                will(returnValue(null));

                one(parentAction).getParameters(with((Filter<ObjectActionParameter>) anything()));
                will(returnValue(Lists.newArrayList(stubObjectActionParameterString, objectActionParameter, stubObjectActionParameterString2)));
            }
        });

        assertThat(objectActionParameter.getName(), is("string 2"));
    }

}
