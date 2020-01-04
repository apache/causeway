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

package org.apache.isis.commons.internal.base;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import org.apache.isis.commons.internal.collections._Lists;

public class ReductionTest {

    @Test
    public void findMinimum() throws Exception {

        final List<Integer> values = _Lists.of(5, 4, 3, 2, 1, 2, 3, 4);

        _Reduction<Integer> toMinReduction = _Reduction.of((min, next)-> next<min ? next : min);

        values.forEach(toMinReduction);

        Assert.assertThat(toMinReduction.getResult().get(), is(1));
    }

    @Test
    public void findMinimum_initialized_low() throws Exception {

        final int initial = -1;

        final List<Integer> values = _Lists.of(5, 4, 3, 2, 1, 2, 3, 4);

        _Reduction<Integer> toMinReduction = _Reduction.of(initial, (min, next)-> next<min ? next : min);

        values.forEach(toMinReduction);

        Assert.assertThat(toMinReduction.getResult().get(), is(initial));
    }

    @Test
    public void findMinimum_initialized_high() throws Exception {

        final int initial = 3;

        final List<Integer> values = _Lists.of(5, 4, 3, 2, 1, 2, 3, 4);

        _Reduction<Integer> toMinReduction = _Reduction.of(initial, (min, next)-> next<min ? next : min);

        values.forEach(toMinReduction);

        Assert.assertThat(toMinReduction.getResult().get(), is(1));
    }


}
