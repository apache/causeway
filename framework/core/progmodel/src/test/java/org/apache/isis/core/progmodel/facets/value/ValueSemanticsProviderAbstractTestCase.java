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

import static org.apache.isis.core.testsupport.jmock.ReturnArgumentJMockAction.returnArgument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.map.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.progmodel.facets.object.encodeable.EncodableFacetUsingEncoderDecoder;
import org.apache.isis.core.progmodel.facets.object.parseable.ParseableFacetUsingParser;
import org.apache.isis.core.progmodel.facets.object.value.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.progmodel.facets.object.value.ValueSemanticsProviderContext;

@RunWith(JMock.class)
public abstract class ValueSemanticsProviderAbstractTestCase {

    protected Mockery mockery = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private ValueSemanticsProviderAndFacetAbstract<?> valueSemanticsProvider;
    private EncodableFacetUsingEncoderDecoder encodeableFacet;
    private ParseableFacetUsingParser parseableFacet;

    protected FacetHolder mockFacetHolder;

    protected IsisConfiguration mockConfiguration;
    protected ValueSemanticsProviderContext mockContext;

    protected SpecificationLoaderSpi mockSpecificationLoader;
    protected ServicesInjector mockDependencyInjector;
    protected AdapterManager mockAdapterManager;
    protected AuthenticationSessionProvider mockAuthenticationSessionProvider;
    protected ObjectAdapter mockAdapter;

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.UK);

        mockFacetHolder = mockery.mock(FacetHolder.class);
        mockConfiguration = mockery.mock(IsisConfiguration.class);

        mockContext = mockery.mock(ValueSemanticsProviderContext.class);

        mockDependencyInjector = mockery.mock(ServicesInjector.class);
        mockAdapterManager = mockery.mock(AdapterManager.class);
        mockAuthenticationSessionProvider = mockery.mock(AuthenticationSessionProvider.class);
        mockSpecificationLoader = mockery.mock(SpecificationLoaderSpi.class);

        mockery.checking(new Expectations() {
            {
                allowing(mockConfiguration).getString(with(any(String.class)), with(any(String.class)));
                will(returnArgument(1));

                allowing(mockConfiguration).getBoolean(with(any(String.class)), with(any(Boolean.class)));
                will(returnArgument(1));

                allowing(mockConfiguration).getString("isis.locale");
                will(returnValue(null));

                allowing(mockDependencyInjector).injectServicesInto(with(any(Object.class)));

                never(mockAuthenticationSessionProvider);

                never(mockAdapterManager);
            }
        });

        mockAdapter = mockery.mock(ObjectAdapter.class);
    }

    @After
    public void tearDown() throws Exception {
        mockery.assertIsSatisfied();
    }

    protected void allowMockAdapterToReturn(final Object pojo) {
        mockery.checking(new Expectations() {
            {
                allowing(mockAdapter).getObject();
                will(returnValue(pojo));
            }
        });
    }

    protected void setValue(final ValueSemanticsProviderAndFacetAbstract<?> value) {
        this.valueSemanticsProvider = value;
        this.encodeableFacet = new EncodableFacetUsingEncoderDecoder(value, mockFacetHolder, mockAdapterManager, mockDependencyInjector);
        this.parseableFacet = new ParseableFacetUsingParser(value, mockFacetHolder, mockAuthenticationSessionProvider, mockDependencyInjector, mockAdapterManager);
    }

    protected ValueSemanticsProviderAndFacetAbstract<?> getValue() {
        return valueSemanticsProvider;
    }

    protected EncodableFacet getEncodeableFacet() {
        return encodeableFacet;
    }

    protected ParseableFacet getParseableFacet() {
        return parseableFacet;
    }

    protected void setupSpecification(final Class<?> type) {
        // final TestProxySpecification specification =
        // system.getSpecification(cls);
        // specification.setupHasNoIdentity(true);
    }

    protected ObjectAdapter createAdapter(final Object object) {
        // return system.createAdapterForTransient(object);
        return mockAdapter;
    }

    @Test
    public void testParseNull() throws Exception {
        try {
            valueSemanticsProvider.parseTextEntry(null, null, null);
            fail();
        } catch (final IllegalArgumentException expected) {
        }
    }

    @Test
    public void testParseEmptyString() throws Exception {
        final Object newValue = valueSemanticsProvider.parseTextEntry(null, "", null);
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
        assertEquals("", valueSemanticsProvider.displayTitleOf(null, (Localization) null));
    }

}
