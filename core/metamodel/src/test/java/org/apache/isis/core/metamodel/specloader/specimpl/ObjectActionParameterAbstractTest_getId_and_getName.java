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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

//FIXME[ISIS-3207]
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
class ObjectActionParameterAbstractTest_getId_and_getName {

    @Mock ObjectActionDefault parentAction;
    @Mock TypedHolder actionParamPeer;
    @Mock ParamNamedFacet namedFacet;
    @Mock FacetedMethod mockFacetedMethod;

    @Mock ObjectSpecification stubSpecForString;
    @Mock ObjectActionParameter stubObjectActionParameterString;
    @Mock ObjectActionParameter stubObjectActionParameterString2;

    private static final class ObjectActionParameterAbstractToTest
    extends ObjectActionParameterAbstract {
        private ObjectActionParameterAbstractToTest(
                final int number,
                final ObjectActionDefault objectAction,
                final TypedHolder peer) {
            super(FeatureType.ACTION_PARAMETER_SCALAR, number, null, objectAction);
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

    @BeforeEach
    public void setUp() throws Exception {

//FIXME
//        Mockito.when(stubSpecForString.getSingularName()).thenReturn("string");
//        Mockito.when(stubObjectActionParameterString.getElementType()).thenReturn(stubSpecForString);
//        Mockito.when(stubObjectActionParameterString2.getElementType()).thenReturn(stubSpecForString);
//        Mockito.when(parentAction.getFacetedMethod()).thenReturn(mockFacetedMethod);
//        Mockito.when(mockFacetedMethod.getParameters())
//        .thenReturn(Can.<FacetedMethodParameter>of(
//                (FacetedMethodParameter)stubObjectActionParameterString,
//                (FacetedMethodParameter)objectActionParameter,
//                (FacetedMethodParameter)stubObjectActionParameterString2));
//        Mockito.when(mockFacetedMethod.getMethod())
//        .thenReturn(Customer.class.getMethod("aMethod", new Class[] {Object.class, Object.class, Object.class}));
    }

    @Test
    public void getId_whenNamedFacetPresent() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);

        assertThat(objectActionParameter.getId(), is("someParameterName"));
    }

    @Test
    public void getName_whenNamedFacetPresent() throws Exception {

        objectActionParameter = new ObjectActionParameterAbstractToTest(0, parentAction, actionParamPeer);

//FIXME
//        context.checking(new Expectations() {
//            {
//
//                oneOf(stubObjectActionParameterString).getFacet(ParamNamedFacet.class);
//                will(returnValue(namedFacet));
//
//                atLeast(1).of(namedFacet).translated();
//                will(returnValue("Some parameter name"));
//            }
//        });

        assertThat(objectActionParameter.getStaticFriendlyName().get(), is("Some parameter name"));
    }


}
