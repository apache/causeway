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
package org.apache.isis.viewer.bdd.common.registries;

import org.hamcrest.CoreMatchers;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.story.registries.AliasRegistryDefault;

@RunWith(JMock.class)
public class AliasesRegistryTest {

    private final Mockery mockery = new JUnit4Mockery();

    private AliasRegistryDefault registry;

    private ObjectAdapter mockAdapter1;
    private ObjectAdapter mockAdapter2;
    @SuppressWarnings("unused")
    private ObjectAdapter mockAdapter3;

    @Before
    public void setUp() throws Exception {
        mockAdapter1 = mockery.mock(ObjectAdapter.class, "adapter1");
        mockAdapter2 = mockery.mock(ObjectAdapter.class, "adapter2");
        mockAdapter3 = mockery.mock(ObjectAdapter.class, "adapter3");
        registry = new AliasRegistryDefault();
    }

    @Test
    public void registerOneAdapter() {
        final String heldAs1 = registry.aliasPrefixedAs("Foo", mockAdapter1);
        Assert.assertThat(heldAs1, CoreMatchers.is("Foo#1"));
    }

    @Test
    public void registerTwoAdaptersOfSamePrefix() {
        @SuppressWarnings("unused")
        final String heldAs1 = registry.aliasPrefixedAs("Foo", mockAdapter1);
        final String heldAs2 = registry.aliasPrefixedAs("Foo", mockAdapter2);
        Assert.assertThat(heldAs2, CoreMatchers.is("Foo#2"));
    }

    @Test
    public void registerAdaptersOfDiffereingPrefixes() {
        @SuppressWarnings("unused")
        final String heldAs1 = registry.aliasPrefixedAs("Foo", mockAdapter1);
        final String heldAs2 = registry.aliasPrefixedAs("Bar", mockAdapter2);
        Assert.assertThat(heldAs2, CoreMatchers.is("Bar#1"));
    }

}
