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
package org.apache.causeway.core.metamodel.services.grid;

import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.core.metamodel.services.grid.GridLoader.LayoutKey;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResource;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoader;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoaderDefault;

class GridCache_resourceNameTest {

    private GridCache gridCache;
    private LayoutResourceLoader layoutResourceLoader;

    @BeforeEach
    void setUp() throws Exception {
        layoutResourceLoader = new LayoutResourceLoaderDefault();
        gridCache = new GridCache(null, false, List.of(layoutResourceLoader));
    }

    @Test
    void when_default_exists() {
        assertEquals(
                "Foo.layout.xml",
                resourceNameFor(new GridLoader.LayoutKey(Foo.class, null)));
    }

    @Test
    void when_fallback_exists() {
        assertEquals(
                "Foo2.layout.fallback.xml",
                resourceNameFor(new GridLoader.LayoutKey(Foo2.class, null)));
    }

    @Test
    void when_default_and_fallback_both_exist() {
        assertEquals(
                "Foo3.layout.xml",
                resourceNameFor(new GridLoader.LayoutKey(Foo3.class, null)));
    }

    @Test
    void when_neither_exist() {
        assertEquals(
                (String)null,
                resourceNameFor(new GridLoader.LayoutKey(Foo4.class, null)));
    }

    // -- HELPER

    private String resourceNameFor(final LayoutKey dcal) {
        return gridCache.gridLoader().loadLayoutResource(dcal, EnumSet.of(CommonMimeType.XML))
            .map(LayoutResource::resourceName)
            .orElse(null);
    }

}
