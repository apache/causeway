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
package org.apache.isis.testing.unittestsupport.applib.dom.pojo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.BooleanWrapperHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.ByteWrapperHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.DoubleWrapperHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.FloatWrapperHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.IntWrapperHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.LongWrapperHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.ShortWrapperHolder;

import lombok.val;

import junit.framework.AssertionFailedError;

public class PojoTester_datatypes_wrapper_Test {

    @Test
    public void exercise_byte_wrapper() {

        // given
        val holder = new ByteWrapperHolder();
        Assertions.assertThat(holder).extracting(ByteWrapperHolder::getSomeByteWrapper).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(ByteWrapperHolder::getSomeByteWrapper).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_byte_wrapper_broken() {

        // given
        val holder = new ByteWrapperHolder().butBroken();
        Assertions.assertThat(holder).extracting(ByteWrapperHolder::getSomeByteWrapper).isNull();

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_short_wrapper() {

        // given
        val holder = new ShortWrapperHolder();
        Assertions.assertThat(holder).extracting(ShortWrapperHolder::getSomeShortWrapper).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(ShortWrapperHolder::getSomeShortWrapper).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_short_wrapper_broken() {

        // given
        val holder = new ShortWrapperHolder().butBroken();
        Assertions.assertThat(holder).extracting(ShortWrapperHolder::getSomeShortWrapper).isNull();

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_int_wrapper() {

        // given
        val holder = new IntWrapperHolder();
        Assertions.assertThat(holder).extracting(IntWrapperHolder::getSomeIntWrapper).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(IntWrapperHolder::getSomeIntWrapper).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_int_wrapper_broken() {

        // given
        val holder = new IntWrapperHolder().butBroken();
        Assertions.assertThat(holder).extracting(IntWrapperHolder::getSomeIntWrapper).isNull();

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_long_wrapper() {

        // given
        val holder = new LongWrapperHolder();
        Assertions.assertThat(holder).extracting(LongWrapperHolder::getSomeLongWrapper).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(LongWrapperHolder::getSomeLongWrapper).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_long_wrapper_broken() {

        // given
        val holder = new LongWrapperHolder().butBroken();
        Assertions.assertThat(holder).extracting(LongWrapperHolder::getSomeLongWrapper).isNull();

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_float_wrapper() {

        // given
        val holder = new FloatWrapperHolder();
        Assertions.assertThat(holder).extracting(FloatWrapperHolder::getSomeFloatWrapper).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(FloatWrapperHolder::getSomeFloatWrapper).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_float_wrapper_broken() {

        // given
        val holder = new FloatWrapperHolder().butBroken();
        Assertions.assertThat(holder).extracting(FloatWrapperHolder::getSomeFloatWrapper).isNull();

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_double_wrapper() {

        // given
        val holder = new DoubleWrapperHolder();
        Assertions.assertThat(holder).extracting(DoubleWrapperHolder::getSomeDoubleWrapper).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(DoubleWrapperHolder::getSomeDoubleWrapper).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_double_wrapper_broken() {

        // given
        val holder = new DoubleWrapperHolder().butBroken();
        Assertions.assertThat(holder).extracting(DoubleWrapperHolder::getSomeDoubleWrapper).isNull();

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_boolean_wrapper() {

        // given
        val holder = new BooleanWrapperHolder();
        Assertions.assertThat(holder).extracting(BooleanWrapperHolder::getSomeBooleanWrapper).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(BooleanWrapperHolder::getSomeBooleanWrapper).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_boolean_wrapper_broken() {

        // given
        val holder = new BooleanWrapperHolder().butBroken();
        Assertions.assertThat(holder).extracting(BooleanWrapperHolder::getSomeBooleanWrapper).isNull();

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

}
