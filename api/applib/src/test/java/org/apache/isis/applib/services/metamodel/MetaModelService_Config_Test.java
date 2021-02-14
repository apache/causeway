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
package org.apache.isis.applib.services.metamodel;

import org.junit.Test;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotSame;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class MetaModelService_Config_Test {

    @Test
    public void ignore_noop() throws Exception {

        // when
        Config config = new Config();
        // then
        assertThat(config.isIgnoreNoop(), is(equalTo(false)));

        // and when
        Config config2 = config.withIgnoreNoop();

        // then
        assertNotSame(config, config2);
        assertThat(config.isIgnoreNoop(), is(equalTo(false)));
        assertThat(config2.isIgnoreNoop(), is(equalTo(true)));
    }

    @Test
    public void packages_prefixes() throws Exception {

        // when
        Config config = new Config();
        // then
        assertThat(config.getPackagePrefixes(), is(emptyCollectionOf(String.class)));

        // and when
        Config config2 = config.withPackagePrefix("org.foo");

        // then
        assertNotSame(config, config2);
        assertThat(config2.getPackagePrefixes().size(), is(equalTo(1)));
        assertThat(config2.getPackagePrefixes().iterator().next(), is(equalTo("org.foo")));

        // and when
        Config config3 = config2.withPackagePrefix("org.bar");

        // then
        assertNotSame(config, config3);
        assertNotSame(config2, config3);
        assertThat(config3.getPackagePrefixes().size(), is(equalTo(2)));
        assertTrue(config3.getPackagePrefixes().contains("org.foo"));
        assertTrue(config3.getPackagePrefixes().contains("org.bar"));
    }
}
