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
package org.apache.isis.config.builder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.builder.IsisConfigurationDefault;

public class IsisConfigurationDefaultTest_createSubset {
    
    private IsisConfigurationDefault configuration;

    @Before
    public void setUp() throws Exception {
        configuration = new IsisConfigurationDefault();
    }

    @After
    public void tearDown() throws Exception {
        configuration = null;
    }

    @Test
    public void empty() {
        final IsisConfiguration subset = configuration.createSubset("foo");
        assertThat(subset.iterator().hasNext(), is(false));
    }

    @Test
    public void nonEmptyButNoneInSubset() {
        configuration.add("bar", "barValue");
        final IsisConfiguration subset = configuration.createSubset("foo");
        assertThat(subset.iterator().hasNext(), is(false));
    }

    @Test
    public void nonEmptyButSingleKeyedInSubset() {
        configuration.add("foo", "fooValue");
        final IsisConfiguration subset = configuration.createSubset("foo");
        final Iterator<String> iterator = subset.iterator();
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void nonEmptyAndMultiKeyedInSubset() {
        configuration.add("foo.foz", "fozValue");
        final IsisConfiguration subset = configuration.createSubset("foo");
        final Iterator<String> iterator = subset.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is("foz"));
        assertThat(iterator.hasNext(), is(false));
        assertThat(subset.getString("foz"), is("fozValue"));
    }

    @Test
    public void propertiesOutsideOfSubsetAreIgnored() {
        configuration.add("foo.foz", "fozValue");
        configuration.add("foo.faz", "fazValue");
        configuration.add("bar.baz", "bazValue");
        final IsisConfiguration subset = configuration.createSubset("foo");
        assertThat(subset.getString("foz"), is("fozValue"));
        assertThat(subset.getString("faz"), is("fazValue"));
        
        final Iterator<String> iterator = subset.iterator();
        assertThat(iterator.hasNext(), is(true));
        iterator.next();
        assertThat(iterator.hasNext(), is(true));
        iterator.next();
        assertThat(iterator.hasNext(), is(false));
    }

}
