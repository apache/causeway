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
package org.apache.causeway.applib.layout.grid.bootstrap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BSColTest {

    BSCol bsCol;
    @BeforeEach
    public void setUp() throws Exception {
        bsCol = new BSCol();
    }

    @Test
    public void size_and_span() throws Exception {
        bsCol.setSize(Size.MD);
        bsCol.setSpan(4);

        assertThat(bsCol.toCssClass(), is(equalTo("col-md-4")));
    }

    @Test
    public void extra_css_class() throws Exception {
        bsCol.setSize(Size.SM);
        bsCol.setSpan(8);
        bsCol.setCssClass("foobar");

        assertThat(bsCol.toCssClass(), is(equalTo("col-sm-8 foobar")));
    }

    @Test
    public void with_additional_classes() throws Exception {
        bsCol.setSize(Size.SM);
        bsCol.setSpan(6);

        bsCol.getSizeSpans().add(SizeSpan.with(Size.MD, 5));
        bsCol.getSizeSpans().add(SizeSpan.offset(Size.MD, 2));

        assertThat(bsCol.toCssClass(), is(equalTo("col-sm-6 col-md-5 offset-2")));
    }
}