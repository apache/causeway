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
package org.apache.isis.applib.layout.grid.bootstrap3;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SizeSpanTest {

    SizeSpan ss;

    @Before
    public void setUp() throws Exception {
        ss = new SizeSpan();
    }

    @Test
    public void with_no_offset() throws Exception {

        ss.setSize(Size.MD);
        ss.setSpan(4);

        final String s = ss.toCssClassFragment();

        assertThat(s, is(equalTo("col-md-4")));

    }

    @Test
    public void with_offset() throws Exception {

        ss.setSize(Size.SM);
        ss.setSpan(0);
        ss.setOffset(true);

        final String s = ss.toCssClassFragment();

        assertThat(s, is(equalTo("offset-0")));

    }

}