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


package org.apache.isis.remoting.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.alternatives.remoting.common.exchange.ClearValueRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ClearValueResponse;
import org.apache.isis.alternatives.remoting.common.exchange.SetValueRequest;
import org.apache.isis.alternatives.remoting.common.exchange.SetValueResponse;
import org.apache.isis.alternatives.remoting.common.facade.ServerFacade;
import org.apache.isis.alternatives.remoting.common.facade.impl.ServerFacadeImpl;
import org.apache.isis.alternatives.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.ConcurrencyException;
import org.apache.isis.core.runtime.testsystem.ProxyJunit4TestCase;
import org.apache.isis.core.runtime.testsystem.TestProxyAssociation;
import org.apache.isis.core.runtime.testsystem.TestProxyVersion;
import org.apache.isis.remoting.data.DummyEncodeableObjectData;
import org.apache.isis.remoting.data.DummyReferenceData;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JMock.class)
public class ServerFacadeImpl_ParseableAssociationsTest extends ProxyJunit4TestCase {

    private Mockery mockery = new JUnit4Mockery();

    private ServerFacadeImpl server;
    private AuthenticationSession session;
    private DummyReferenceData movieData;
    private ObjectAdapter object;
    private TestProxyAssociation nameField;

    private AuthenticationManager mockAuthenticationManager;
    private ObjectEncoderDecoder mockObjectEncoder;

    /**
     * Testing the {@link ServerFacadeImpl} implementation of {@link ServerFacade}.
     *
     * <p>
     * This uses the encoder to unmarshall objects
     * and then calls the persistor and reflector; all of which should be mocked.
     */
    @Before
    public void setUp() throws Exception {

        mockAuthenticationManager = mockery.mock(AuthenticationManager.class);
        mockObjectEncoder = mockery.mock(ObjectEncoderDecoder.class);

        server = new ServerFacadeImpl(mockAuthenticationManager);
        server.setEncoder(mockObjectEncoder);
        server.init();

        object = system.createPersistentTestObject();

        final TestProxySpecification spec = (TestProxySpecification) object.getSpecification();
        nameField = new TestProxyAssociation("name", system.getSpecification(String.class));
        spec.setupFields(new ObjectAssociation[] { nameField });

        movieData = new DummyReferenceData(object.getOid(), "none", new TestProxyVersion(1));

    }

    @After
    public void tearDown() throws Exception {
        system.shutdown();
    }

    /**
     * TODO: other tests for clear: - clear collection element - fails if unauthorised - fails if unavailable
     *
     * <p>
     * could place all these clear test in one class; test other methods in other classes
     */
    @Test
    public void testClearAssociation() {
        IsisContext.getTransactionManager().startTransaction();
        ClearValueRequest request = new ClearValueRequest(session, "name", movieData);
		ClearValueResponse response = server.clearValue(request );
		final ObjectData[] updatesData = response.getUpdates();
        IsisContext.getTransactionManager().endTransaction();

        nameField.assertFieldEmpty(object);
        assertEquals(0, updatesData.length);
    }

    @Test
    public void testSetValue() {
        final TestProxySpecification specf = system.getSpecification(String.class);
        specf.addFacet(new EncodableFacet() {
            public String toEncodedString(final ObjectAdapter object) {
                return null;
            }

            public ObjectAdapter fromEncodedString(final String encodedData) {
                return getAdapterManager().adapterFor(encodedData);
            }

            public Class facetType() {
                return EncodableFacet.class;
            }

            public FacetHolder getFacetHolder() {
                return null;
            }

            public void setFacetHolder(final FacetHolder facetHolder) {}

            public boolean alwaysReplace() {
                return false;
            }

            public boolean isDerived() {
            	return false;
            }

            public boolean isNoop() {
                return false;
            }
        	public Facet getUnderlyingFacet() {
        		return null;
        	}
        	public void setUnderlyingFacet(Facet underlyingFacet) {
        		throw new UnsupportedOperationException();
        	}

        });

        IsisContext.getTransactionManager().startTransaction();
        SetValueRequest request = new SetValueRequest(session, "name", movieData, new DummyEncodeableObjectData("name of movie"));
		SetValueResponse response = server.setValue(request);
		final ObjectData[] updates = response.getUpdates();
        IsisContext.getTransactionManager().endTransaction();

        nameField.assertField(object, "name of movie");
        assertEquals(0, updates.length);
    }

    @Test
    public void testSetAssociationFailsWithNonCurrentTarget() {
        try {
            object.setOptimisticLock(new TestProxyVersion(2));
            SetValueRequest request = new SetValueRequest(session, "name", movieData, new DummyEncodeableObjectData("name of movie"));
			server.setValue(request);
            fail();
        } catch (final ConcurrencyException expected) {}
    }

    @Test
    public void testSetAssociationFailsWhenInvisible() {
        nameField.setUpIsVisible(false);
        try {
            SetValueRequest request = new SetValueRequest(session, "name", movieData, new DummyEncodeableObjectData("name of movie"));
			server.setValue(request);
            fail();
        } catch (final IsisException expected) {
            assertEquals("can't modify field as not visible or editable", expected.getMessage());
        }
    }

    @Test
    public void testSetAssociationFailsWhenUnavailable() {
        nameField.setUpIsUnusableFor(object);
        try {
            SetValueRequest request = new SetValueRequest(session, "name", movieData, new DummyEncodeableObjectData("test data"));
			server.setValue(request);
            fail();
        } catch (final IsisException expected) {
            assertEquals("can't modify field as not visible or editable", expected.getMessage());
        }
    }


}
