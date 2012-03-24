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

package org.apache.isis.runtimes.dflt.runtime.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.Aggregated;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterLookup;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.pojo.PojoAdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.internal.OidAdapterHashMap;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.internal.PojoAdapterHashMap;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.embedded.EmbeddedContext;
import org.apache.isis.runtimes.embedded.IsisMetaModel;

public class AdapterManagerDefault_aggregateAdapters {

    public static class Customer {
        // {{ Name (property)
        private Name name;

        @MemberOrder(sequence = "1")
        public Name getName() {
            return name;
        }

        public void setName(final Name name) {
            this.name = name;
        }
        // }}
    }
    
    @Aggregated
    public static class Name {}
    
    public static class CustomerRepository {
        public Customer x() { return null; }
    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ObjectAssociation mockAssociation;

    @Mock
    private EmbeddedContext mockMetaModelContext;
    
    private AdapterManagerDefault adapterManager;
    
    private ObjectAdapter persistentParentAdapter;
    private ObjectAdapter aggregatedAdapter;

    private Name aggregatedObject;
    private ObjectAdapterFactory adapterFactory;


    private IsisMetaModel isisMetaModel;

    @Mock
    private OidGenerator mockOidGenerator;

    private Customer rootObject;

    @Mock
    protected Localization mockLocalization;
    
    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        isisMetaModel = new IsisMetaModel(mockMetaModelContext, new CustomerRepository());
        isisMetaModel.init();

        adapterManager = new AdapterManagerDefault();

        adapterFactory = new PojoAdapterFactory() {
            @Override
            protected Localization getLocalization() {
                return mockLocalization;
            }
            @Override
            protected SpecificationLoader getSpecificationLoader() {
                return isisMetaModel.getSpecificationLoader();
            }
            @Override
            protected ObjectAdapterLookup getObjectAdapterLookup() {
                return adapterManager;
            }
        };
        
        adapterManager.setPojoAdapterMap(new PojoAdapterHashMap());
        adapterManager.setOidAdapterMap(new OidAdapterHashMap());
        adapterManager.setAdapterFactory(adapterFactory);
        adapterManager.setServicesInjector(isisMetaModel.getServicesInjector());
        adapterManager.setSpecificationLoader(isisMetaModel.getSpecificationLoader());
        adapterManager.setOidGenerator(mockOidGenerator);

        rootObject = new Customer();
        aggregatedObject = new Name();
        
        persistentParentAdapter = adapterManager.recreateAdapter(
                RootOidDefault.create("CUS", "1"), rootObject);
    }

    private void allowing_oidGenerator_createAggregatedLocalId(final Object value, final String result) {
        context.checking(new Expectations() {
            {
                allowing(mockOidGenerator).createAggregateLocalId(value);
                will(returnValue(result));
                ignoring(mockOidGenerator);
            }
        });
    }


    @Test
    public void adapterFor_whenAggregated() throws Exception {
        // given
        allowing_oidGenerator_createAggregatedLocalId(aggregatedObject, "123");
        
        // when
        aggregatedAdapter = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter);

        // then
        final AggregatedOid aggregatedOid = (AggregatedOid) aggregatedAdapter.getOid();
        assertEquals(persistentParentAdapter.getOid(), aggregatedOid.getParentOid());
    }

    @Test
    public void testOidHasSubId() throws Exception {
        allowing_oidGenerator_createAggregatedLocalId(aggregatedObject, "123");
        aggregatedAdapter = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter);

        final AggregatedOid aggregatedOid = (AggregatedOid) aggregatedAdapter.getOid();
        assertEquals("123", aggregatedOid.getLocalId());
    }

    @Test
    public void getResolveState_isInitiallyGhost() throws Exception {
        allowing_oidGenerator_createAggregatedLocalId(aggregatedObject, "123");
        aggregatedAdapter = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter);

        assertEquals(ResolveState.GHOST, aggregatedAdapter.getResolveState());
    }

    @Test
    public void testSameParametersRetrievesSameAdapter() throws Exception {
        allowing_oidGenerator_createAggregatedLocalId(aggregatedObject, "123");
        aggregatedAdapter = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter);

        final ObjectAdapter valueAdapter2 = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter, mockAssociation);
        assertSame(aggregatedAdapter, valueAdapter2);
    }

}
