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

import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.BooleanHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.ByteHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.DoubleHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.FloatHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.IntHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.LongHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.ShortHolder;

import lombok.val;

import junit.framework.AssertionFailedError;

public class PojoTester_datatypes_primitive_Test {

    @Test
    public void exercise_byte() {

        // given
        val holder = new ByteHolder();
        Assertions.assertThat(holder).extracting(ByteHolder::getSomeByte).isEqualTo((byte)0);

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(ByteHolder::getSomeByte).isNotEqualTo((byte)0);
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_byte_broken() {

        // given
        val holder = new ByteHolder().butBroken();
        Assertions.assertThat(holder).extracting(ByteHolder::getSomeByte).isEqualTo((byte)0);

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_short() {

        // given
        val holder = new ShortHolder();
        Assertions.assertThat(holder).extracting(ShortHolder::getSomeShort).isEqualTo((short)0);

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(ShortHolder::getSomeShort).isNotEqualTo((short)0);
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_short_broken() {

        // given
        val holder = new ShortHolder().butBroken();
        Assertions.assertThat(holder).extracting(ShortHolder::getSomeShort).isEqualTo((short)0);

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_int() {

        // given
        val holder = new IntHolder();
        Assertions.assertThat(holder).extracting(IntHolder::getSomeInt).isEqualTo((int)0);

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(IntHolder::getSomeInt).isNotEqualTo((int)0);
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_int_broken() {

        // given
        val holder = new IntHolder().butBroken();
        Assertions.assertThat(holder).extracting(IntHolder::getSomeInt).isEqualTo((int)0);

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_long() {

        // given
        val holder = new LongHolder();
        Assertions.assertThat(holder).extracting(LongHolder::getSomeLong).isEqualTo((long)0);

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(LongHolder::getSomeLong).isNotEqualTo((long)0);
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_long_broken() {

        // given
        val holder = new LongHolder().butBroken();
        Assertions.assertThat(holder).extracting(LongHolder::getSomeLong).isEqualTo((long)0);

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_float() {

        // given
        val holder = new FloatHolder();
        Assertions.assertThat(holder).extracting(FloatHolder::getSomeFloat).isEqualTo((float)0);

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(FloatHolder::getSomeFloat).isNotEqualTo((float)0);
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_float_broken() {

        // given
        val holder = new FloatHolder().butBroken();
        Assertions.assertThat(holder).extracting(FloatHolder::getSomeFloat).isEqualTo((float)0);

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_double() {

        // given
        val holder = new DoubleHolder();
        Assertions.assertThat(holder).extracting(DoubleHolder::getSomeDouble).isEqualTo((double)0);

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(DoubleHolder::getSomeDouble).isNotEqualTo((double)0);
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_double_broken() {

        // given
        val holder = new DoubleHolder().butBroken();
        Assertions.assertThat(holder).extracting(DoubleHolder::getSomeDouble).isEqualTo((double)0);

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_boolean() {

        // given
        val holder = new BooleanHolder();
        Assertions.assertThat(holder).extracting(BooleanHolder::isSomeBoolean).isEqualTo(false);

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        //Assertions.assertThat(holder).extracting(BooleanHolder::isSomeBoolean).isNotEqualTo(false);
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }
    
    @Test
    public void exercise_boolean_broken() {

        // given
        val holder = new BooleanHolder().butBroken();
        Assertions.assertThat(holder).extracting(BooleanHolder::isSomeBoolean).isEqualTo(false);

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }


}
