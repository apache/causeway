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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

@RunWith(JMock.class)
public class ObjectActionParameterAbstractTest_getName {

    private final static class ObjectActionParameterAbstractToTest extends ObjectActionParameterAbstract {
        private ObjectActionParameterAbstractToTest(int number, ObjectActionImpl objectAction, TypedHolder peer) {
            super(number, objectAction, peer);
        }
        
        private ObjectSpecification objectSpec;
        
        @Override
        public ObjectAdapter get(ObjectAdapter owner) {
            return null;
        }
        @Override
        public Instance getInstance(ObjectAdapter adapter) {
            return null;
        }
        @Override
        public FeatureType getFeatureType() {
            return null;
        }
        @Override
        public String isValid(ObjectAdapter adapter, Object proposedValue) {
            return null;
        }
        
        @Override
        public ObjectSpecification getSpecification() {
            return objectSpec;
        }
        
        public void setSpecification(ObjectSpecification objectSpec) {
            this.objectSpec = objectSpec;
        }
    }


    private ObjectActionParameterAbstractToTest objectActionParameter;
    private ObjectActionParameter stubObjectActionParameterString;
    private ObjectActionParameter stubObjectActionParameterString2;

    private final Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private ObjectActionImpl parentAction;
    private TypedHolder actionParamPeer;
    private NamedFacet namedFacet;

    private ObjectSpecification stubSpecForString, stubSpecForInt;

    @Before
    public void setUp() throws Exception {
        parentAction = context.mock(ObjectActionImpl.class);
        actionParamPeer = context.mock(TypedHolder.class);
        namedFacet = context.mock(NamedFacet.class);
        
        stubSpecForString = context.mock(ObjectSpecification.class, "specForString");
        context.checking(new Expectations() {
            {
                allowing(stubSpecForString).getSingularName();
                will(returnValue("string"));
            }
        });
        
        stubObjectActionParameterString = context.mock(ObjectActionParameter.class, "actionParamString");
        context.checking(new Expectations() {
            {
                allowing(stubObjectActionParameterString).getSpecification();
                will(returnValue(stubSpecForString));
            }
        });

        stubObjectActionParameterString2 = context.mock(ObjectActionParameter.class, "actionParamOtherString");
        context.checking(new Expectations() {
            {
                allowing(stubObjectActionParameterString2).getSpecification();
                will(returnValue(stubSpecForString));
            }
        });

    }


    @Test
    public void whenNamedFacetPresent() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);

        context.checking(new Expectations() {
            {
                one(actionParamPeer).getFacet(NamedFacet.class);
                will(returnValue(namedFacet));
                
                one(namedFacet).value();
                will(returnValue("Some parameter name"));
            }
        });
        
        assertThat(objectActionParameter.getName(), is("someParameterName"));
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
                
                one(parentAction).getParameters((Filter<ObjectActionParameter>) with(anything()));
                will(returnValue(Lists.newArrayList(objectActionParameter)));
            }
        });
        
        assertThat(objectActionParameter.getName(), is("string"));
    }

    @Test
    public void whenNamedFaceNotPresentAndMultipleParamsOfSameType() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(2, parentAction, actionParamPeer);
        objectActionParameter.setSpecification(stubSpecForString);

        context.checking(new Expectations() {
            {
                one(actionParamPeer).getFacet(NamedFacet.class);
                will(returnValue(null));
                
                one(parentAction).getParameters((Filter<ObjectActionParameter>) with(anything()));
                will(returnValue(Lists.newArrayList(stubObjectActionParameterString, objectActionParameter, stubObjectActionParameterString2)));
            }
        });
        
        assertThat(objectActionParameter.getName(), is("string1"));
    }

}
