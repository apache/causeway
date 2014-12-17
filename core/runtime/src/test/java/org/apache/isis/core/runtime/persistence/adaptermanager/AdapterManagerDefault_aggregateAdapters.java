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

package org.apache.isis.core.runtime.persistence.adaptermanager;

import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.applib.annotation.Aggregated;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.app.IsisMetaModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapterFactory;
import org.apache.isis.core.runtime.system.persistence.OidGenerator;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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
    private OneToManyAssociation mockCollection;

    @Mock
    private RuntimeContext mockRuntimeContext;
    
    @Mock
    private OidGenerator mockOidGenerator;

    @Mock
    protected Localization mockLocalization;
    
    @Mock
    private AuthenticationSession mockAuthenticationSession;
    
    @Mock
    private IsisConfiguration mockConfiguration;
    
    private IsisMetaModel isisMetaModel;
    
    private ObjectAdapterFactory adapterFactory;
    
    private AdapterManagerDefault adapterManager;
    
    private Customer rootObject;
    private Name aggregatedObject;
    
    private ObjectAdapter persistentParentAdapter;
    private ObjectAdapter aggregatedAdapter;


    
    
    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        context.ignoring(mockRuntimeContext);
        context.ignoring(mockAuthenticationSession);
        context.ignoring(mockConfiguration);
        
        isisMetaModel = new IsisMetaModel(
                                mockRuntimeContext,
                                new ProgrammingModelFacetsJava5(),
                                Lists.newArrayList(new CustomerRepository()));
        isisMetaModel.init();

        adapterFactory = new PojoAdapterFactory() {
            @Override
            protected Localization getLocalization() {
                return mockLocalization;
            }
            @Override
            protected SpecificationLoaderSpi getSpecificationLoader() {
                return isisMetaModel.getSpecificationLoader();
            }
            
            @Override
            protected AuthenticationSession getAuthenticationSession() {
                return mockAuthenticationSession;
            }
        };

        adapterManager = new AdapterManagerDefault(new PojoRecreatorDefault()) {
            @Override
            protected SpecificationLoaderSpi getSpecificationLoader() {
                return isisMetaModel.getSpecificationLoader();
            }
            @Override
            protected ObjectAdapterFactory getObjectAdapterFactory() {
                return adapterFactory;
            }
            @Override
            public OidGenerator getOidGenerator() {
                return mockOidGenerator;
            }
            @Override
            protected ServicesInjector getServicesInjector() {
                return isisMetaModel.getDependencyInjector();
            }
        };

        rootObject = new Customer();
        aggregatedObject = new Name();
        
        persistentParentAdapter = adapterManager.mapRecreatedPojo(
                RootOidDefault.create(ObjectSpecId.of("CUS"), "1"), rootObject);
    }

    private void allowing_oidGenerator_createAggregatedOid(final Object value, final AggregatedOid resultOid) {
        context.checking(new Expectations() {
            {
                allowing(mockOidGenerator).createAggregateOid(with(equalTo(value)), with(any(ObjectAdapter.class)));
                will(returnValue(resultOid));
                ignoring(mockOidGenerator);
            }
        });
    }


    @Test
    public void adapterFor_whenAggregated() throws Exception {
        // given
        allowing_oidGenerator_createAggregatedOid(
                aggregatedObject, 
                new AggregatedOid(ObjectSpecId.of("NME"), (TypedOid) persistentParentAdapter.getOid(), "123"));
        
        // when
        aggregatedAdapter = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter);

        // then
        final AggregatedOid aggregatedOid = (AggregatedOid) aggregatedAdapter.getOid();
        assertEquals(persistentParentAdapter.getOid(), aggregatedOid.getParentOid());
    }

    @Test
    public void testOidHasSubId() throws Exception {
        allowing_oidGenerator_createAggregatedOid(aggregatedObject, new AggregatedOid(ObjectSpecId.of("NME"), (TypedOid) persistentParentAdapter.getOid(), "123"));
        aggregatedAdapter = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter);

        final AggregatedOid aggregatedOid = (AggregatedOid) aggregatedAdapter.getOid();
        assertEquals("123", aggregatedOid.getLocalId());
    }

    @Test
    public void getResolveState_isInitiallyGhost() throws Exception {
        allowing_oidGenerator_createAggregatedOid(aggregatedObject, new AggregatedOid(ObjectSpecId.of("NME"), (TypedOid) persistentParentAdapter.getOid(), "123"));
        aggregatedAdapter = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter);

        assertEquals(ResolveState.GHOST, aggregatedAdapter.getResolveState());
    }

    @Test
    public void testSameParametersRetrievesSameAdapter() throws Exception {
        allowing_oidGenerator_createAggregatedOid(aggregatedObject, new AggregatedOid(ObjectSpecId.of("NME"), (TypedOid) persistentParentAdapter.getOid(), "123"));
        aggregatedAdapter = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter);

        final ObjectAdapter valueAdapter2 = adapterManager.adapterFor(aggregatedObject, persistentParentAdapter, mockCollection);
        assertSame(aggregatedAdapter, valueAdapter2);
    }

}
