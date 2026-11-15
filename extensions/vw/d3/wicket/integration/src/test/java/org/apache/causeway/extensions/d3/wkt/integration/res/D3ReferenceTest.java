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
package org.apache.causeway.extensions.d3.wkt.integration.res;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.causeway.extensions.d3.wkt.integration.D3TestApplication;

/**
 * Verifies that the D3 webjar reference resolves (requires webjars to be installed,
 * as done by {@link D3TestApplication}).
 */
@DisplayName("D3Reference — D3 webjar resource reference")
class D3ReferenceTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new D3TestApplication());
    }

    @AfterEach
    void tearDown() {
        tester.destroy();
    }

    @Test
    @DisplayName("asHeaderItem() resolves to a JavaScriptHeaderItem")
    void asHeaderItem_resolvesToJavaScriptHeaderItem() {
        var item = D3Reference.asHeaderItem();

        assertThat(item).isNotNull();
        assertThat(item).isInstanceOf(JavaScriptHeaderItem.class);
    }

    @Test
    @DisplayName("the underlying reference is a lazily-created singleton")
    void reference_isLazySingleton() {
        assertThat(D3Reference.instance()).isSameAs(D3Reference.instance());
    }
}
