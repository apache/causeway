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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Repainting;

public class PropertyLayoutData_repaint_Test {

    PropertyLayoutData data;
    @Before
    public void setUp() throws Exception {
        data = new PropertyLayoutData();

        assertThat(data.getUnchanging(), is(nullValue()));
        assertThat(data.getRepainting(), is(nullValue()));
    }

    @Test
    public void derive_from_setUnchanging_TRUE_when_null() throws Exception {

        // when
        data.setUnchanging(true);

        // then
        assertThat(data.getRepainting(), is(Repainting.NO_REPAINT));
        assertThat(data.getUnchanging(), is(true));

    }

    @Test
    public void derive_from_setUnchanging_FALSE_when_null() throws Exception {

        // when
        data.setUnchanging(false);

        // then
        assertThat(data.getRepainting(), is(Repainting.REPAINT));
        assertThat(data.getUnchanging(), is(false));

    }

    @Test
    public void ignore_from_setUnchanging_once_set_to_NO_REPAINT() throws Exception {

        // given
        data.setRepainting(Repainting.NO_REPAINT);

        // when
        data.setUnchanging(false);

        // then (ignored)
        assertThat(data.getRepainting(), is(Repainting.NO_REPAINT));
        assertThat(data.getUnchanging(), is(true));
    }

    @Test
    public void ignore_from_setUnchanging_once_set_to_REPAINT() throws Exception {

        // given
        data.setRepainting(Repainting.REPAINT);

        // when
        data.setUnchanging(true);

        // then (ignored)
        assertThat(data.getRepainting(), is(Repainting.REPAINT));
        assertThat(data.getUnchanging(), is(false));
    }

}