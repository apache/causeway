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
package org.apache.causeway.applib.layout.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PropertyLayoutData_renderedAsDayBefore_Test {

    PropertyLayoutData data;
    @BeforeEach
    public void setUp() throws Exception {
        data = new PropertyLayoutData();

        assertThat(data.getDateRenderAdjustDays(), is(0));
    }

    @Test
    public void ignore_from_setRenderedAsDayBefore_once_set_to_DAY_BEFORE() throws Exception {

        // given
        data.setDateRenderAdjustDays(-1);

        // then (ignored)
        assertThat(data.getDateRenderAdjustDays(), is(-1));
    }

    @Test
    public void ignore_from_setRenderedAsDayBefore_once_set_to_DAY() throws Exception {

        // given
        data.setDateRenderAdjustDays(0);

        // then (ignored)
        assertThat(data.getDateRenderAdjustDays(), is(0));
    }

}