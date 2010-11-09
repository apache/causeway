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


package org.apache.isis.core.progmodel.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.apache.isis.core.testsupport.jmock.ReturnArgumentJMockAction.returnArgument;

import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.progmodel.facets.object.encodeable.EncodableFacetUsingEncoderDecoder;
import org.apache.isis.core.progmodel.facets.object.parseable.ParseableFacetUsingParser;


@RunWith(JMock.class)
public abstract class ValueSemanticsProviderAbstractTestCase {
    
    protected Mockery mockery = new JUnit4Mockery();

    private ValueSemanticsProviderAbstract value;
    private EncodableFacetUsingEncoderDecoder encodeableFacet;
    private ParseableFacetUsingParser parseableFacet;
    
    protected FacetHolder mockFacetHolder;
    
    protected IsisConfiguration mockConfiguration;
    protected SpecificationLoader mockSpecificationLoader;
    protected RuntimeContext mockRuntimeContext;
    protected ObjectAdapter mockAdapter;

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.UK);
        
        mockFacetHolder = mockery.mock(FacetHolder.class);
        mockRuntimeContext = mockery.mock(RuntimeContext.class);
        mockSpecificationLoader = mockery.mock(SpecificationLoader.class);
        mockConfiguration = mockery.mock(IsisConfiguration.class);

        mockery.checking(new Expectations(){{
        	allowing(mockConfiguration).getString(with(any(String.class)), with(any(String.class)));
        	will(returnArgument(1));
        	
        	allowing(mockConfiguration).getBoolean(with(any(String.class)), with(any(Boolean.class)));
        	will(returnArgument(1));

        	allowing(mockConfiguration).getString("isis.locale");
        	will(returnValue(null));
        	
        	allowing(mockRuntimeContext).injectDependenciesInto(with(any(Object.class)));
        }});

        mockAdapter = mockery.mock(ObjectAdapter.class);
    }

    @After
    public void tearDown() throws Exception {
        mockery.assertIsSatisfied();
    }

	protected void allowMockAdapterToReturn(final Object pojo) {
		mockery.checking(new Expectations(){{
        	allowing(mockAdapter).getObject();
			will(returnValue( pojo ));
        }});
	}

    protected void setValue(final ValueSemanticsProviderAbstract value) {
        this.value = value;
        this.encodeableFacet = new EncodableFacetUsingEncoderDecoder(value, mockFacetHolder, mockRuntimeContext);
        this.parseableFacet = new ParseableFacetUsingParser(value, mockFacetHolder, mockRuntimeContext);
    }

	protected ValueSemanticsProviderAbstract getValue() {
        return value;
    }

    protected EncodableFacet getEncodeableFacet() {
        return encodeableFacet;
    }

    protected ParseableFacet getParseableFacet() {
        return parseableFacet;
    }


    protected void setupSpecification(final Class<?> type) {
//        final TestProxySpecification specification = system.getSpecification(cls);
//        specification.setupHasNoIdentity(true);
    }

    protected ObjectAdapter createAdapter(final Object object) {
        //return system.createAdapterForTransient(object);
    	return mockAdapter;
    }

    @Test
    public void testParseNull() throws Exception {
        try {
            value.parseTextEntry(null, null);
            fail();
        } catch (final IllegalArgumentException expected) {}
    }

    @Test
    public void testParseEmptyString() throws Exception {
        final Object newValue = value.parseTextEntry(null, "");
        assertNull(newValue);
    }

    @Test
    public void testDecodeNULL() throws Exception {
        final Object newValue = encodeableFacet.fromEncodedString(EncodableFacetUsingEncoderDecoder.ENCODED_NULL);
        assertNull(newValue);
    }

    @Test
    public void testEmptyEncoding() {
        assertEquals(EncodableFacetUsingEncoderDecoder.ENCODED_NULL, encodeableFacet.toEncodedString(null));
    }

    @Test
    public void testTitleOfForNullObject() {
        assertEquals("", value.displayTitleOf(null));
    }
    
    

}
