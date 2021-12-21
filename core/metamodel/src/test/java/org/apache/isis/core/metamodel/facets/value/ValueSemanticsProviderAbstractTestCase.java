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
package org.apache.isis.core.metamodel.facets.value;

import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.encoder.EncodableFacetFromValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.valuesemantics.StringValueSemantics;

public abstract class ValueSemanticsProviderAbstractTestCase {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock protected FacetHolder mockFacetHolder;
    @Mock protected InteractionProvider mockInteractionProvider;
    @Mock protected ManagedObject mockAdapter;

    protected MetaModelContext metaModelContext;

    //private ValueSemanticsProviderAndFacetAbstract<?> valueSemanticsProvider;
    private ValueSemanticsProvider<?> valueSemanticsProvider;
    private EncodableFacetFromValueFacet encodeableFacet;

    @Before
    public void setUp() throws Exception {

        Locale.setDefault(Locale.UK);

        metaModelContext = MetaModelContext_forTesting.builder()
                .interactionProvider(mockInteractionProvider)
                .build();

        context.checking(new Expectations() {
            {

                never(mockInteractionProvider);
                //never(mockSessionServiceInternal);

                allowing(mockFacetHolder).getMetaModelContext();
                will(returnValue(metaModelContext));
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        context.assertIsSatisfied();
    }

    protected void allowMockAdapterToReturn(final Object pojo) {
        context.checking(new Expectations() {
            {
                allowing(mockAdapter).getPojo();
                will(returnValue(pojo));
            }
        });
    }

    protected void setSemantics(final ValueSemanticsAbstract<?> valueSemantics) {
        this.valueSemanticsProvider = valueSemantics;
        this.encodeableFacet = EncodableFacetFromValueFacet.forTesting(
                valueSemantics.getEncoderDecoder(),
                mockFacetHolder);
    }

    protected EncodableFacet getEncodeableFacet() {
        return encodeableFacet;
    }

    protected ManagedObject createAdapter(final Object object) {
        return mockAdapter;
    }

    @Test
    public void testParseNull() throws Exception {
        Assume.assumeThat(valueSemanticsProvider.getParser(), is(not(nullValue())));
        assertEquals(null, valueSemanticsProvider.getParser().parseTextRepresentation(null, null));
    }

    @Test
    public void testParseEmptyString() throws Exception {
        Assume.assumeThat(valueSemanticsProvider.getParser(), is(not(nullValue())));

        final Object newValue = valueSemanticsProvider.getParser().parseTextRepresentation(null, "");

        if(valueSemanticsProvider instanceof StringValueSemantics) {
            // string parsing is an unary identity
            assertEquals("", newValue);
        } else {
            assertNull(newValue);
        }

    }

    @Test
    public void testDecodeNULL() throws Exception {
        Assume.assumeThat(valueSemanticsProvider.getEncoderDecoder(), is(not(nullValue())));

        final Object newValue = encodeableFacet.fromEncodedString(EncodableFacetFromValueFacet.ENCODED_NULL);
        assertNull(newValue);
    }

    @Test
    public void testEmptyEncoding() {
        Assume.assumeThat(valueSemanticsProvider.getEncoderDecoder(), is(not(nullValue())));

        assertEquals(EncodableFacetFromValueFacet.ENCODED_NULL, encodeableFacet.toEncodedString(null));
    }

    @Test
    public void testTitleOfForNullObject() {

        if(valueSemanticsProvider instanceof StringValueSemantics) {
            // string representation has null-to-empty semantics
            assertEquals("",
                    valueSemanticsProvider.getRenderer().simpleTextPresentation(null, null));
        } else {
            assertEquals(ValueSemanticsAbstract.NULL_REPRESENTATION,
                    valueSemanticsProvider.getRenderer().simpleTextPresentation(null, null));
        }

    }
}
