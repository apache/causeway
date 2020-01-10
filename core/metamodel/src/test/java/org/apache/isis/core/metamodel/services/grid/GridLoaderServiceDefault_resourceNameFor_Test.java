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
package org.apache.isis.core.metamodel.services.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class GridLoaderServiceDefault_resourceNameFor_Test {

    private GridLoaderServiceDefault gridLoaderServiceDefault;

    @Before
    public void setUp() throws Exception {
        gridLoaderServiceDefault = new GridLoaderServiceDefault();
    }

    @Test
    public void when_default_exists() {
        final String s = gridLoaderServiceDefault.resourceNameFor(new GridLoaderServiceDefault.DomainClassAndLayout(Foo.class, null));
        Assert.assertThat(s, is(equalTo("Foo.layout.xml")));
    }

    @Test
    public void when_fallback_exists() {
        final String s = gridLoaderServiceDefault.resourceNameFor(new GridLoaderServiceDefault.DomainClassAndLayout(Foo2.class, null));
        Assert.assertThat(s, is(equalTo("Foo2.layout.fallback.xml")));
    }
    @Test
    public void when_default_and_fallback_both_exist() {
        final String s = gridLoaderServiceDefault.resourceNameFor(new GridLoaderServiceDefault.DomainClassAndLayout(Foo3.class, null));
        Assert.assertThat(s, is(equalTo("Foo3.layout.xml")));
    }
    @Test
    public void when_neither_exist() {
        final String s = gridLoaderServiceDefault.resourceNameFor(new GridLoaderServiceDefault.DomainClassAndLayout(Foo4.class, null));
        Assert.assertNull(s);
    }
}