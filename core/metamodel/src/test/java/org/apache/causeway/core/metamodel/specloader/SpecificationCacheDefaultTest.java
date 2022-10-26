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
package org.apache.causeway.core.metamodel.specloader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.val;

class SpecificationCacheDefaultTest {

    private LogicalType cus = _LogicalTypeTestFactory.cus();
    private LogicalType ord = _LogicalTypeTestFactory.ord();

    ObjectSpecification customerSpec;
    ObjectSpecification orderSpec;

    private SpecificationCache<ObjectSpecification> specificationCache =
            new SpecificationCacheDefault<>();
    private LogicalTypeResolver logicalTypeResolver =
            new LogicalTypeResolverDefault();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @BeforeEach
    public void setUp() throws Exception {

        customerSpec = Mockito.mock(ObjectSpecification.class);
        orderSpec = Mockito.mock(ObjectSpecification.class);

        Mockito.when(customerSpec.getCorrespondingClass()).thenReturn((Class)Customer.class);
        Mockito.when(customerSpec.getLogicalType()).thenReturn(cus);

        Mockito.when(orderSpec.getCorrespondingClass()).thenReturn((Class)Order.class);
        Mockito.when(orderSpec.getLogicalType()).thenReturn(ord);
    }

    @AfterEach
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
        val type = logicalTypeResolver.lookup(cus.getLogicalTypeName());
        assertFalse(type.isPresent());
    }

    @Test
    public void getByObjectType_whenSet() {

        specificationCache.computeIfAbsent(Customer.class, __->customerSpec);

        val objectSpec = specificationCache.lookup(Customer.class).orElse(null);

        assertSame(objectSpec, customerSpec);
    }

}
