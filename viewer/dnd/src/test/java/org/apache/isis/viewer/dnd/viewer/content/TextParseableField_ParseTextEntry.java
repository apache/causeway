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


package org.apache.isis.viewer.dnd.viewer.content;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.testsystem.TestProxySystem;
import org.apache.isis.viewer.dnd.view.field.TextParseableFieldImpl;


@RunWith(JMock.class)
public class TextParseableField_ParseTextEntry {

    private final Mockery context = new JUnit4Mockery();

    private ObjectAdapter mockParent;
    private ObjectAdapter mockChild;
    private OneToOneAssociation mockOtoa;
    private ObjectSpecification mockOtoaSpec;
    private ParseableFacet mockParseableFacet;
    private ObjectAdapter mockParsedText;
    private TextParseableFieldImpl fieldImpl;

    private ValidatingInteractionAdvisor mockValidatingInteractionAdvisorFacet;

    private TestProxySystem system;

    @Before
    public void setUp() throws Exception {
        system = new TestProxySystem();
        system.init();

        mockParent = context.mock(ObjectAdapter.class, "parent");
        mockChild = context.mock(ObjectAdapter.class, "child");
        mockOtoa = context.mock(OneToOneAssociation.class);
        mockOtoaSpec = context.mock(ObjectSpecification.class);
        mockParseableFacet = context.mock(ParseableFacet.class);
        mockParsedText = context.mock(ObjectAdapter.class, "parsedText");
        mockValidatingInteractionAdvisorFacet = context.mock(ValidatingInteractionAdvisor.class);

        context.checking(new Expectations() {
            {
                allowing(mockOtoa).getIdentifier();

                allowing(mockOtoa).getSpecification();
                will(returnValue(mockOtoaSpec));

                one(mockOtoaSpec).getFacet(ParseableFacet.class);
                will(returnValue(mockParseableFacet));
            }
        });

        fieldImpl = new TextParseableFieldImpl(mockParent, mockChild, mockOtoa);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void parsedTextIsValidForSpecAndCorrespondingObjectValidAsAssociation() {

        context.checking(new Expectations() {
            {
                one(mockParseableFacet).parseTextEntry(mockChild, "foo");
                will(returnValue(mockParsedText));

                atLeast(1).of(mockOtoa).isAssociationValid(mockParent, mockParsedText);
                will(returnValue(Allow.DEFAULT));

                one(mockOtoa).isMandatory();
            }
        });

        fieldImpl.parseTextEntry("foo");
    }

    @Test(expected = InvalidEntryException.class)
    public void parsedTextIsNullWhenMandatoryThrowsException() {

        context.checking(new Expectations() {
            {
                one(mockParseableFacet).parseTextEntry(mockChild, "foo");
                will(returnValue(null));

                atLeast(1).of(mockOtoa).isAssociationValid(mockParent, null);
                will(returnValue(Allow.DEFAULT));

                one(mockOtoa).isMandatory();
                will(returnValue(true));
            }
        });

        fieldImpl.parseTextEntry("foo");
    }

    @Test
    public void parsedTextIsValidAccordingToSpecificationFacet() {

        context.checking(new Expectations() {
            {
                one(mockParseableFacet).parseTextEntry(mockChild, "foo");
                will(returnValue(mockParsedText));

                atLeast(1).of(mockOtoa).isAssociationValid(mockParent, mockParsedText);
                will(returnValue(Allow.DEFAULT));

                allowing(mockOtoa).isMandatory();
                will(returnValue(true));
            }
        });

        fieldImpl.parseTextEntry("foo");
    }

    @Test(expected = InvalidEntryException.class)
    public void parsedTextIsInvalidAccordingToSpecification() {

        context.checking(new Expectations() {
            {
                allowing(mockParseableFacet).parseTextEntry(mockChild, "foo");
                will(returnValue(mockParsedText));

                atLeast(1).of(mockOtoa).isAssociationValid(mockParent, mockParsedText);
                will(returnValue(Veto.DEFAULT));

                allowing(mockOtoa).isMandatory();
                will(returnValue(true));
            }
        });

        fieldImpl.parseTextEntry("foo");
    }

    @Test(expected = InvalidEntryException.class)
    public void parsedTextIsInvalidAccordingToAssociation() {

        context.checking(new Expectations() {
            {
                allowing(mockParseableFacet).parseTextEntry(mockChild, "foo");
                will(returnValue(mockParsedText));
                
                one(mockOtoa).isAssociationValid(mockParent, mockParsedText);
                will(returnValue(Veto.DEFAULT));
            }
        });

        fieldImpl.parseTextEntry("foo");
    }

}
