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

package org.apache.isis.core.progmodel.facets.collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Iterator;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.map.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.progmodel.facets.collections.collection.JavaCollectionFacet;

@RunWith(JMock.class)
public class JavaCollectionFacetTest {

    private JavaCollectionFacet facet;

    private final Mockery mockery = new JUnit4Mockery();

    private FacetHolder mockFacetHolder;

    private ObjectAdapter mockCollection;
    private Collection<ObjectAdapter> mockWrappedCollection;
    private Iterator<ObjectAdapter> mockIterator;

    private AdapterManager mockAdapterManager;

    @Before
    public void setUp() throws Exception {
        mockFacetHolder = mockery.mock(FacetHolder.class);
        mockCollection = mockery.mock(ObjectAdapter.class);
        mockWrappedCollection = mockery.mock(Collection.class);
        mockIterator = mockery.mock(Iterator.class);
        mockAdapterManager = mockery.mock(AdapterManager.class);

        facet = new JavaCollectionFacet(mockFacetHolder, mockAdapterManager);
    }

    @After
    public void tearDown() throws Exception {
        facet = null;
    }

    @Test
    public void firstElementForEmptyCollectionIsNull() {
        mockery.checking(new Expectations() {
            {
                one(mockCollection).getObject();
                will(returnValue(mockWrappedCollection));

                one(mockWrappedCollection).size();
                will(returnValue(0));

                one(mockWrappedCollection).iterator();
                will(returnValue(mockIterator));

                one(mockIterator).hasNext();
                will(returnValue(false));
            }
        });
        assertThat(facet.firstElement(mockCollection), is(nullValue()));
    }

}
