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

public class BS3ColTest {

    BS3Col bs3Col;
    @Before
    public void setUp() throws Exception {
        bs3Col = new BS3Col();
    }

    @Test
    public void size_and_span() throws Exception {
        bs3Col.setSize(Size.MD);
        bs3Col.setSpan(4);

        assertThat(bs3Col.toCssClass(), is(equalTo("col-md-4")));
    }

    @Test
    public void extra_css_class() throws Exception {
        bs3Col.setSize(Size.SM);
        bs3Col.setSpan(8);
        bs3Col.setCssClass("foobar");

        assertThat(bs3Col.toCssClass(), is(equalTo("col-sm-8 foobar")));
    }

    @Test
    public void with_additional_classes() throws Exception {
        bs3Col.setSize(Size.SM);
        bs3Col.setSpan(6);

        bs3Col.getSizeSpans().add(SizeSpan.with(Size.MD, 5));
        bs3Col.getSizeSpans().add(SizeSpan.offset(Size.MD, 2));

        assertThat(bs3Col.toCssClass(), is(equalTo("col-sm-6 col-md-5 col-md-offset-2")));
    }
}