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


package org.apache.isis.core.runtime.persistence;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.progmodel.facets.object.aggregated.AggregatedFacetAlways;
import org.apache.isis.core.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.core.runtime.testsystem.TestProxyField;


public class PersistorUtil_ValueAdapterTest extends ProxyJunit3TestCase {

    private ObjectAdapter aggregatedAdapter;
    private ObjectAdapter parent;
    private TestProxyField field;
    private Object value;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        parent = system.createPersistentTestObject();
        field = new TestProxyField("fieldName", system.getSpecification(Object.class));
        FacetUtil.addFacet(new AggregatedFacetAlways(field));
        value = new Object();
        aggregatedAdapter = getAdapterManager().adapterFor(value, parent, field);
    }

    public void testOidKnowsParent() throws Exception {
        final AggregatedOid aggregatedOid = (AggregatedOid) aggregatedAdapter.getOid();
        assertEquals(parent.getOid(), aggregatedOid.getParentOid());
    }

    public void testOidKnowsField() throws Exception {
        final AggregatedOid aggregatedOid = (AggregatedOid) aggregatedAdapter.getOid();
        assertEquals("fieldName", aggregatedOid.getFieldName());
    }

    public void testResolveStateStartsAsGhost() throws Exception {
        assertEquals(ResolveState.GHOST, aggregatedAdapter.getResolveState());
    }

    public void testSameParametersRetrievesSameAdapter() throws Exception {
        final ObjectAdapter valueAdapter2 = getAdapterManager().adapterFor(value, parent, field);
        assertSame(aggregatedAdapter, valueAdapter2);

    }

}

