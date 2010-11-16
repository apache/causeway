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


package org.apache.isis.core.progmodel.facets.value;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetHolderImpl;
import org.apache.isis.core.progmodel.facets.value.PercentageValueSemanticsProvider;

@RunWith(JMock.class)
public class PercentageValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {
    PercentageValueSemanticsProvider adapter;
    private Object percentage;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
    	mockery.checking(new Expectations(){{
    		allowing(mockConfiguration).getString("isis.value.format.percentage");
    		will(returnValue(null));
    	}});
    	
        setupSpecification(Percentage.class);
        
        percentage = new Percentage(0.105f);
        allowMockAdapterToReturn(percentage);
        
        holder = new FacetHolderImpl();
        
        setValue(adapter = new PercentageValueSemanticsProvider(holder, mockConfiguration, mockSpecificationLoader, mockRuntimeContext));
    }

    @Test
    public void testAsEncodedString() {
        final String encoded = getEncodeableFacet().toEncodedString(mockAdapter);
        assertEquals("0.105", encoded);
    }

    @Test
    public void testParseTextEntryWithNumber() {
        final Object parsed = adapter.parseTextEntry(percentage, "21%");
        assertEquals(new Percentage(0.21f), parsed);
    }

    @Test
    public void testParseTextEntryWithNumberAndDecimalPoint() {
        final Object parsed = adapter.parseTextEntry(percentage, "21.4%");
        assertEquals(new Percentage(0.214f), parsed);
    }

    @Test
    public void testParseTextEntryWithBlank() {
        final Object parsed = adapter.parseTextEntry(percentage, "");
        assertEquals(null, parsed);
    }

    @Test
    public void testRestoreFromEncodedString() {
        final Object restored = adapter.fromEncodedString("0.2134");
        assertEquals(new Percentage(0.2134f), restored);
    }

    @Test
    public void testTitleOf() {
        assertEquals("10%", adapter.displayTitleOf(percentage));
    }

    @Test
    public void testFloatValue() {
        assertEquals(0.105f, adapter.floatValue(mockAdapter), 0.0f);
    }

}

