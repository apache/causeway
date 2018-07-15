/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.layout.component;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.annotation.RenderDay;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PropertyLayoutData_renderedAsDayBefore_Test {

    PropertyLayoutData data;
    @Before
    public void setUp() throws Exception {
        data = new PropertyLayoutData();

        assertThat(data.getRenderedAsDayBefore(), is(nullValue()));
        assertThat(data.getRenderDay(), is(nullValue()));
    }

    @Test
    public void derive_from_setRenderedAsDayBefore_TRUE_when_null() throws Exception {

        // when
        data.setRenderedAsDayBefore(true);

        // then
        assertThat(data.getRenderDay(), is(RenderDay.AS_DAY_BEFORE));
        assertThat(data.getRenderedAsDayBefore(), is(true));

    }

    @Test
    public void derive_from_setRenderedAsDayBefore_FALSE_when_null() throws Exception {

        // when
        data.setRenderedAsDayBefore(false);

        // then
        assertThat(data.getRenderDay(), is(RenderDay.AS_DAY));
        assertThat(data.getRenderedAsDayBefore(), is(false));

    }

    @Test
    public void ignore_from_setRenderedAsDayBefore_once_set_to_DAY_BEFORE() throws Exception {

        // given
        data.setRenderDay(RenderDay.AS_DAY_BEFORE);

        // when
        data.setRenderedAsDayBefore(false);

        // then (ignored)
        assertThat(data.getRenderDay(), is(RenderDay.AS_DAY_BEFORE));
        assertThat(data.getRenderedAsDayBefore(), is(true));
    }

    @Test
    public void ignore_from_setRenderedAsDayBefore_once_set_to_DAY() throws Exception {

        // given
        data.setRenderDay(RenderDay.AS_DAY);

        // when
        data.setRenderedAsDayBefore(true);

        // then (ignored)
        assertThat(data.getRenderDay(), is(RenderDay.AS_DAY));
        assertThat(data.getRenderedAsDayBefore(), is(false));
    }

}