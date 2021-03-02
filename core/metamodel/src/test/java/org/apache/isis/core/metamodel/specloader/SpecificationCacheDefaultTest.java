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
package org.apache.isis.core.metamodel.specloader;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel.adapter.oid.LogicalTypeTestFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public class SpecificationCacheDefaultTest {
    
    private LogicalType cus = LogicalTypeTestFactory.cus();
    private LogicalType ord = LogicalTypeTestFactory.ord();

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Mock
    private ObjectSpecification customerSpec;
    @Mock
    private ObjectSpecification orderSpec;

    private SpecificationCache<ObjectSpecification> specificationCache = new SpecificationCacheDefault<>();
    private LogicalTypeResolver specIdToClassResolver = new LogicalTypeResolverDefault();

    @Before
    public void setUp() throws Exception {

        context.checking(new Expectations() {{
            allowing(customerSpec).getCorrespondingClass();
            will(returnValue(Customer.class));

            allowing(customerSpec).getLogicalType();
            will(returnValue(cus));
            
            allowing(orderSpec).getCorrespondingClass();
            will(returnValue(Order.class));
            
            allowing(orderSpec).getLogicalType();
            will(returnValue(ord));
            
        }});
    }

    @After
    public void tearDown() throws Exception {
        specificationCache = null;
    }

    static class Customer {}
    static class Order {}

    @Test
    public void get_whenNotCached() {
        assertFalse(specificationCache.lookup(Customer.class).isPresent());
    }

    @Test
    public void get_whenCached() {
        
        specificationCache.computeIfAbsent(Customer.class, __->customerSpec);

        final ObjectSpecification objectSpecification = specificationCache.lookup(Customer.class)
                .orElse(null);

        assertSame(objectSpecification, customerSpec);
    }


    @Test
    public void allSpecs_whenCached() {
        specificationCache.computeIfAbsent(Customer.class, __->customerSpec);
        specificationCache.computeIfAbsent(Order.class, __->orderSpec);

        val allSpecs = specificationCache.snapshotSpecs();

        assertThat(allSpecs.size(), is(2));
    }

    @Test
    public void getByObjectType_whenNotSet() {
        val type = specIdToClassResolver.lookup(cus.getLogicalTypeName());
        assertFalse(type.isPresent());
    }

    @Test
    public void getByObjectType_whenSet() {
        
        specificationCache.computeIfAbsent(Customer.class, __->customerSpec);
        
        val objectSpec = specificationCache.lookup(Customer.class).orElse(null);

        assertSame(objectSpec, customerSpec);
    }

}
