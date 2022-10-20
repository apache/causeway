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
package org.apache.causeway.core.metamodel.facets.object.layoutxml;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.layout.component.FieldSet;

public class GridFacetDefaultTest {

    @Test
    public void xxx() throws Exception {

        final AtomicReference<FieldSet> x = new AtomicReference<>();

        FieldSet firstValue = new FieldSet();
        FieldSet otherValue = new FieldSet();

        assertThat(x.get(), is(nullValue()));

        boolean b = x.compareAndSet(null, firstValue);
        assertThat(b, is(true));
        assertThat(x.get(), is(firstValue));

        boolean b2 = x.compareAndSet(null, firstValue);
        assertThat(b2, is(false));
        assertThat(x.get(), is(firstValue));

        boolean b3 = x.compareAndSet(null, otherValue);
        assertThat(b3, is(false));
        assertThat(x.get(), is(firstValue));

        boolean b4 = x.compareAndSet(firstValue, otherValue);
        assertThat(b4, is(true));
        assertThat(x.get(), is(otherValue));

    }
}